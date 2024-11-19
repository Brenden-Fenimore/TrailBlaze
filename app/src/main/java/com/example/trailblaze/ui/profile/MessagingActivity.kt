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


data class Message(
    val id: String? = null,
    val senderId: String = "",
    val recipientId: String = " ",
    val text: String = " ",
    val imageUrl: String? = null,
    val timestamp: Long = 0L)


class MessagingActivity : Fragment() {

        // Initialize UI components

   // private lateinit var recipientSearch: AutoCompleteTextView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var attachImageButton: ImageButton
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter


    // Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var selectedFriendId: String? = null
    private val messagesList = mutableListOf<Message>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_messaging, container, false)

        // Initialize views
        backButton = view.findViewById(R.id.backButton)
        attachImageButton = view.findViewById(R.id.attachImageButton)
        sendMessageButton = view.findViewById(R.id.sendMessageButton)
        messageEditText = view.findViewById(R.id.messageEditText)
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView)

            // Setup RecyclerView
        messageAdapter = MessageAdapter{message ->
            // handle message click, e.g., show details or forward message
        }
        messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        messagesRecyclerView.adapter = messageAdapter


        // Get selected friend ID (passed from previous screen via arguments)
        selectedFriendId = arguments?.getString("selectedFriendId")

        // Populate messages
        fetchMessages()

        // Set up button listeners
        backButton.setOnClickListener { goBack() }
        attachImageButton.setOnClickListener { attachPhoto() }
        sendMessageButton.setOnClickListener { sendMessage() }

        return view


    }
    private fun fetchMessages() {
        if (currentUserId == null || selectedFriendId == null) {
            Log.e("MessagingFragment", "User ID or Friend ID is null")
            return
        }

        firestore.collection("messages")
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("receiverId", selectedFriendId)
            .get()
            .addOnSuccessListener { documents ->
                messagesList.clear()
                for (document in documents) {
                    val message = document.toObject(Message::class.java)
                    messagesList.add(message)
                }
                messageAdapter.submitList(messagesList)
            }
            .addOnFailureListener { exception ->
                Log.e("MessagingFragment", "Error loading messages", exception)
            }
    }

    private fun goBack() {
        requireActivity().onBackPressed()
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
            Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId == null || selectedFriendId == null) {
            Toast.makeText(requireContext(), "Unable to send message", Toast.LENGTH_SHORT).show()
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
                messageEditText.text.clear()
                messagesList.add(message)
                messageAdapter.notifyItemInserted(messagesList.size - 1)
                messagesRecyclerView.scrollToPosition(messagesList.size - 1)
            }
            .addOnFailureListener { exception ->
                Log.e("MessagingFragment", "Failed to send message", exception)
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
                Toast.makeText(requireContext(), "Failed to get image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        // Logic to upload image to Firebase Storage and send the image message
        Toast.makeText(requireContext(), "Uploading image... (not yet implemented)", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_IMAGE = 100
    }
}