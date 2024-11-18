package com.example.trailblaze.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

data class Friend(val id: String, val name: String)

class MessagingViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private val _message = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _message

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _uploadImageStatus = MutableLiveData<String?>()
    val uploadImageStatus: LiveData<String?> get() = _uploadImageStatus

    private val _sendMessageStatus = MutableLiveData<Boolean>()
    val sendMessageStatus: LiveData<Boolean> get() = _sendMessageStatus

    private val _friends = MutableLiveData<List<Friend>>()
    val friends: LiveData<List<Friend>> get() = _friends

    init {
        loadMessages()
    }

    private fun loadMessages() {

        val userId = auth.currentUser?.uid ?: return

        firestore.collection("messages")
            .whereEqualTo("recipient_id", userId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("MessagingViewModel", "Error loading messages", error)
                    return@addSnapshotListener
                }
                val messagesList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _message.value = messagesList
            }

    }
    // Fetch friends for recipient selection
    fun fetchFriends(): LiveData<List<Friend>> {
        val userId = auth.currentUser?.uid ?: return MutableLiveData(emptyList())
        firestore.collection("friends")
            .whereEqualTo("ownerId", userId) // Ensure you're fetching the user's friends
            .get()
            .addOnSuccessListener { snapshot ->
                val friendsList = snapshot.documents.mapNotNull { doc ->
                    val friendId = doc.id
                    val friendName = doc.getString("name") ?: return@mapNotNull null
                    Friend(id = friendId, name = friendName)
                }
                _friends.value = friendsList
            }
            .addOnFailureListener { error ->
                Log.e("MessagingViewModel", "Error fetching friends: ${error.message}", error)
            }
        return _friends
    }

    fun sendMessage(
        recipientId: String,
        message: String,
        imageUri: Uri? = null
    ) {
        val senderId = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()

        if (imageUri != null) {
            uploadImage(imageUri){
                imageUrl->
                if(imageUrl != null){
            saveMessage(senderId, recipientId, message, imageUrl)
        } else {
            Log.e("MessagingViewModel", "Error uploading image")
        }
    }
}else{
    saveMessage(senderId, recipientId, message, null)
}

}

private fun uploadImage(imageUri: Uri, onComplete: (String?) -> Unit){
    val storageRef = storage.reference.child("message_images/${UUID.randomUUID()}.jpg")

    storageRef.putFile(imageUri)
        .addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri->
                onComplete(uri.toString())
            }
        }
        .addOnFailureListener{error ->
            Log.e("MessageViewModel", "Error uploading image: ${error.message}", error)
            onComplete(null)
        }
}
    private fun saveMessage(senderId: String, recipientId: String, text: String, imageUrl: String?) {
        val message = Message(
            senderId = senderId,
            recipientId = recipientId,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("messages").add(message)
            .addOnSuccessListener{
                Log.e("MessageViewModel", "Message sent successfully")
                _sendMessageStatus.value = true
            }
            .addOnFailureListener { error ->
                Log.e("MessageViewModel", "Error sending message: ${error.message}", error)
                _sendMessageStatus.value = false
            }
    }
}