package com.example.trailblaze.watcherFeature

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Switch
import android.widget.ImageView
import android.widget.EditText
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.example.trailblaze.R

class EditWatcherProfile : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var nameField: EditText
    private lateinit var nicknameField: EditText
    private lateinit var phoneNumberField: EditText
    private lateinit var emailField: EditText
    private lateinit var trailBlazeUserCheckbox: CheckBox
    private lateinit var trailBlazeUsernameField: EditText
    private lateinit var receiveMessagesSwitch: Switch
    private lateinit var receiveEmailSwitch: Switch
    private lateinit var emergencyNotificationsSwitch: Switch
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_edit_watcher_profile)

        // Initialize views
        profilePicture = findViewById(R.id.profilePicture)
        nameField = findViewById(R.id.watcherName)
        nicknameField = findViewById(R.id.nicknameLabel)
        phoneNumberField = findViewById(R.id.edit_phone)
        emailField = findViewById(R.id.edit_email)
        trailBlazeUserCheckbox = findViewById(R.id.isTrailBlazeUser)
        trailBlazeUsernameField = findViewById(R.id.usernameLabel) // Make sure this ID exists
        receiveMessagesSwitch = findViewById(R.id.receiveMessagesSwitch)
        receiveEmailSwitch = findViewById(R.id.receiveEmailSwitch)
        emergencyNotificationsSwitch = findViewById(R.id.emergencyNotificationsSwitch)
        updateButton = findViewById(R.id.saveWatcherProfile)

        // Set up listeners
        setupListeners()
    }

    private fun setupListeners() {
        // Listener for TrailBlaze user checkbox
        trailBlazeUserCheckbox.setOnCheckedChangeListener { _, isChecked ->
            trailBlazeUsernameField.isEnabled = isChecked
            if (!isChecked) {
                trailBlazeUsernameField.text.clear() // Clear if checkbox is deselected
            }
        }

        // Update button listener
        updateButton.setOnClickListener {
            saveWatcherProfile()
        }
    }
    private fun saveWatcherProfile() {
        val name = nameField.text.toString()
        val nickname = nicknameField.text.toString()
        val phoneNumber = phoneNumberField.text.toString()
        val email = emailField.text.toString()
        val isTrailBlazeUser = trailBlazeUserCheckbox.isChecked
        val trailBlazeUsername = trailBlazeUsernameField.text.toString()
        val receiveMessages = receiveMessagesSwitch.isChecked
        val receiveEmail = receiveEmailSwitch.isChecked
        val emergencyNotifications = emergencyNotificationsSwitch.isChecked

        // Check if name and one contact method (phone or email) are provided
        if (name.isBlank()) {
            nameField.error = "Name is required"
            return
        }
        if (phoneNumber.isBlank() && email.isBlank()) {
            phoneNumberField.error = "Provide at least one contact (phone or email)"
            emailField.error = "Provide at least one contact (phone or email)"
            return
        }

        // Save data (for example, into a database or send to backend)
        // Replace this with actual data handling logic, such as Room database or API call
        // You could use a data model to encapsulate the watcher information, like WatcherModel(name, ...)

        // Show success message or navigate back
        // Toast.makeText(this, "Watcher profile saved", Toast.LENGTH_SHORT).show()
}
}