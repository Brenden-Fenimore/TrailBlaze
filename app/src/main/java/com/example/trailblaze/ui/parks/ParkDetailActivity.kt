package com.example.trailblaze.ui.parks

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Fetch parks data again or use a shared data source
        fetchParksData { parksList ->
            if (parkIndex >= 0 && parkIndex < parksList.size) {
                val park = parksList[parkIndex] // Get the park using the index
                parkNameTextView.text = park.fullName
                parkDescriptionTextView.text = park.description
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