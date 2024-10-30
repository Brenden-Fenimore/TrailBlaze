package com.example.trailblaze.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.trailblaze.R
import android.view.LayoutInflater
import com.example.trailblaze.databinding.FragmentEditProfileBinding
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.trailblaze.firestore.ImageLoader
import com.example.trailblaze.firestore.UserRepository
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        //setup firestore instance
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(firestore)
        loadProfilePicture()

        // Fetch and display current user data
        loadUserProfile()

        // Load saved visibility settings from Firebase
        loadVisibilitySettings()

        // Setup other views and buttons
        setupUI()


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        storageReference = FirebaseStorage.getInstance().reference.child("profile_pictures")



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

        return view
    }


    private fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the TextView with the current progress/value of the SeekBar
                selectedValueTextView.text = progress.toString()
                selectedFilterValue = progress.toDouble()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
            .placeholder(R.drawable.account_circle) // Placeholder image while loading
            .error(R.drawable.account_circle) // Error image if the load fails
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
                    // Retrieve each visibility setting from Firebase and set the switch states
                    binding.leaderboardSwitch.isChecked = document.getBoolean("leaderboardVisible") ?: false
                    binding.photosSwitch.isChecked = document.getBoolean("photosVisible") ?: false
                    binding.favoritetrailsSwitch.isChecked = document.getBoolean("favoriteTrailsVisible") ?: false
                    binding.watcherSwitch.isChecked = document.getBoolean("watcherVisible") ?: false
                    binding.sharelocationSwitch.isChecked = document.getBoolean("shareLocationVisible") ?: false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditProfileFragment", "Error loading visibility settings", exception)
            }
    }
}


