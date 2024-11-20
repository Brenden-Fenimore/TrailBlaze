package com.example.trailblaze.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.*


data class Message(
    val id: String? = null,
    val senderId: String = "",
    val recipientId: String = " ",
    val text: String = " ",
    val imageUrl: String? = null,
    val timestamp: Long = 0L)


class MessagingActivity : AppCompatActivity() {
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var attachImageButton: ImageButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var chevronLeft: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var friendNameTextView: TextView
    private lateinit var friendProfileImage: ImageView

    // Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var selectedFriendId: String? = null
    private val messagesList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        friendProfileImage = findViewById(R.id.friendProfileImage)
        // Get intent extras
        selectedFriendId = intent.getStringExtra("selectedFriendId")
        val selectedFriendName = intent.getStringExtra("selectedFriendName")
        val selectedFriendProfileImage = intent.getStringExtra("selectedFriendProfileImage")

        Glide.with(this)
            .load(selectedFriendProfileImage)
            .circleCrop()
            .into(friendProfileImage)

        // Initialize views
        chevronLeft = findViewById(R.id.chevron_left)
        attachImageButton = findViewById(R.id.attachImageButton)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageEditText = findViewById(R.id.messageEditText)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        friendNameTextView = findViewById(R.id.friendNameTextView)

        // Set friend name
        friendNameTextView.text = selectedFriendName

        // Setup RecyclerView
        getCurrentUserProfileUrl { currentUserProfileUrl ->
            messageAdapter = MessageAdapter(
                currentUserProfileUrl = currentUserProfileUrl,
                friendProfileUrl = selectedFriendProfileImage,
                onMessageClick = { message ->
                    // handle message click
                }
            )
            messagesRecyclerView.layoutManager = LinearLayoutManager(this)
            messagesRecyclerView.adapter = messageAdapter

            // Fetch messages after adapter is ready
            fetchMessages()
        }

        // Set up button listeners
        chevronLeft.setOnClickListener { finish() }
        attachImageButton.setOnClickListener { attachPhoto() }
        sendMessageButton.setOnClickListener { sendMessage() }
    }

    private fun attachPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId == null || selectedFriendId == null) {
            Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show()
            return
        }

        val message = Message(
            senderId = currentUserId,
            recipientId = selectedFriendId!!,
            text = messageText,
            timestamp = System.currentTimeMillis(),
            imageUrl = null
        )

        // Add message to local list and update UI immediately
        messagesList.add(message)
        messageAdapter.submitList(messagesList.toList())
        messagesRecyclerView.scrollToPosition(messagesList.size - 1)
        messageEditText.text.clear()

        // Save to Firestore and send FCM notification
        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Send FCM notification
                firestore.collection("users").document(selectedFriendId!!)
                    .get()
                    .addOnSuccessListener { document ->
                        val recipientToken = document.getString("fcmToken")

                        if (recipientToken != null) {
                            // Create message payload
                            val fcmMessage = RemoteMessage.Builder(recipientToken)
                                .setMessageId(UUID.randomUUID().toString())
                                .setData(mapOf(
                                    "title" to "New Message",
                                    "body" to messageText,
                                    "type" to "chat"
                                ))
                                .build()

                            // Send message using FCM
                            FirebaseMessaging.getInstance().send(fcmMessage)
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("MessagingActivity", "Failed to send message", exception)
                messagesList.remove(message)
                messageAdapter.submitList(messagesList.toList())
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                uploadImage(imageUri)
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        Toast.makeText(this, "Uploading image... (not yet implemented)", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_IMAGE = 100
    }

    private fun fetchMessages() {
        if (currentUserId == null || selectedFriendId == null) {
            Log.e("MessagingActivity", "User ID or Friend ID is null")
            return
        }

        firestore.collection("messages")
            .whereIn("senderId", listOf(currentUserId, selectedFriendId))
            .whereIn("recipientId", listOf(currentUserId, selectedFriendId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessagingActivity", "Error loading messages", error)
                    return@addSnapshotListener
                }

                val newMessages = mutableListOf<Message>()
                snapshot?.documents?.forEach { document ->
                    document.toObject(Message::class.java)?.let { message ->
                        newMessages.add(message)
                    }
                }
                messagesList.clear()
                messagesList.addAll(newMessages)
                messageAdapter.submitList(messagesList.toList())
                messagesRecyclerView.scrollToPosition(messagesList.size - 1)
            }
    }
    private fun getCurrentUserProfileUrl(callback: (String?) -> Unit) {
        currentUserId?.let { userId ->
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val profileUrl = document.getString("profileImageUrl")
                    callback(profileUrl)
                }
        }
    }
}