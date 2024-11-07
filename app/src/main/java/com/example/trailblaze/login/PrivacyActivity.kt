package com.example.trailblaze.login

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import java.io.BufferedReader
import java.io.InputStreamReader

class PrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        supportActionBar?.hide()

// Reference and read terms_cons_policy.txt
        val privacyTextView = findViewById<TextView>(R.id.privacyTextView)
        val privacyContent = readTextFileFromRaw()
        privacyTextView.text = privacyContent

        //find ok button id
        val privacyOkButton = findViewById<Button>(R.id.privacyOkBtn)

        //set click listener for ok button and close page
        privacyOkButton.setOnClickListener{
            finish()
        }

    }
    private fun readTextFileFromRaw(): String {
        val inputStream = resources.openRawResource(R.raw.privacy_policy)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = StringBuilder()
        reader.forEachLine {line->
            content.append(line).append("\n")
        }
        inputStream.close()
        return content.toString()
    }
}