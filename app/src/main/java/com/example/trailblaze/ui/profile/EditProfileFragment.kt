package com.example.trailblaze.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.trailblaze.R
import android.view.LayoutInflater
import com.example.trailblaze.databinding.FragmentEditProfileBinding
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.trailblaze.firestore.ImageLoader
import com.example.trailblaze.firestore.UserRepository
import com.example.trailblaze.ui.achievements.AchievementManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    lateinit var seekBar: SeekBar
    lateinit var selectedValueTextView: TextView
    private var selectedFilterValue: Double = 0.0
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userRepository: UserRepository
    private lateinit var achievementManager: AchievementManager
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

        //setup firestore instance
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(firestore)
        achievementManager = AchievementManager(requireContext())
        loadProfilePicture()

        // Fetch and display current user data
        loadUserProfile()

        // Load saved visibility settings from Firebase
        loadVisibilitySettings()

        // Setup other views and buttons
        setupUI()


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        storageReference = FirebaseStorage.getInstance().reference.child("profile_pictures")

        setupStateEditText()

        seekBar = view.findViewById(R.id.seekBar)
        selectedValueTextView = view.findViewById(R.id.range)
        setupSeekBarListener()

        // Initialize views using ViewBinding
        binding.backButton.setOnClickListener {
            // Navigate to the profile fragment
            findNavController().navigate(R.id.action_editProfileFragment_to_navigation_profile)
        }

        // Add a button to trigger image selection
        binding.cameraIcon.setOnClickListener {
            selectImage()
        }

        // Initialize views using ViewBinding
        binding.settingsButton.setOnClickListener {
            //Navigate to the settings page
            findNavController().navigate(R.id.action_editProfileFragment_to_settingsScreenActivity)
        }

        binding.leaderboardSwitch.setOnCheckedChangeListener { _, _ -> updateVisibilitySettings() }
        binding.photosSwitch.setOnCheckedChangeListener { _, _ -> updateVisibilitySettings() }
        binding.favoritetrailsSwitch.setOnCheckedChangeListener { _, _ -> updateVisibilitySettings() }
        binding.watcherSwitch.setOnCheckedChangeListener { _, _ -> updateVisibilitySettings() }
        binding.sharelocationSwitch.setOnCheckedChangeListener { _, _ -> updateVisibilitySettings() }

        setupSpinners()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Check and update the SeekBar label when the fragment resumes
        val isMetric = sharedPreferences.getBoolean("isMetricUnits", true)
        updateSeekBarLabel(binding.seekBar.progress) // Update label based on current SeekBar progress
    }

    private fun setupStateEditText() {
        binding.editState.filters = arrayOf(InputFilter.LengthFilter(2))

        binding.editState.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && !s.toString().isEmpty()) {
                    val uppercase = s.toString().uppercase()
                    if (uppercase != s.toString()) {
                        binding.editState.setText(uppercase)
                        binding.editState.setSelection(uppercase.length)
                    }
                }
            }
        })
    }

    private fun validateState(): Boolean {
        val stateText = binding.editState.text.toString()
        if (stateText.length != 2) {
            binding.editState.error = "State must be 2 letters"
            return false
        }
        return true
    }

    private fun setupSpinners() {
        // Set up a theme-aware custom adapter for the Terrain spinner
        // Load the array of terrain options from resources
        val terrainOptions = resources.getStringArray(R.array.terrain_options)
        // Apply the custom adapter to display each option in a theme-aware way
        binding.terrainSpinner.adapter = ArrayAdapter(
            requireContext(),
            // Custom layout for consistent text color based on theme
            R.layout.spinner_item,
            terrainOptions
        )

        // Set up the Fitness Level spinner with theme-aware styling
        // Load the array of fitness level options from resources
        val fitnessLevelOptions = resources.getStringArray(R.array.fitness_level_options)
        // Apply the custom adapter to the spinner for fitness levels
        binding.fitnessLevelSpinner.adapter = ArrayAdapter(
            requireContext(),
            // Custom layout for consistent text color based on theme
            R.layout.spinner_item,
            fitnessLevelOptions
        )

        // Set up the Difficulty Level spinner with theme-aware styling
        // Load the array of difficulty level options from resources
        val difficultyOptions = resources.getStringArray(R.array.difficulty_level_options)
        // Apply the custom adapter to the spinner for difficulty levels
        binding.difficultySpinner.adapter = ArrayAdapter(
            requireContext(),
            // Custom layout for consistent text color based on theme
            R.layout.spinner_item,
            difficultyOptions
        )

        // Set up the Type of Hike spinner with theme-aware styling
        // Load the array of hike type options from resources
        val hikeTypeOptions = resources.getStringArray(R.array.hike_type_options)
        // Apply the custom adapter to the spinner for hike types
        binding.typeOfHikeSpinner.adapter = ArrayAdapter(
            requireContext(),
            // Custom layout for consistent text color based on theme
            R.layout.spinner_item,
            hikeTypeOptions
        )
    }

    private fun setupSeekBarListener() {
        seekBar.apply {
            max = 50  // Maximum distance in km/miles
            progress = 10  // Default value

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // Ensure minimum value of 1
                    val actualProgress = if (progress < 1) 1 else progress
                    selectedFilterValue = actualProgress.toDouble()
                    updateSeekBarLabel(actualProgress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun updateSeekBarLabel(progress: Int) {
        val isMetric = sharedPreferences.getBoolean("isMetricUnits", true)
        val distance = if (isMetric) {
            "$progress km"  // Metric (kilometers)
        } else {
            "${(progress * 0.621371).toInt()} miles"  // Imperial (miles)
        }

        // Add zoom level reference
        val zoomLevel = when (progress) {
            in 1..2 -> "Street level view"
            in 3..5 -> "Neighborhood view"
            in 6..10 -> "City view"
            in 11..20 -> "Regional view"
            in 21..35 -> "State view"
            else -> "Wide area view"
        }

        selectedValueTextView.text = "$distance ($zoomLevel)"
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {

                // Populate the EditText fields with current user data
                binding.editUsername.setText(document.getString("username"))
                binding.editEmail.setText(document.getString("email"))
                binding.editDob.setText(document.getString("dateOfBirth"))
                binding.editCity.setText(document.getString("city"))
                binding.editState.setText(document.getString("state"))
                binding.editZip.setText(document.getString("zip"))
                binding.editPhone.setText(document.getString("phone"))

                // For spinners, you can select the right value programmatically
                val terrain = document.getString("terrain") ?: ""
                setSpinnerSelection(binding.terrainSpinner, terrain)

                val fitnessLevel = document.getString("fitnessLevel") ?: ""
                setSpinnerSelection(binding.fitnessLevelSpinner, fitnessLevel)

                val difficulty = document.getString("difficulty") ?: ""
                setSpinnerSelection(binding.difficultySpinner, difficulty)

                val distance = document.getDouble("distance") ?: 0.0
                binding.seekBar.progress = distance.toInt().coerceIn(0, binding.seekBar.max)
                binding.range.text = distance.toString()

                binding.leaderboardSwitch.isChecked = document.getBoolean("showLeaderboard") == true
                binding.photosSwitch.isChecked = document.getBoolean("showPhotos") == true
                binding.favoritetrailsSwitch.isChecked = document.getBoolean("showFavoriteTrails") == true
                binding.watcherSwitch.isChecked = document.getBoolean("showWatcher") == true
                binding.sharelocationSwitch.isChecked = document.getBoolean("showLocation") == true
            }
        }.addOnFailureListener { exception ->
            Log.e("EditProfile", "Error loading user data: ${exception.message}")
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(value, ignoreCase = true)) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                // Handle the selected image URI
                uploadProfilePicture(imageUri)
            }
        }
    }

    private val IMAGE_PICK_CODE = 1000

    private fun uploadProfilePicture(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val imageName = "$userId.jpg" // Or use a unique filename
        val imageRef = storageReference.child(imageName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Update the user's profile picture in your database or UI
                    updateProfilePicture(downloadUri.toString())
                    // Update the user's document in Firestore
                    val userRef = firestore.collection("users").document(userId)
                    userRef.update("profileImageUrl", downloadUri.toString())
                        .addOnSuccessListener {
                            Log.d("Firestore", "Profile picture URL updated successfully")

                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firestore", "Error updating profile picture URL: ${exception.message}")
                        }
                    Log.d("Firebase", "Profile picture uploaded successfully: ${downloadUri.toString()}")
                }
            }
    }

    // Create a method to load the image into the ImageView
    private fun updateProfilePicture(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_downloading_24) // Placeholder while loading
            .error(R.drawable.no_image_available) // Fallback image if loading fails
            .circleCrop() // To display the image in a circular format
            .into(binding.profilePicture) // Assuming you have an ImageView with this ID in your layout
    }

    private fun setupUI() {
        // Initialize views
        binding.updateProfileButton.setOnClickListener {
            updateUserProfile()
            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        // Collect updated profile data from input fields
        val updatedUserData = mapOf(
            "username" to binding.editUsername.text.toString(),
            "email" to binding.editEmail.text.toString(),
            "dob" to binding.editDob.text.toString(),
            "city" to binding.editCity.text.toString(),
            "state" to binding.editState.text.toString(),
            "zip" to binding.editZip.text.toString(),
            "phone" to binding.editPhone.text.toString(),
            "terrain" to binding.terrainSpinner.selectedItem.toString(),
            "fitnessLevel" to binding.fitnessLevelSpinner.selectedItem.toString(),
            "difficulty" to binding.difficultySpinner.selectedItem.toString(),
            "typeOfHike" to binding.typeOfHikeSpinner.selectedItem.toString(),
            "distance" to selectedFilterValue

        )

        // Call repository to update Firestore
        userRepository.updateUserProfile(userId, updatedUserData) { success ->
            if (success) {

                // Profile updated successfully
                Log.d("EditProfile", "Profile updated successfully")

                Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()

                // Navigate back to the profile screen
                findNavController().navigate(R.id.action_editProfileFragment_to_navigation_profile)

                // Optionally, navigate back or show a success message
            } else {
                // Handle failure
                Log.e("EditProfile", "Failed to update profile")
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        userRepository.getUserProfileImage(userId) { imageUrl ->
            ImageLoader.loadProfilePicture(requireContext(), binding.profilePicture, imageUrl)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateVisibilitySettings() {
        val currentUserId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "leaderboardVisible" to binding.leaderboardSwitch.isChecked,
            "photosVisible" to binding.photosSwitch.isChecked,
            "favoriteTrailsVisible" to binding.favoritetrailsSwitch.isChecked,
            "watcherVisible" to binding.watcherSwitch.isChecked,
            "shareLocationVisible" to binding.sharelocationSwitch.isChecked
        )

        firestore.collection("users").document(currentUserId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Error updating visibility settings: ", exception)
                Toast.makeText(requireContext(), "Failed to update settings.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadVisibilitySettings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false

                    if (isPrivateAccount) {
                        // If account is private, set all visibility switches to false
                        binding.leaderboardSwitch.isChecked = false
                        binding.photosSwitch.isChecked = false
                        binding.favoritetrailsSwitch.isChecked = false
                        binding.watcherSwitch.isChecked = false
                        binding.sharelocationSwitch.isChecked = false

                        // Update these values in Firestore
                        updateAllVisibilitySettings(false)
                    } else {
                        // Load normal visibility settings if account is public
                        binding.leaderboardSwitch.isChecked = document.getBoolean("leaderboardVisible") ?: false
                        binding.photosSwitch.isChecked = document.getBoolean("photosVisible") ?: false
                        binding.favoritetrailsSwitch.isChecked = document.getBoolean("favoriteTrailsVisible") ?: false
                        binding.watcherSwitch.isChecked = document.getBoolean("watcherVisible") ?: false
                        binding.sharelocationSwitch.isChecked = document.getBoolean("shareLocationVisible") ?: false
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Error loading visibility settings", exception)
            }
    }

    private fun updateAllVisibilitySettings(visible: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updates = hashMapOf(
            "leaderboardVisible" to visible,
            "photosVisible" to visible,
            "favoriteTrailsVisible" to visible,
            "watcherVisible" to visible,
            "shareLocationVisible" to visible
        )

        firestore.collection("users").document(userId)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("EditProfileFragment", "All visibility settings updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Error updating visibility settings", exception)
            }
    }

}


