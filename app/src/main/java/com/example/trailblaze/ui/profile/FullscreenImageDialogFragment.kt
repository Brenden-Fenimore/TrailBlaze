package com.example.trailblaze.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FullscreenImageDialogFragment : DialogFragment() {

    interface OnPhotoDeletedListener {
        fun onPhotoDeleted()
    }

    private lateinit var imageView: ImageView
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private var isOwnProfile: Boolean = false
    private var currentIndex: Int = 0
    private lateinit var imageUrls: MutableList<String>
    private lateinit var photosAdapter: PhotosAdapter
    private var photoDeletedListener: OnPhotoDeletedListener? = null

    companion object {
        fun newInstance(imageUrls: List<String>, initialPosition: Int, isOwnProfile: Boolean): FullscreenImageDialogFragment {
            val fragment = FullscreenImageDialogFragment()
            val args = Bundle().apply {
                putStringArrayList("imageUrls", ArrayList(imageUrls))
                putInt("position", initialPosition)
                putBoolean("isOwnProfile", isOwnProfile)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fullscreen_image, container, false)
        imageView = view.findViewById(R.id.fullscreenImageView)
        nextButton = view.findViewById(R.id.nextImageButton)
        prevButton = view.findViewById(R.id.prevImageButton)
        deleteButton = view.findViewById(R.id.deleteImageButton)

        imageUrls = (arguments?.getStringArrayList("imageUrls") ?: emptyList()) as MutableList<String>
        currentIndex = arguments?.getInt("position") ?: 0

        displayImage(currentIndex)

        nextButton.setOnClickListener { navigateImage(1) }
        prevButton.setOnClickListener { navigateImage(-1) }

        isOwnProfile = arguments?.getBoolean("isOwnProfile") ?: false

        // Set delete button visibility based on ownership
        deleteButton.visibility = if (isOwnProfile) View.VISIBLE else View.GONE

        deleteButton.setOnClickListener {
            Log.d("FullscreenImageDialog", "Delete button clicked")
            deleteCurrentPhoto()  // Call the delete function when the delete button is clicked
        }


        return view
    }

    fun setPhotosAdapter(adapter: PhotosAdapter) {
        this.photosAdapter = adapter
    }

    fun setOnPhotoDeletedListener(listener: OnPhotoDeletedListener) {
        photoDeletedListener = listener
    }

    private fun displayImage(index: Int) {
        currentIndex = index
        val imageUrl = imageUrls[currentIndex]

        // Loads images through Glide
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        // Disable navigation buttons at the ends
        prevButton.visibility = if (currentIndex > 0) View.VISIBLE else View.INVISIBLE
        nextButton.visibility = if (currentIndex < imageUrls.size - 1) View.VISIBLE else View.INVISIBLE
    }

    private fun navigateImage(direction: Int) {
        val newIndex = currentIndex + direction
        if (newIndex in imageUrls.indices) {
            displayImage(newIndex)
        }
    }

    private fun deleteCurrentPhoto() {
        if (imageUrls.isEmpty() || currentIndex !in imageUrls.indices) return

        val deletedImageUrl = imageUrls[currentIndex]
        Log.d("DeletePhoto", "Attempting to delete photo: $deletedImageUrl")

        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract storage path for Firebase Storage deletion
        val storagePath = deletedImageUrl.substringAfter("/o/").substringBefore("?").replace("%2F", "/")
        val storageRef = FirebaseStorage.getInstance().getReference(storagePath)

        // Delete from Firebase Storage
        storageRef.delete()
            .addOnSuccessListener {
                Log.d("DeletePhoto", "Deleted photo from Firebase Storage")

                // Query Firestore to find and delete the document by URL
                val photosCollection = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("photos")

                photosCollection.whereEqualTo("url", deletedImageUrl).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            Log.d("DeletePhoto", "No matching Firestore document found for URL: $deletedImageUrl")
                        } else {
                            // Delete each matching document (in case of duplicates)
                            for (document in querySnapshot.documents) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        Log.d("DeletePhoto", "Deleted photo document from Firestore")

                                        // Remove the photo from local list and update UI
                                        imageUrls.removeAt(currentIndex)
                                        photosAdapter.removeItem(currentIndex)

                                        if (currentIndex >= imageUrls.size) {
                                            currentIndex = imageUrls.size - 1
                                        }
                                        if (imageUrls.isNotEmpty()) {
                                            displayImage(currentIndex)
                                        } else {
                                            dismiss() // Close dialog if no images are left
                                        }

                                        Toast.makeText(context, "Photo deleted successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("DeletePhoto", "Error deleting photo document from Firestore", e)
                                        Toast.makeText(context, "Failed to delete photo document", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeletePhoto", "Error querying Firestore for photo document", e)
                        Toast.makeText(context, "Error finding photo document", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DeletePhoto", "Error deleting photo from Firebase Storage", e)
                Toast.makeText(context, "Failed to delete photo from Storage", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

}