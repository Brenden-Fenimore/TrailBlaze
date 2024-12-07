package com.byteforce.trailblaze.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.byteforce.trailblaze.databinding.ActivityInboxBinding


class InboxActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInboxBinding
    private lateinit var adapter: InboxAdapter
    private val messagingViewModel: MessagingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeMessages()

        binding.createMessage.setOnClickListener {
            startActivity(Intent(this, MessageSearchActivity::class.java))
        }

        binding.backButton.setOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = InboxAdapter { friendId, friendName, friendProfileImage ->
            val intent = Intent(this, MessagingActivity::class.java).apply {
                putExtra("selectedFriendId", friendId)
                putExtra("selectedFriendName", friendName)
                putExtra("selectedFriendProfileImage", friendProfileImage)
            }
            startActivity(intent)
        }
        binding.recyclerViewInbox.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewInbox.adapter = adapter
    }

    private fun observeMessages() {
        messagingViewModel.messages.observe(this) { messages ->
            val conversations = messages.map { message ->
                InboxAdapter.InboxConversation(
                    friendId = message.friendId,
                    friendName = message.friendName,
                    friendProfileImage = message.friendProfileImage,
                    lastMessage = message.lastMessage,
                    timestamp = message.timestamp
                )
            }
            adapter.submitList(conversations)
        }
    }
}