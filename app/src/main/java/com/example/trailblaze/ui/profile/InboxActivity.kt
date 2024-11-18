package com.example.trailblaze.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.MainActivity
import com.example.trailblaze.databinding.ActivityInboxBinding


class InboxActivity : AppCompatActivity() {

  private lateinit var binding: ActivityInboxBinding
    private lateinit var adapter: MessageAdapter
    private val messagingViewModel: MessagingViewModel by viewModels()



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeMessages()

        binding.createMessage.setOnClickListener{
            startActivity(Intent(this, MessagingActivity::class.java))
        }

binding.backButton.setOnClickListener { onBackPressed() }

    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter { message ->
            val intent = Intent(this, MessagingActivity::class.java).apply {
                putExtra("MESSAGE_ID", message.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewInbox.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewInbox.adapter = adapter

    }

    private fun observeMessages() {
        messagingViewModel.messages.observe(this) { messageList ->
            adapter.submitList(messageList)
        }
    }


}