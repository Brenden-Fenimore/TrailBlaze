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

    private var friendsList: ArrayList<Friends> = arrayListOf()

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

     //   searchInput.requestFocus()

        // Set the layout manager for the RecyclerView
        friendsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup the adapter for the RecyclerView and provide an empty list initially
        friendAdapter = FriendAdapter(emptyList()) { user ->
            openMessagingFragment(user.userId, user.username)
        }

        // Set the adapter to the RecyclerView
        friendsRecyclerView.adapter = friendAdapter

        // Back button listener
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Search button listener
        searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if (searchTerm.isEmpty() || searchTerm.length < 3) {
                Log.e("MessageSearchActivity", "Invalid username")
            }else{
            filterFriendsList(searchTerm)
            }
        }


        // Fetch users from Firestore after setting up RecyclerView
        fetchFriends()

    }

    private fun fetchFriends() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users")
            .whereArrayContains("friends", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                friendsList = documents.mapNotNull { document ->
                    val userId = document.id
                    val username = document.getString("username")
                    val profileImageUrl = document.getString("profileImageUrl")
                    val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false

                    if (username != null) {
                        Friends(userId, username, profileImageUrl, isPrivateAccount, watcherVisible = true)
                    } else {
                        null
                    }
                } as ArrayList<Friends>
                friendAdapter.updateUserList(friendsList)
            }
            .addOnFailureListener { exception ->
                Log.e("MessageSearchActivity", "Error fetching friends", exception)
            }
    }

    private fun filterFriendsList(searchTerm: String) {
        val filteredList = friendsList.filter {
            it.username.contains(searchTerm, ignoreCase = true)
        }
        friendAdapter.updateUserList(filteredList)
    }

    private fun openMessagingFragment(friendUserId: String, friendUsername: String) {
        val messagingFragment = MessagingActivity().apply {
            arguments = Bundle().apply {
                putString("recipientId", friendUserId)
                putString("recipientName", friendUsername)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, messagingFragment) // Replace `fragmentContainer` with the correct container ID
            .addToBackStack(null) // Optional: Add to back stack for navigation
            .commit()
    }
}