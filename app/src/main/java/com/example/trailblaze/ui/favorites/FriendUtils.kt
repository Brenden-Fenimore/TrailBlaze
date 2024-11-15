package com.example.trailblaze.ui.favorites

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.ui.profile.Friends

// Object that contains utility functions related to friends
object FriendUtils {

    // Function to fetch details of friends based on their IDs
    fun fetchFriendsDetails(friendIds: List<String>, firestore: FirebaseFirestore, onComplete: (List<Friends>) -> Unit) {
        // Create a list of tasks to fetch each friend's document from Firestore
        val tasks = friendIds.map { friendId ->
            firestore.collection("users").document(friendId).get() // Get the document for each friend ID
        }

        // Mutable list to hold the details of favorite friends
        val favoriteFriends = mutableListOf<Friends>()
        // Variable to track the number of completed requests
        var completedRequests = 0

        // Iterate through each task and set up listeners for success and failure
        tasks.forEach { task ->
            task.addOnSuccessListener { document ->
                // Check if the document exists and is not null
                if (document != null && document.exists()) {
                    // Retrieve friend details from the document
                    val userId = document.id // Get the friend ID
                    val username = document.getString("username") ?: "Unknown" // Get the username; default to "Unknown" if not available
                    val profileImageUrl = document.getString("profileImageUrl") ?: "" // Get the profile image URL; default to empty string if not available
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false // Get the privacy status; default to false if not available
                    val watcherVisible = document.getBoolean("isWatcherVisible") ?: false
                    // Create a Friends object with the retrieved details
                    val friend = Friends(userId, username, profileImageUrl, isPrivateAccount, watcherVisible)
                    // Add the friend to the list of favorite friends
                    favoriteFriends.add(friend)
                }
                // Increment the count of completed requests
                completedRequests++
                // Check if all requests have completed
                if (completedRequests == friendIds.size) {
                    // Call the onComplete callback with the list of favorite friends
                    onComplete(favoriteFriends)
                }
            }.addOnFailureListener { exception ->
                // Log an error message if the request fails
                Log.e("FriendUtils", "Error fetching friend details: ${exception.message}")
                // Increment the count of completed requests even on failure
                completedRequests++
                // Check if all requests have completed
                if (completedRequests == friendIds.size) {
                    // Call the onComplete callback with the list of favorite friends, even if some requests failed
                    onComplete(favoriteFriends)
                }
            }
        }
    }
}