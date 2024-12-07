package com.byteforce.trailblaze.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class Friend(val id: String, val name: String)
data class InboxMessage(
    val id: String,
    val friendId: String,
    val friendName: String,
    val friendProfileImage: String?,
    val lastMessage: String,
    val timestamp: Long
)
class MessagingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val _messages = MutableLiveData<List<InboxMessage>>()
    val messages: LiveData<List<InboxMessage>> = _messages

    init {
        fetchInboxMessages()
    }

    private fun fetchInboxMessages() {
        currentUserId?.let { userId ->
            firestore.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    val inboxMessages = mutableListOf<InboxMessage>()
                    val processedFriends = mutableSetOf<String>()

                    snapshot?.documents?.forEach { doc ->
                        val message = doc.toObject(Message::class.java)
                        message?.let {
                            // Check if user is part of this conversation
                            if (it.senderId == userId || it.recipientId == userId) {
                                // Get the other person's ID
                                val friendId = if (it.senderId == userId) it.recipientId else it.senderId

                                if (!processedFriends.contains(friendId)) {
                                    processedFriends.add(friendId)
                                    firestore.collection("users").document(friendId)
                                        .get()
                                        .addOnSuccessListener { userDoc ->
                                            val inboxMessage = InboxMessage(
                                                id = doc.id,
                                                friendId = friendId,
                                                friendName = userDoc.getString("username") ?: "",
                                                friendProfileImage = userDoc.getString("profileImageUrl"),
                                                lastMessage = it.text,
                                                timestamp = it.timestamp
                                            )
                                            inboxMessages.add(inboxMessage)
                                            _messages.value = inboxMessages.sortedByDescending { it.timestamp }
                                        }
                                }
                            }
                        }
                    }
                }
        }
    }
}