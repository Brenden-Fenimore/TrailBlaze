package com.example.trailblaze.ui.favorites

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.ui.profile.Friends

object FriendUtils {

    fun fetchFriendsDetails(friendIds: List<String>, firestore: FirebaseFirestore, onComplete: (List<Friends>) -> Unit) {
        val tasks = friendIds.map { friendId ->
            firestore.collection("users").document(friendId).get()
        }

        val favoriteFriends = mutableListOf<Friends>()
        var completedRequests = 0

        tasks.forEach { task ->
            task.addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userId = document.id
                    val username = document.getString("username") ?: "Unknown"
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false
                    val friend = Friends(userId, username, profileImageUrl, isPrivateAccount)
                    favoriteFriends.add(friend)
                }
                completedRequests++
                if (completedRequests == friendIds.size) {
                    onComplete(favoriteFriends)
                }
            }.addOnFailureListener { exception ->
                Log.e("FriendUtils", "Error fetching friend details: ${exception.message}")
                completedRequests++
                if (completedRequests == friendIds.size) {
                    onComplete(favoriteFriends)
                }
            }
        }
    }
}