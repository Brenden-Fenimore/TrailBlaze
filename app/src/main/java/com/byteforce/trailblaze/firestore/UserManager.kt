package com.byteforce.trailblaze.firestore

import com.byteforce.trailblaze.nps.Park
import com.google.firebase.firestore.FirebaseFirestore

// Object to manage user-related operations, such as fetching user data and managing the current user.
object UserManager {
    // Private variable to hold the current user instance, initially null
    private var currentUser: User? = null

    // Function to get the current user
    fun getCurrentUser(): User? {
        return currentUser // Return the current user, or null if no user is set
    }

    fun getCurrentUserState(): String? {
        return currentUser?.state
    }

    // Function to set the current user
    fun setCurrentUser(user: User) {
        currentUser = user // Assign the provided user to the currentUser variable
    }

    // Function to fetch user data from Firestore based on userId
    fun fetchUserData(userId: String, firestore: FirebaseFirestore, callback: (User?) -> Unit) {

        // Query the "users" collection in Firestore using the provided userId
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Check if the document exists and is not null
                if (document != null && document.exists()) {
                    // Create a User instance from the retrieved document data
                    val user = User(
                        uid = document.id,
                        username = document.getString("username") ?: "",
                        state = document.getString("state") ?: "",
                        city = document.getString("city") ?: "",
                        dateOfBirth = document.getString("dateOfBirth") ?: "",
                        difficulty = document.getString("difficulty") ?: "",
                        fitnessLevel = document.getString("fitnessLevel") ?: "",
                        phone = document.getString("phone") ?: "",
                        zip = document.getString("zip") ?: "",
                        profileImageUrl = document.getString("profileImageUrl") ?: "",
                        distance = document.getDouble("distance") ?: 0.0,
                        email = document.getString("email") ?: "",
                        friends = document.get("friends") as? List<String> ?: emptyList(),
                        favoriteParks = document.get("favoriteParks") as? List<Park> ?: emptyList(),
                        badges = document.get("badges") as? List<String> ?: emptyList(),
                        bucketListParks = document.get("bucketList") as? List<Park> ?: emptyList(),
                        isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false,
                        favoriteTrailsVisible = document.getBoolean("favoriteTrailsVisible") ?: false,
                        leaderboardVisible = document.getBoolean("leaderboardVisible") ?: false,
                        photosVisible = document.getBoolean("photosVisible") ?: false,
                        shareLocationVisible = document.getBoolean("shareLocationVisible") ?: false,
                        watcherVisible = document.getBoolean("watcherVisible") ?: false,

                    )
                    // Set the fetched user as the current user
                    setCurrentUser(user)
                    // Invoke the callback with the fetched user
                    callback(user)
                } else {
                    // If no document was found, invoke the callback with null
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                // If the fetch operation fails, invoke the callback with null
                callback(null)
            }
    }
}