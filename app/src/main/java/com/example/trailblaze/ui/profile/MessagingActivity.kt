package com.example.trailblaze.ui.profile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R


data class Message(val sendUserId: String, val text: String, val timestamp: Long)


class MessagingActivity : AppCompatActivity() {

//    companion object {
//        fun newInstance() = MessagingActivity()
//    }
    private lateinit var adapter: MessageAdapter
    private lateinit var viewModel: MessagingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_messaging)
        // TODO: Use the ViewModel
        viewModel = ViewModelProvider(this).get(MessagingViewModel::class.java)

        adapter = MessageAdapter()
        val messagesRecyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter

        viewModel.messages.observe(this){ messages ->
            adapter.submitList(messages)
            messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        findViewById<Button>(R.id.sendMessageButton).setOnClickListener {
            val messageContent = messageEditText.text.toString()
            if(messageContent.isNotEmpty()){
                viewModel.sendMessage(messageContent)
                messageEditText.text.clear()
            }
        }
    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        return inflater.inflate(R.layout.fragment_messaging, container, false)
//    }
}