package com.example.trailblaze.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class MessagingViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private val _message = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _message

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    private fun loadMessages(){

        firestore.collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if(error != null || snapshot == null){
                    Log.e("MessagingViewModel", "Error loading messages", error)
                    return@addSnapshotListener
                }
                val messagesList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                }
                _message.value = messagesList
            }

    }
    fun sendMessage(message: String){
        val sendUserId = auth.currentUser?.uid ?: return
        val newMessage = Message(message, sendUserId, System.currentTimeMillis())
        firestore.collection("messages").add(message)
        .addOnSuccessListener {
            Log.d("MessagingViewModel", "Message sent successfully")
        }
            .addOnFailureListener {e->
                Log.e("MessagingViewModel", "Error sending message", e)
            }
    }
}