package com.example.trailblaze.ui.profile

import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.R
import com.google.android.play.core.integrity.v

class MessageSearchActivity : AppCompatActivity() {

private lateinit var searchInput: AutoCompleteTextView
private lateinit var backButton: ImageButton
private lateinit var searchButton: ImageButton
private lateinit var friendsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_search)

        searchInput = findViewById(R.id.recipientSearch)
        backButton = findViewById(R.id.backButton)
        searchButton = findViewById(R.id.searchIcon)
        friendsRecyclerView = findViewById(R.id.searchFriendsRecycler)

        searchInput.requestFocus()

        backButton.setOnClickListener {
            onBackPressed()
        }

        searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if (searchTerm.isBlank() || searchTerm.length < 3) {
                Log.e("MessageSearchActivity", "Invalid username")
                return@setOnClickListener
            }else{
            setupSearchRecyclerView(searchTerm)
            }
        }

    }

    private fun setupSearchRecyclerView(searchTerm: String) {}
}