package com.byteforce.trailblaze.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.byteforce.trailblaze.R
import java.io.BufferedReader
import java.io.InputStreamReader

class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)
        supportActionBar?.hide()

// Reference and read terms_cons_policy.txt
        val termsTextView = findViewById<TextView>(R.id.termsTextView)
        val termsContent = readTextFileFromRaw()
        termsTextView.text = termsContent

        //find ok button id
        val okButton = findViewById<Button>(R.id.termsOKbtn)
        val acceptableUseButton = findViewById<Button>(R.id.acceptableUseBtn)
        val privacyPolicyButton = findViewById<Button>(R.id.privacyPolicyBtn)

        //set click listener for ok button and close page
        okButton.setOnClickListener{
            finish()
        }

        //set click listener for acceptableUseBtn
        acceptableUseButton.setOnClickListener{
            // open Acceptable Use Policy
            val intent = Intent(this, AcceptableActivity::class.java)
            startActivity(intent)
        }

        //set click listener for privacyPolicyBtn
        privacyPolicyButton.setOnClickListener{
            // open Privacy Policy
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
        }
    }
   private fun readTextFileFromRaw(): String {
       val inputStream = resources.openRawResource(R.raw.terms_cons_policy)
       val reader = BufferedReader(InputStreamReader(inputStream))
       val content = StringBuilder()
       reader.forEachLine {line->
           content.append(line).append("\n")
       }
       inputStream.close()
       return content.toString()
   }
}
