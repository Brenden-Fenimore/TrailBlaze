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
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.trailblaze.firestore.ImageLoader
import com.example.trailblaze.firestore.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

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

        return view
    }

    private fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //update the TextView with the current progress/value of the seekBar
                selectedValueTextView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when the user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //get the progress value when the user stops dragging the SeekBar
                selectedFilterValue = (seekBar?.progress ?: 0.0).toDouble()

            }
        })
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
}


