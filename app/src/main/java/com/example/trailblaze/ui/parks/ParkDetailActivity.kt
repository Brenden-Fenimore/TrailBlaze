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

    private var parkIndex: Int = -1 // Default to -1 if not found
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

        // Retrieve the park index from the intent
        parkIndex = intent.getIntExtra("PARK_INDEX", -1) // Default to -1 if not found

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


        // Fetch parks data again or use a shared data source
        fetchParksData { parksList ->
            if (parkIndex >= 0 && parkIndex < parksList.size) {
                val park = parksList[parkIndex] // Get the park using the index
                val address = park.addresses.joinToString("\n") { "${it.line1}, ${it.line2}, ${it.line3}, ${it.city}, ${it.postalCode}, ${it.stateCode}" }
                val contactNumber = park.contacts.phoneNumbers.joinToString("\n"){ it.phoneNumber }
                val contactEmail = park.contacts.emailAddresses.joinToString("\n"){ it.emailAddress }
                val entrancePass = park.entrancePasses.joinToString("\n") {"${it.cost}, ${it.description}, ${it.title}"   }
                val weatherInfo = park.weatherInfo ?: "No weather information available"
                val firstOperatingHours = park.operatingHours.firstOrNull()
                firstOperatingHours?.let {operatingHours ->
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
                    parkOperatingHoursTextView.text = hoursText} ?: run{parkOperatingHoursTextView.text = "Operating hours not available"}
                val activity = park.activities.joinToString("\n") { it.name }

                parkNameTextView.text = park.fullName
                parkDescriptionTextView.text = park.description
                parkLatitudeTextView.text = park.latitude ?: "N/A"
                parkLongitudeTextView.text = park.longitude ?: "N/A"
                parkAddressTextView.text = address
                parkActivitiesTextView.text = activity
                parkContactsPhoneTextView.text = contactNumber
                parkContactsEmailTextView.text = contactEmail
                parkWeatherInfoTextView.text = weatherInfo
                if (entrancePass.isNotEmpty()) {
                    parkEntrancePassesTextView.text = entrancePass
                } else {
                    parkEntrancePassesTextView.text = "No entrance fee information available."
                }

                // Debugging: Log image URLs list size
                Log.d("ParkDetailActivity", "Image List Size: ${park.images.size}")

                // Set up the images RecyclerView
                if (park.images.isNotEmpty()) {
                    val imagesAdapter = ImagesAdapter(park.images.map { it.url }) // Ensure only URLs are passed
                    parkImagesRecyclerView.adapter = imagesAdapter
                } else {
                    Toast.makeText(this, "No images available for this park", Toast.LENGTH_SHORT).show()
                }


                // Optionally load the park image if you have an ImageView for it
            } else {
                Toast.makeText(this, "Invalid park index", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchParksData(onDataFetched: (List<Park>) -> Unit) {
        RetrofitInstance.api.getParks(10).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    val parks = response.body()?.data ?: emptyList()
                    onDataFetched(parks) // Return the parks list to the caller
                } else {
                    Toast.makeText(this@ParkDetailActivity, "Failed to fetch parks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Toast.makeText(this@ParkDetailActivity, "Error fetching parks: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}