package com.example.trailblaze.watcherFeature

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trailblaze.ui.profile.Friends
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.example.trailblaze.ui.parks.AddFriend

data class WatcherMember(
    val userId: String = "",
    val username: String = "",
    val profileImageUrl: String? = "",
    val watcherVisible: Boolean
)

class WatcherMemberViewModel: ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _watcherMembers = MutableLiveData<List<Friends>>()
    val watcherMembers: LiveData<List<Friends>> get() = _watcherMembers

private val _selectedWatchers = MutableLiveData<MutableList<Friends>>()
    val selectedWatchers: LiveData<MutableList<Friends>> get() = _selectedWatchers

    init{
        _selectedWatchers.value = mutableListOf()
        fetchWatcherMembers()
    }


     @SuppressLint("SuspiciousIndentation")
     fun fetchWatcherMembers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firestore.collection("users").get()
            .addOnSuccessListener {documents->
                val watcherList = documents.mapNotNull{ document ->
                val userId = document.id
                val username = document.getString("username")
                val profileImageUrl = document.getString("profileImageUrl")
                val isPrivateAccount = document.getBoolean("isPrivateAccount") ?: false
                val watcherVisible = document.getBoolean("watcherVisible") ?: false

                    // Verify data fetched correctly
                    Log.d("WatcherMemberViewModel", "Fetched UserId: $userId, Username: $username, WatcherVisible: $watcherVisible")

                    // Add visible friends who are not the current user
                    if(username != null && userId != currentUserId && watcherVisible){
                        Friends(userId, username, profileImageUrl, isPrivateAccount, watcherVisible)
                    }else {
                        null
                    }
                    }
                _watcherMembers.value = watcherList
            }
            .addOnFailureListener {exception->
                Log.e("WatcherMemberViewModel", "Error fetching watcher members: ", exception)
            }
    }

    fun addWatcher(watcher: Friends) {
        _selectedWatchers.value?.let {
            if(!it.contains(watcher)){
                it.add(watcher)
                _selectedWatchers.value = it
                Log.d("WatcherMemberViewModel", "Added watcher ${watcher.username}")
            }
        }
    }
    fun removeWatcher(watcher: Friends) {
        _selectedWatchers.value?.let {
            if(!it.contains(watcher)){
                it.remove(watcher)
                _selectedWatchers.value = it
                Log.d("WatcherMemberViewModel", "Removed watcher ${watcher.username}")
            }
        }
    }
}