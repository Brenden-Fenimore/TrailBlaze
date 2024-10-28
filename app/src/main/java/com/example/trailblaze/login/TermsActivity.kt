package com.example.trailblaze.login

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.trailblaze.R
import android.text.Html
import java.io.BufferedReader
import java.io.InputStreamReader

class TermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms)

// Load the HTML content from the raw resource
        val htmlText = loadHtmlFromRawResource(R.raw.terms_conditions_policy)

        // Reference the Terms and Conditions Body Textview
val termsTextView: TextView = findViewById(R.id.termsTextView)

        // Load the HTML content from TrailBlaze_termsConditionsPolicy.html
val htmlStream = resources.openRawResource(R.raw.terms_conditions_policy)
val htmlContent = htmlStream.bufferedReader().use {it.readText()}

        // Set the HTML Content to the Textview (convert to rich)
        termsTextView.text = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)

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
        }

        //set click listener for privacyPolicyBtn
        privacyPolicyButton.setOnClickListener{
            // open Privacy Policy
        }
    }
    private fun loadHtmlFromRawResource(resourceId: Int): String {
        val inputStream = resources.openRawResource(resourceId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.use { it.readText() }
    }
}
