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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
        friendsRecyclerView.isNestedScrollingEnabled = true

        // Setup the adapter for the RecyclerView and provide an empty list initially
        friendAdapter = FriendAdapter(emptyList()) { friend ->
            openMessagingFragment(friend)
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
        fetchUserFriends()

    }

    private fun fetchUserFriends() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friendsIds = document.get("friends") as? List<String> ?: emptyList()
                        loadFriendsData(friendsIds)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching friends: ", e)
                }
        }
    }
    private fun loadFriendsData(friendIds: List<String>) {
        val tasks = mutableListOf<Task<DocumentSnapshot>>()

        for (friendId in friendIds) {
            tasks.add(firestore.collection("users").document(friendId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val friend = Friends(
                        userId  = friendId,
                        username = document.getString("username") ?: "Unknown",
                        profileImageUrl = document.getString("profileImageUrl"),
                        isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false,
                        watcherVisible = document.getBoolean("watcherVisible") ?: false
                    )
                    friendsList.add(friend) // Add friend to the list
                }
            })
        }

        // Wait until all friend data is fetched
        Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener {
            // Update the RecyclerView with the fetched friends
            friendAdapter.updateUserList(friendsList)
        }
    }

    private fun filterFriendsList(searchTerm: String) {
        val filteredList = friendsList.filter {
            it.username.contains(searchTerm, ignoreCase = true)
        }
        friendAdapter.updateUserList(filteredList)
    }

    private fun openMessagingFragment(friend: Friends) {
        val intent = Intent(this, MessagingActivity::class.java).apply {
            putExtra("selectedFriendId", friend.userId)
            putExtra("selectedFriendName", friend.username)
            putExtra("selectedFriendProfileImage", friend.profileImageUrl)
        }
        startActivity(intent)
    }
}