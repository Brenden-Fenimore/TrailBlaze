package com.example.trailblaze.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.android.play.core.integrity.v
import com.example.trailblaze.ui.profile.FriendAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageSearchActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

private lateinit var searchInput: AutoCompleteTextView
private lateinit var backButton: ImageButton
private lateinit var searchButton: ImageButton
private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendAdapter: FriendAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_search)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize Views
        searchInput = findViewById(R.id.recipientSearch)
        backButton = findViewById(R.id.backButton)
        searchButton = findViewById(R.id.searchIcon)
        friendsRecyclerView = findViewById(R.id.searchFriendsRecycler)

        searchInput.requestFocus()

        backButton.setOnClickListener {
            onBackPressed()
        }

        searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if (searchTerm.isEmpty() || searchTerm.length < 3) {
                Log.e("MessageSearchActivity", "Invalid username")
                return@setOnClickListener
            }else{
            setupSearchRecyclerView(searchTerm)
            }
        }
// Set the layout manager for the RecyclerView
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup the adapter for the RecyclerView and provide an empty list initially
        friendAdapter = FriendAdapter(emptyList()) { user ->
            // Handle user click
            val intent = Intent(this, FriendsProfileActivity::class.java)
            intent.putExtra("friendUserId", user.userId)
            startActivity(intent)
        }

        // Set the adapter to the RecyclerView
        friendsRecyclerView.adapter = friendAdapter

        // Fetch users from Firestore after setting up RecyclerView
        fetchUsers()

    }

    private fun fetchUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

         var friendsList: List<Friends>

        firestore.collection("users").get()
            .addOnSuccessListener { documents ->
                friendsList = documents.mapNotNull { document ->
                    val userId = document.id
                    val username = document.getString("username")
                    val profileImageUrl = document.getString("profileImageUrl")
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false
                    val watcherVisible = document.getBoolean("watcherVisible") ?: false

                    // Log the values fetched to check if they are retrieving correctly
                    Log.d("FetchUsers", "UserId: $userId, Username: $username, IsPrivateAccount: $isPrivateAccount, WatcherVisible: $watcherVisible")

                    // Check for null username and ensure the user is not the current user and is visible
                    if (username != null && userId != currentUserId && watcherVisible) {
                        Friends(userId, username, profileImageUrl, isPrivateAccount, watcherVisible)
                    } else {
                        null
                    }
                }
                // Update the adapter with the filtered user list
                friendAdapter.updateUserList(friendsList)
            }
            .addOnFailureListener { exception ->
                Log.e("UserListActivity", "Error fetching users: ", exception)
            }
    }

    private fun setupSearchRecyclerView(searchTerm: String) {

    }
}