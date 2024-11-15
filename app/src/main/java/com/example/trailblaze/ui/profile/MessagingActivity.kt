package com.example.trailblaze.ui.profile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.trailblaze.R


data class Message(val sendUserId: String, val text: String, val timestamp: Long)


class MessagingActivity : Fragment() {

    companion object {
        fun newInstance() = MessagingActivity()
    }

    private val viewModel: MessagingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_messaging, container, false)
    }
}