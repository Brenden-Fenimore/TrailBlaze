package com.example.trailblaze.ui.parks

import ImagesAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.Park
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
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
    private lateinit var favoriteButton: ImageButton            // Declare button variables
    private val firestore = FirebaseFirestore.getInstance()     // Declare Firestore instance
    private lateinit var bucketListButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park_detail)
        // Hide the ActionBar
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // Get the park code from the intent
        parkCode = intent.getStringExtra("PARK_CODE") ?: ""

        fetchParkDetails(parkCode)

        val hikeButton: ImageButton = findViewById(R.id.hike_btn)
        hikeButton.setOnClickListener {
            val intent = Intent(this, AttemptTrailActivity::class.java)
            intent.putExtra("PARK_CODE", parkCode)
            startActivity(intent)
        }

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

        // Initialize the favorite button and set up its click listener
        favoriteButton = findViewById(R.id.favorite_park_btn)
        checkFavoriteStatus()       // Check if the park is already in favorites and update the button

        favoriteButton.setOnClickListener {
            toggleFavoriteStatus()  // Toggle the favorite status
        }

        // Initialize the bucket list button and set up its click listener
        bucketListButton = findViewById(R.id.bucket_list_btn)
        bucketListButton.setOnClickListener {
            addToBucketList(parkCode)   // Call function to add park to bucket list
        }

    }

    private fun fetchParkDetails(parkCode: String) {
        Log.d("ParkDetailActivity", "Fetching details for park code: $parkCode")
        RetrofitInstance.api.getParkDetails(parkCode).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { npsResponse ->
                        val park = npsResponse.data.firstOrNull() // Assuming the first park in the response
                        park?.let {
                            populateParkDetails(it)
                        } ?: run {
                            Toast.makeText(this@ParkDetailActivity, "Park details not found", Toast.LENGTH_SHORT).show()
                        }
                    }
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

    // Initializes and sets the favorite status of a park when the activity loads.
    private fun checkFavoriteStatus() {
        // Retrieve the current user's ID; if unavailable, exit the function
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Reference to the user's Firestore document
        val userDocRef = firestore.collection("users").document(userId)

        // Retrieve user's document to check if the current park is in their favorites
        userDocRef.get().addOnSuccessListener { document ->
            // Retrieve favorite parks list; if it's missing, use an empty list
            val favoriteParks = document.get("favoriteParks") as? List<String> ?: emptyList()

            // Update the favorite button icon based on whether the park is in the user's favorites
            if (favoriteParks.contains(parkCode)) {
                // Show filled heart icon if park is a favorite
                favoriteButton.setImageResource(R.drawable.favorite_filled)
            } else {
                // Show outline heart icon if park is not a favorite
                favoriteButton.setImageResource(R.drawable.favorite)
            }
        }
    }

    // Toggles the favorite status of a park: adds it if not in favorites, or removes it if already a favorite
    private fun toggleFavoriteStatus() {
        // Retrieve the current user's ID; if unavailable, exit the function
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Reference to the user's Firestore document
        val userDocRef = firestore.collection("users").document(userId)

        // Get user's favorite parks list and check if the park is already a favorite
        userDocRef.get().addOnSuccessListener { document ->
            val favoriteParks = document.get("favoriteParks") as? List<String> ?: emptyList()

            if (favoriteParks.contains(parkCode)) {
                // Park is already a favorite; proceed to remove it
                userDocRef.update("favoriteParks", FieldValue.arrayRemove(parkCode))        // Remove park from favorites
                    .addOnSuccessListener {
                        // Notify user of success
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        // Update to outline icon to reflect removal
                        favoriteButton.setImageResource(R.drawable.favorite)
                    }
                    .addOnFailureListener { e ->
                        // Notify user of failure
                        Toast.makeText(this, "Failed to remove from favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Park is not a favorite; proceed to add it
                userDocRef.update("favoriteParks", FieldValue.arrayUnion(parkCode))     // Add park to favorites
                    .addOnSuccessListener {
                        // Show confetti when adding to favorites
                        showConfetti()
                        // Notify user of success
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                        // Update to filled icon to reflect addition
                        favoriteButton.setImageResource(R.drawable.favorite_filled)
                    }
                    .addOnFailureListener { e ->
                        // Notify user of failure
                        Toast.makeText(this, "Failed to add to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Function to add the current park to the user's bucket list in Firestore
    private fun addToBucketList(parkCode: String) {
        // Get the user's unique ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Reference to the user's document in Firestore
        val userDocRef = firestore.collection("users").document(userId)

        // Retrieve the current bucketListParks list
        userDocRef.get().addOnSuccessListener { document ->
            // Check if the document exists and retrieve the list of bucket list parks
            val bucketListParks = document.get("bucketListParks") as? List<String> ?: emptyList()
            // Check if the park is already in the bucket list
            if (bucketListParks.contains(parkCode)) {
                // If the park already exists in the list, show a message to the user
                Toast.makeText(this, "Park already in your bucket list", Toast.LENGTH_SHORT).show()
            } else {
                // If the park is not in the bucket list, add it using arrayUnion to avoid duplicates
                userDocRef.update("bucketListParks", FieldValue.arrayUnion(parkCode))
                    .addOnSuccessListener {
                        // Display success message when the park is successfully added to the bucket list
                        Toast.makeText(this, "Added to bucket list", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Display failure message if adding to the bucket list fails
                        Toast.makeText(this, "Failed to add to bucket list: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            // If retrieving the document fails, show an error message
            Toast.makeText(this, "Failed to retrieve bucket list: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Displays a confetti animation on the screen for a celebratory effect
    private fun showConfetti() {
        // Get the KonfettiView from the layout
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)

        // Set the view to visible
        konfettiView.visibility = View.VISIBLE

        // Configure the confetti animation properties
        konfettiView.build()
            // Define confetti colors
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN)
            // Allow confetti to fall in all directions
            .setDirection(0.0, 359.0)
            // Define speed range for confetti particles
            .setSpeed(1f, 5f)
            // Set the duration each confetti particle remains on screen (3 seconds)
            .setTimeToLive(3000L) // Increase the time to live to allow for longer fall
            .addShapes(Shape.Circle)
            .addSizes(Size(8))
            // Set the position to emit from the right side and farther down
            .setPosition(konfettiView.width + 400f, konfettiView.width + 400f, -100f, -50f)
            // Stream 300 particles for 3000 milliseconds (3 seconds)
            .stream(300, 3000L)

        // Hide the konfetti view after a delay to stop the animation gracefully
        konfettiView.postDelayed({
            konfettiView.visibility = View.GONE
            // Hide after 6 seconds
        }, 6000)
    }
}



