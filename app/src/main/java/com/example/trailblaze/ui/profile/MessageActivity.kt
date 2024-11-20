package com.example.trailblaze.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


data class Message(
    val id: String? = null,
    val senderId: String = "",
    val recipientId: String = " ",
    val text: String = " ",
    val imageUrl: String? = null,
    val timestamp: Long = 0L)


class MessageActivity: AppCompatActivity (){

    // Initialize UI components

    // private lateinit var recipientSearch: AutoCompleteTextView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var attachImageButton: ImageButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter


    // Firebase
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var selectedFriendId: String? = null
    private val messagesList = mutableListOf<Message>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        // Retrieve the selected friend's ID and name
        selectedFriendId = intent.getStringExtra("friendId")
        val friendName = intent.getStringExtra("friendName") ?: "Unknown Friend"


        // Initialize views
        backButton = findViewById(R.id.backButton)
        attachImageButton = findViewById(R.id.attachImageButton)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageEditText = findViewById(R.id.messageInput)
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)

        // Setup RecyclerView
        messageAdapter = MessageAdapter(mutableListOf()){ message ->
            Toast.makeText(this, "Message clicked $message", Toast.LENGTH_LONG).show()
        }
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = messageAdapter


        // Populate messages
       fetchMessages()

        // Set up button listeners
       backButton.setOnClickListener {
           onBackPressed()
       }

        sendMessageButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if(message.isNotEmpty()) {
                sendMessage(message)
                messageEditText.text.clear()
            }
            }
    }

    private fun fetchMessages() {
        if (currentUserId == null || selectedFriendId == null) {
            Log.e("MessagingActivity", "User ID or Friend ID is null")
            return
        }

        firestore.collection("messages")
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("receiverId", selectedFriendId)
            .get()
            .addOnSuccessListener { documents ->
                val fetchedMessages = mutableListOf<Message>()
                for (document in documents) {
                    val message = document.toObject(Message::class.java)
                    fetchedMessages.add(message)
                }
                // Update the adapter with the new list
                messageAdapter.submitList(fetchedMessages)
            }
            .addOnFailureListener { exception ->
                Log.e("MessagingActivity", "Error loading messages", exception)
            }
    }

    private fun attachPhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_IMAGE)
    }

    private fun sendMessage(messageText: String) {
       // val messageText = messageEditText.text.toString().trim()
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
            imageUrl = null // For now, this is null
        )

        // Save message to Firestore
        firestore.collection("messages")
            .add(message)
            .addOnSuccessListener {
                messagesList.add(message)
                messageAdapter.notifyItemInserted(messagesList.size - 1)
                messagesRecyclerView.scrollToPosition(messagesList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("MessagingActivity", "Failed to send message", exception)
            }
    }

    @Deprecated("Deprecated in Java")
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
        if(currentUserId == null || selectedFriendId == null) {
            Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRef = storage.reference.child("images/${selectedFriendId}/${uri.lastPathSegment}")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnFailureListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val message = Message(
                    senderId = currentUserId,
                    recipientId = selectedFriendId!!,
                    text = "",
                    imageUrl = downloadUri.toString(),
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        messagesList.add(message)
                        messageAdapter.notifyItemInserted(messagesList.size - 1)
                        messagesRecyclerView.scrollToPosition(messagesList.size - 1)
                        Toast.makeText(this, "Image sent successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MessagingActivity", "Failure to send image", exception)
                    }
            }
        }.addOnFailureListener{ exception ->
            Log.e("MessagingActivity", "Failure to upload image", exception)
        }
    }

    companion object {
        private const val REQUEST_IMAGE = 100
    }
}
