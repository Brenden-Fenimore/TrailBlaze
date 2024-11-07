package com.example.trailblaze.login
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R
import java.io.BufferedReader
import java.io.InputStreamReader

class AcceptableActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceptable)
        supportActionBar?.hide()

// Reference and read terms_cons_policy.txt
        val acceptableTextView = findViewById<TextView>(R.id.acceptableTextView)
        val acceptableContent = readTextFileFromRaw()
        acceptableTextView.text = acceptableContent

        //find ok button id
        val acceptableOkButton = findViewById<Button>(R.id.acceptableOkBtn)

        //set click listener for ok button and close page
        acceptableOkButton.setOnClickListener{
            finish()
        }

    }
    private fun readTextFileFromRaw(): String {
        val inputStream = resources.openRawResource(R.raw.acceptable_use_policy)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = StringBuilder()
        reader.forEachLine {line->
            content.append(line).append("\n")
        }
        inputStream.close()
        return content.toString()
    }
}