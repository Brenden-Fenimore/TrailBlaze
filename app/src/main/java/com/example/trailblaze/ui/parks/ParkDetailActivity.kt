package com.example.trailblaze.ui.parks

import ImagesAdapter
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParkDetailActivity : AppCompatActivity() {

    private lateinit var parkCode: String // Default to -1 if not found
    private lateinit var parkNameTextView: TextView
    private lateinit var parkDescriptionTextView: TextView
    private lateinit var parkLatitudeTextView: TextView
    private lateinit var parkLongitudeTextView: TextView
    private lateinit var parkAddressTextView: TextView
    private lateinit var parkActivitiesTextView: TextView
    private lateinit var parkOperatingHoursTextView: TextView
    private lateinit var parkContactsPhoneTextView: TextView
    private lateinit var parkContactsEmailTextView: TextView
    private lateinit var parkWeatherInfoTextView: TextView
    private lateinit var parkEntrancePassesTextView: TextView
    private lateinit var parkImagesRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park_detail)

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        // Get the park code from the intent
        parkCode = intent.getStringExtra("PARK_CODE") ?: ""

        fetchParkDetails(parkCode)

        // Initialize views
        parkNameTextView = findViewById(R.id.parkNameTextView)
        parkDescriptionTextView = findViewById(R.id.parkDescriptionTextView)
        parkLatitudeTextView = findViewById(R.id.parkLatitudeTextView)
        parkLongitudeTextView = findViewById(R.id.parkLongitudeTextView)
        parkAddressTextView = findViewById(R.id.parkAddressTextView)
        parkActivitiesTextView = findViewById(R.id.parkActivitiesTextView)
        parkOperatingHoursTextView = findViewById(R.id.parkOperatingHoursTextView)
        parkContactsPhoneTextView = findViewById(R.id.parkContactsPhoneTextView)
        parkContactsEmailTextView = findViewById(R.id.parkContactsEmailTextView)
        parkWeatherInfoTextView = findViewById(R.id.parkWeatherInfoTextView)
        parkEntrancePassesTextView = findViewById(R.id.parkEntrancePassesTextView)

        // Initialize RecyclerView for images once at the beginning of onCreate()
        parkImagesRecyclerView = findViewById(R.id.parkImagesRecyclerView)
        parkImagesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fetchParkDetails(parkCode)
    }

    private fun fetchParkDetails(parkCode: String) {
        Log.d("ParkDetailActivity", "Fetching details for park code: $parkCode")
        RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { npsResponse ->
                        // Assuming the park details are in the data field of the response
                        val park = npsResponse.data.firstOrNull() // Adjust based on actual response structure
                        park?.let {
                            populateParkDetails(it) // Pass the park object to populate the UI
                        } ?: run {
                            Toast.makeText(this@ParkDetailActivity, "Park details not found", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this@ParkDetailActivity, "Park details not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ParkDetailActivity", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Log.e("ParkDetailActivity", "Error fetching park details: ${t.message}")
            }
        })
    }

    private fun populateParkDetails(park: Park) {
        // Populate UI elements with park details
        parkNameTextView.text = park.fullName
        parkDescriptionTextView.text = park.description
        parkLatitudeTextView.text = park.latitude?.toString() ?: "N/A"
        parkLongitudeTextView.text = park.longitude?.toString() ?: "N/A"

        // Format the address
        val address = park.addresses.joinToString("\n") {
            "${it.line1}, ${it.line2 ?: ""}, ${it.line3 ?: ""}, ${it.city}, ${it.postalCode}, ${it.stateCode}"
        }.trim().replace(", ,", ",") // Remove empty lines
        parkAddressTextView.text = address

        // Populate activities
        val activities = park.activities.joinToString("\n") { it.name }
        parkActivitiesTextView.text = activities.ifEmpty { "No activities available." }

        // Populate contact information
        val contactNumber = park.contacts.phoneNumbers.joinToString("\n") { it.phoneNumber }
        parkContactsPhoneTextView.text = contactNumber.ifEmpty { "No contact number available." }

        val contactEmail = park.contacts.emailAddresses.joinToString("\n") { it.emailAddress }
        parkContactsEmailTextView.text = contactEmail.ifEmpty { "No contact email available." }

        // Populate weather information
        parkWeatherInfoTextView.text = park.weatherInfo ?: "No weather information available."

        // Populate entrance passes
        val entrancePass = park.entrancePasses.joinToString("\n") { "${it.cost}, ${it.description}, ${it.title}" }
        parkEntrancePassesTextView.text = if (entrancePass.isNotEmpty()) {
            entrancePass
        } else {
            "No entrance fee information available."
        }

        // Handle operating hours
        val firstOperatingHours = park.operatingHours.firstOrNull()
        firstOperatingHours?.let { operatingHours ->
            val standardHours = operatingHours.standardHours
            val hoursText = """
            Sunday: ${standardHours.sunday}
            Monday: ${standardHours.monday}
            Tuesday: ${standardHours.tuesday}
            Wednesday: ${standardHours.wednesday}
            Thursday: ${standardHours.thursday}
            Friday: ${standardHours.friday}
            Saturday: ${standardHours.saturday}
        """.trimIndent()
            parkOperatingHoursTextView.text = hoursText
        } ?: run {
            parkOperatingHoursTextView.text = "Operating hours not available"
        }

        // Set up the images RecyclerView if there are images
        if (park.images.isNotEmpty()) {
            val imagesAdapter = ImagesAdapter(park.images.map { it.url }) // Ensure only URLs are passed
            parkImagesRecyclerView.adapter = imagesAdapter
        } else {
            Toast.makeText(this, "No images available for this park", Toast.LENGTH_SHORT).show()
        }
    }
}