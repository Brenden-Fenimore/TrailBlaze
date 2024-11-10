package com.example.trailblaze.login

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.trailblaze.R

class PersonalizeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalize)
        // Hide the ActionBar
        supportActionBar?.hide()

        if(savedInstanceState == null) {
            loadFragment(FirstPersonalizeFragment())
        }

        //set the listener for the back button
        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    //fucntion to load new fragments
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.termsBodyScroll, fragment)
            .addToBackStack(null)
            .commit()
    }
}


