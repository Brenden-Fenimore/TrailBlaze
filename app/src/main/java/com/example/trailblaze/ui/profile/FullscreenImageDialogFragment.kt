package com.example.trailblaze.ui.profile

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class FullscreenImageDialogFragment : DialogFragment() {

    private lateinit var imageView: ImageView
    private lateinit var nextButton: ImageButton
    private lateinit var prevButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var editCaptionButton: ImageButton
    private var isOwnProfile: Boolean = false
    private var currentIndex: Int = 0
    private lateinit var imageUrls: MutableList<String>
    private lateinit var photosAdapter: PhotosAdapter
    private lateinit var captionTextView: TextView
    private var photoCaptions: MutableMap<String, String> = mutableMapOf()
    private lateinit var locationTextView: TextView
    private lateinit var editLocationButton: ImageButton

    companion object {
        fun newInstance(
            imageUrls: List<String>,
            initialPosition: Int,
            isOwnProfile: Boolean
        ): FullscreenImageDialogFragment {
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
        editCaptionButton = view.findViewById(R.id.editCaptionButton)
        captionTextView = view.findViewById(R.id.captionTextView)
        locationTextView = view.findViewById(R.id.locationTextView)
        editLocationButton = view.findViewById(R.id.editLocationButton)

        imageUrls = (arguments?.getStringArrayList("imageUrls") ?: emptyList()) as MutableList<String>
        currentIndex = arguments?.getInt("position") ?: 0

        displayImage(currentIndex)

        nextButton.setOnClickListener { navigateImage(1) }
        prevButton.setOnClickListener { navigateImage(-1) }

        isOwnProfile = arguments?.getBoolean("isOwnProfile") ?: false

        // Set delete button visibility based on ownership
        deleteButton.visibility = if (isOwnProfile) View.VISIBLE else View.GONE
        editCaptionButton.visibility = if (isOwnProfile) View.VISIBLE else View.GONE
        editLocationButton.visibility = if (isOwnProfile) View.VISIBLE else View.GONE

        deleteButton.setOnClickListener {
            // Show a confirmation dialog
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete") { _, _ ->
                    // User confirmed, proceed with deletion
                    Log.d("FullscreenImageDialog", "Delete confirmed")
                    deleteCurrentPhoto()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User cancelled, dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        editCaptionButton.setOnClickListener {
            Log.d("FullscreenImageDialog", "Edit Caption button clicked")
            openEditCaptionDialog(imageUrls[currentIndex])
        }

        editLocationButton.setOnClickListener {
            val imageUrl = imageUrls[currentIndex] // Get the current image URL
            promptForLocationUpdate(imageUrl)
        }

        return view
    }

    fun setPhotosAdapter(adapter: PhotosAdapter) {
        this.photosAdapter = adapter
    }

    private fun displayImage(index: Int) {
        currentIndex = index
        val imageUrl = imageUrls[currentIndex]

        // Reset text views at the start to prevent carryover
        captionTextView.text = ""
        captionTextView.visibility = View.GONE
        locationTextView.text = ""
        locationTextView.visibility = View.GONE

        // Loads images through Glide
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        // Disable navigation buttons at the ends
        prevButton.visibility = if (currentIndex > 0) View.VISIBLE else View.INVISIBLE
        nextButton.visibility = if (currentIndex < imageUrls.size - 1) View.VISIBLE else View.INVISIBLE

        // Check if the caption exists in local map
        val caption = photoCaptions[imageUrl]
        if (caption.isNullOrEmpty()) {
            // If no caption is available locally, fetch it from Firestore
            fetchCaptionFromFirestore(imageUrl)
        } else {
            // Display the caption if it exists
            captionTextView.text = caption
            captionTextView.visibility = View.VISIBLE
        }

        // Display location based on EXIF data
        loadGeoLocation(imageUrl)
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
                                        Toast.makeText(context, "Failed to delete photo document", Toast.LENGTH_SHORT)
                                            .show()
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

    private fun openEditCaptionDialog(imageUrl: String) {
        val captionInput = EditText(context)
        captionInput.hint = "Caption your photo:"

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Caption")
            .setView(captionInput)
            .setPositiveButton("Save") { _, _ ->
                val newCaption = captionInput.text.toString()
                saveCaptionToFirestore(imageUrl, newCaption)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCaptionToFirestore(imageUrl: String, caption: String) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Find the document matching the image URL and update the caption
        val photosCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("photos")

        photosCollection.whereEqualTo("url", imageUrl).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.d("SaveCaption", "No matching Firestore document found for URL: $imageUrl")
                } else {
                    // Update each matching document (in case of duplicates)
                    for (document in querySnapshot.documents) {
                        document.reference.update("caption", caption)
                            .addOnSuccessListener {
                                Log.d("SaveCaption", "Caption updated successfully")
                                photoCaptions[imageUrl] = caption
                                displayImage(currentIndex) // Refresh to show updated caption
                                Toast.makeText(context, "Caption saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("SaveCaption", "Error updating caption", e)
                                Toast.makeText(context, "Failed to save caption", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SaveCaption", "Error querying Firestore for caption", e)
                Toast.makeText(context, "Error finding photo document", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCaptionFromFirestore(imageUrl: String) {
        Log.d("FetchCaption", "Fetching caption for imageUrl: $imageUrl")

        val userId = getCurrentUserId()  // Check current user's photo, if needed
        if (userId.isEmpty()) {
            Log.d("FetchCaption", "User ID is empty, returning")
            return
        }

        // Fetch from all users' photo collections
        val firestore = FirebaseFirestore.getInstance()

        // Query all users' photos to find the matching imageUrl and fetch the caption
        firestore.collection("users")
            .get() // Get all users
            .addOnSuccessListener { usersSnapshot ->
                var found = false
                for (userDoc in usersSnapshot.documents) {
                    val userPhotosCollection = userDoc.reference.collection("photos")

                    // Query photos collection of each user to find the photo by its URL
                    userPhotosCollection.whereEqualTo("url", imageUrl).get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.documents.isNotEmpty()) {
                                val caption = querySnapshot.documents.firstOrNull()?.getString("caption")
                                if (!caption.isNullOrEmpty()) {
                                    Log.d("FetchCaption", "Found caption: $caption")
                                    photoCaptions[imageUrl] = caption
                                    captionTextView.text = caption
                                    captionTextView.visibility = View.VISIBLE
                                } else {
                                    Log.d("FetchCaption", "No caption found for this imageUrl.")
                                }
                                found = true
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FetchCaption", "Error fetching caption from Firestore", e)
                        }

                    // Break the loop once we've found the caption to prevent querying other users
                    if (found) {
                        return@addOnSuccessListener
                    }
                }
                if (!found) {
                    Log.d("FetchCaption", "No caption found for the imageUrl in any user’s photos.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FetchCaption", "Error querying users from Firestore", e)
            }
    }

    private fun loadGeoLocation(imageUrl: String) {
        Log.d("GeoLocation", "Attempting to load geo-location for image: $imageUrl")

        // First, try to load the location from Firestore
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            Log.d("GeoLocation", "User not authenticated")
            locationTextView.visibility = View.GONE
            return
        }

        val photosCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("photos")

        // Fetch the document for the photo
        photosCollection.whereEqualTo("url", imageUrl).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.d("GeoLocation", "No location found for this image in Firestore.")
                    // If no location in Firestore, fallback to EXIF
                    fetchGeoLocationFromExif(imageUrl)
                } else {
                    // Fetch the location if it exists
                    val document = querySnapshot.documents.firstOrNull()
                    val location = document?.getString("location")
                    val isLocationManuallySet = document?.getBoolean("isLocationManuallySet") ?: false

                    if (isLocationManuallySet && !location.isNullOrEmpty()) {
                        // If manually set location exists in Firestore, show it
                        Log.d("GeoLocation", "Manually set location from Firestore: $location")
                        locationTextView.text = location
                        locationTextView.visibility = View.VISIBLE
                    } else {
                        Log.d("GeoLocation", "No manual location found, falling back to EXIF.")
                        fetchGeoLocationFromExif(imageUrl)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GeoLocation", "Error fetching location from Firestore", e)
                fetchGeoLocationFromExif(imageUrl)  // If Firestore fetch fails, try EXIF
            }
    }

    private fun getLocationName(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]

                    // Debugging log to inspect all address components
                    Log.d("GeoLocation", "Full address components: ${address.featureName}, ${address.locality}, ${address.adminArea}, ${address.countryName}")

                    // Prioritize locality (city, region) over feature name (which may be street number)
                    var locationName = address.locality // Try to get locality first

                    // If locality is not available, try to get the feature name (e.g., point of interest)
                    if (locationName.isNullOrEmpty()) {
                        locationName = address.featureName
                    }

                    // If neither locality nor feature name is found, check administrative area (state/county)
                    if (locationName.isNullOrEmpty()) {
                        locationName = address.adminArea
                    }

                    // If still no location name, fallback to country name
                    if (locationName.isNullOrEmpty()) {
                        locationName = address.countryName
                    }

                    // Log the location name for debugging
                    Log.d("GeoLocation", "Refined location name: $locationName")

                    return locationName
                } else {
                    Log.d("GeoLocation", "No address found for coordinates.")
                }
            }
        } catch (e: IOException) {
            Log.e("GeoLocation", "Geocoder failed", e)
        }
        return null
    }
    private fun promptForLocationUpdate(imageUrl: String) {
        // Example implementation using an AlertDialog to enter location manually
        val editText = EditText(context)
        editText.hint = "Enter new location"

        AlertDialog.Builder(requireContext())
            .setTitle("Update Location Tag")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                val newLocation = editText.text.toString()
                updateLocationTag(imageUrl, newLocation)  // Pass the imageUrl
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateLocationTag(imageUrl: String, newLocation: String) {
        locationTextView.text = newLocation
        locationTextView.visibility = View.VISIBLE
        // Optionally, save this update to Firestore
        saveLocationToFirestore(imageUrl, newLocation)
    }

    private fun saveLocationToFirestore(imageUrl: String, newLocation: String) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Find the document matching the image URL and update the location
        val photosCollection = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("photos")

        photosCollection.whereEqualTo("url", imageUrl).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.d("SaveLocation", "No matching Firestore document found for URL: $imageUrl")
                } else {
                    // Update each matching document (in case of duplicates)
                    for (document in querySnapshot.documents) {
                        document.reference.update("location", newLocation)
                            .addOnSuccessListener {
                                Log.d("SaveLocation", "Location updated successfully")
                                // Optionally update the location in the local map or state
                                Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("SaveLocation", "Error updating location", e)
                                Toast.makeText(context, "Failed to save location", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SaveLocation", "Error querying Firestore for location", e)
                Toast.makeText(context, "Error finding photo document", Toast.LENGTH_SHORT).show()
            }
    }

}

