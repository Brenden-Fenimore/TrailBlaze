package com.example.trailblaze.watcherFeature

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.trailblaze.R
import com.example.trailblaze.databinding.ActivityWatcherProfileBinding
import com.example.trailblaze.firestore.ImageLoader.loadProfilePicture
import com.example.trailblaze.firestore.UserManager
import com.example.trailblaze.firestore.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class WatcherProfile : AppCompatActivity() {

    private var firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var userManager: UserManager

    private lateinit var watcherName: TextView
    private lateinit var watcherProfilePicture: ImageView
    private lateinit var userId: String
    private lateinit var chevronLeftButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watcher_profile)

        // Initialize firebase
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(firestore)
        firestore = FirebaseFirestore.getInstance()
        userManager = UserManager


        // Initialize views
        watcherName = findViewById(R.id.watcherName)
        watcherProfilePicture = findViewById(R.id.watcherProfilePicture)
        chevronLeftButton = findViewById<ImageButton>(R.id.chevron_left)

        // Back button listener
        chevronLeftButton.setOnClickListener {
            // navigate to previous screen
            finish()
        }

        // fetch user
        userId = intent.getStringExtra("userId") ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if(userId.isNotEmpty()){
            loadUserProfile(userId)
        }
    }
    private fun loadUserProfile(userId: String) {
        // get user data from firestore
        firestore.collection("users").document(userId).get()
        .addOnSuccessListener { document->
            if(document.exists()){
                // extract user data
                val username = document.getString("username")?: "Unknown User"
                val profileImageUrl = document.getString("profile_image")

                    // set username
                watcherName.text = username

                // load profile picture
                profileImageUrl?.let {
                    loadProfilePicture(it)
                } ?: run {
                    watcherProfilePicture.setImageResource(R.drawable.account_circle)
                }
            }
        }
            .addOnFailureListener { exception ->}
    }

    private fun loadProfilePicture(imageUrl: String) {
        // fetch profile picture
        val storageRef: StorageReference = storage.getReferenceFromUrl(imageUrl)

        // use glide to load image
        Glide.with(this).load(imageUrl).into(watcherProfilePicture)
    }
}