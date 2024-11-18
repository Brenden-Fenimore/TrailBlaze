package com.example.trailblaze.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import androidx.lifecycle.Observer


data class Message(
    val id: String? = null,
    val senderId: String = "",
    val recipientId: String = " ",
    val text: String = " ",
    val imageUrl: String? = null,
    val timestamp: Long = 0L)


class MessagingActivity : AppCompatActivity() {

    private val viewModel: MessagingViewModel by viewModels()

    private lateinit var recipientSearch: AutoCompleteTextView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button
    private lateinit var attachImageButton: ImageButton
    private lateinit var messageRecyclerView: RecyclerView

    private var selectedImageUri: Uri? = null
    private var selectedRecipientId: String? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Log.d("MessageActivity", "Image selected: $uri")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_messaging)

        // initialize the UI components
        recipientSearch = findViewById(R.id.recipientSearch)
        messageEditText = findViewById(R.id.messageEditText)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        attachImageButton = findViewById(R.id.attachImageButton)
        messageRecyclerView = findViewById(R.id.messagesRecyclerView)


        val adapter = MessageAdapter { message ->
            Log.d("MessagingActivity", "Message clicked: ${message.text}")
        }
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = adapter


        viewModel.messages.observe(this, Observer { messages ->
            adapter.submitList(messages)
        })

        // Set up recipient search
        setupRecipientSearch()

        // Click listeners

        attachImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        sendMessageButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if(message.isBlank() && selectedImageUri == null) {
                return@setOnClickListener
            }
            if(selectedRecipientId.isNullOrBlank()){
                Log.e("MessagingActivity", "Recipient ID is blank")
                return@setOnClickListener
            }

            // send message to the ViewModel

            viewModel.sendMessage(
                recipientId = selectedRecipientId!!,
                message = message,
                imageUri = selectedImageUri)

            // clear fields
            messageEditText.text.clear()
            selectedImageUri = null
        }

    }
    private fun setupRecipientSearch() {
        viewModel.fetchFriends().observe(this, Observer {friends ->
            val friendNames = friends.map{it.name}
            val friendIds = friends.map {it.id}

            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line,friendNames)
            recipientSearch.setAdapter(adapter)

            recipientSearch.setOnItemClickListener{ _, _, position, _ ->
                selectedRecipientId = friendIds[position]
                Log.d("MessagingActivity", "Recipient ID selected: $selectedRecipientId")
            }
        })
    }

}