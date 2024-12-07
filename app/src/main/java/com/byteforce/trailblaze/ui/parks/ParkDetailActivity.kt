package com.byteforce.trailblaze.ui.parks

import ImagesAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byteforce.trailblaze.nps.NPSResponse
import com.byteforce.trailblaze.nps.Park
import com.byteforce.trailblaze.nps.RetrofitInstance
import com.byteforce.trailblaze.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ParkDetailActivity : AppCompatActivity() {

    private var placeId: String? = null
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
    private lateinit var activitiesList: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park_detail)

        // Hide the ActionBar
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.chevron_left).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        initializeViews()

        // Get the data from intent
        parkCode = intent.getStringExtra("PARK_CODE") ?: ""
        placeId = intent.getStringExtra("PLACE_ID")

        // Handle data based on what was passed
        when {
            !placeId.isNullOrEmpty() -> fetchPlaceDetails(placeId!!)
            parkCode.isNotEmpty() -> fetchParkDetails(parkCode)
            else -> {
                Toast.makeText(this, "Invalid data provided", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Initialize buttons and their listeners
        setupButtons()
    }


    private fun initializeViews() {
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

    }

    private fun setupButtons() {
        // Initialize the favorite button
        favoriteButton = findViewById(R.id.favorite_park_btn)
        checkFavoriteStatus()
        favoriteButton.setOnClickListener {
            toggleFavoriteStatus()
        }

        // Initialize the bucket list button
        bucketListButton = findViewById(R.id.bucket_list_btn)
        bucketListButton.setOnClickListener {
            if(!placeId.isNullOrEmpty()){
                addToBucketList(placeId!!)
            }
            else {
                addToBucketList(parkCode)
            }
        }

        // Initialize the hike button with support for both Parks and Places
        val hikeButton: ImageButton = findViewById(R.id.hike_btn)
        hikeButton.setOnClickListener {
            val intent = Intent(this, AttemptTrailActivity::class.java)
            if (!placeId.isNullOrEmpty()) {
                // For Places
                intent.putExtra("PLACE_ID", placeId)
                intent.putExtra("PLACE_NAME", parkNameTextView.text.toString())
            } else {
                // For Parks
                intent.putExtra("PARK_CODE", parkCode)
                intent.putExtra("PARK_ACTIVITIES", activitiesList)
            }
            startActivity(intent)
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

        // Get the activities list from the park object and convert to the array
        activitiesList = park.activities.map { it.name }.toTypedArray()

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
            if(!placeId.isNullOrEmpty()){
                // Update the favorite button icon based on whether the park is in the user's favorites
                if (favoriteParks.contains(placeId)) {
                    // Show filled heart icon if park is a favorite
                    favoriteButton.setImageResource(R.drawable.favorite_filled)
                } else {
                    // Show outline heart icon if park is not a favorite
                    favoriteButton.setImageResource(R.drawable.favorite)
                }
            }
            else {
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
            if(!placeId.isNullOrEmpty()) {
                if (favoriteParks.contains(placeId)) {
                    // Park is already a favorite; proceed to remove it
                    userDocRef.update(
                        "favoriteParks",
                        FieldValue.arrayRemove(placeId)
                    )        // Remove park from favorites
                        .addOnSuccessListener {
                            // Notify user of success
                            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                            // Update to outline icon to reflect removal
                            favoriteButton.setImageResource(R.drawable.favorite)
                        }
                        .addOnFailureListener { e ->
                            // Notify user of failure
                            Toast.makeText(this, "Failed to remove from favorites: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                else {
                    // Park is not a favorite; proceed to add it
                    userDocRef.update("favoriteParks", FieldValue.arrayUnion(placeId))     // Add park to favorites
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

            //NPS Favorite toggle
            else {
                if (favoriteParks.contains(parkCode)) {
                    // Park is already a favorite; proceed to remove it
                    userDocRef.update(
                        "favoriteParks",
                        FieldValue.arrayRemove(parkCode)
                    )        // Remove park from favorites
                        .addOnSuccessListener {
                            // Notify user of success
                            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                            // Update to outline icon to reflect removal
                            favoriteButton.setImageResource(R.drawable.favorite)
                        }
                        .addOnFailureListener { e ->
                            // Notify user of failure
                            Toast.makeText(this, "Failed to remove from favorites: ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                else {
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

                        // Show confetti when adding to favorites
                        showConfetti()

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

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.DISPLAY_NAME,
            Place.Field.EDITORIAL_SUMMARY,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.INTERNATIONAL_PHONE_NUMBER,
            Place.Field.WEBSITE_URI,
            Place.Field.RATING,
            Place.Field.PHOTO_METADATAS,
            Place.Field.OPENING_HOURS,
            Place.Field.TYPES,
            Place.Field.LOCATION,
            Place.Field.PRICE_LEVEL
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        Places.createClient(this).fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                populatePlaceDetails(place)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading place details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populatePlaceDetails(place: Place) {
        parkNameTextView.text = place.displayName

        // Handle editorial summary for description
        parkDescriptionTextView.text = place.editorialSummary?.toString()
            ?: "No description available"

        // Handle address components
        val addressComponents = place.addressComponents
        if (addressComponents != null) {
            val streetNumber = addressComponents.asList().find { it.types.contains("street_number") }?.name ?: ""
            val route = addressComponents.asList().find { it.types.contains("route") }?.name ?: ""
            val city = addressComponents.asList().find { it.types.contains("locality") }?.name ?: ""
            val state = addressComponents.asList().find { it.types.contains("administrative_area_level_1") }?.name ?: ""
            val postalCode = addressComponents.asList().find { it.types.contains("postal_code") }?.name ?: ""

            val formattedAddress = buildString {
                if (streetNumber.isNotEmpty() && route.isNotEmpty()) {
                    append("$streetNumber $route")
                }
                if (city.isNotEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append(city)
                }
                if (state.isNotEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append(state)
                }
                if (postalCode.isNotEmpty()) {
                    if (isNotEmpty()) append(" ")
                    append(postalCode)
                }
            }

            parkAddressTextView.text = if (formattedAddress.isNotEmpty()) formattedAddress else "Address not available"
        } else {
            parkAddressTextView.text = "Address not available"
        }

        parkContactsPhoneTextView.text = place.internationalPhoneNumber ?: "No phone number available"

        // Handle opening hours with detailed formatting
        place.openingHours?.let { hours ->
            val formattedHours = buildString {
                // Add regular hours
                hours.weekdayText?.forEachIndexed { index, dayText ->
                    append(dayText)
                    if (index < (hours.weekdayText?.size ?: 0) - 1) {
                        append("\n")
                    }
                }
            }

            parkOperatingHoursTextView.text = formattedHours
        } ?: run {
            parkOperatingHoursTextView.text = "Hours not available"
        }

        // Set the lat/lng values
        place.location?.let { latLng ->
            parkLatitudeTextView.text = latLng.latitude.toString()
            parkLongitudeTextView.text = latLng.longitude.toString()
        } ?: run {
            parkLatitudeTextView.text = "Latitude not available"
            parkLongitudeTextView.text = "Longitude not available"
        }

        // Set the price level
        val priceLevel = when (place.priceLevel) {
            0 -> "Free"
            1 -> "Inexpensive"
            2 -> "Moderate"
            3 -> "Expensive"
            4 -> "Very Expensive"
            else -> "Price information not available"
        }

        parkEntrancePassesTextView.text = "Price Level: $priceLevel"

        parkContactsEmailTextView.text = place.websiteUri?.toString() ?: "Website not available"

        parkContactsEmailTextView.setOnClickListener {
            place.websiteUri?.let { uri ->
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        // Handle photos
        place.photoMetadatas?.let { photoMetadatas ->
            val photoUrls = mutableListOf<String>()
            var loadedPhotos = 0

            photoMetadatas.forEach { metadata ->
                val photoRequest = FetchPhotoRequest.builder(metadata)
                    .setMaxWidth(1000)
                    .setMaxHeight(1000)
                    .build()

                Places.createClient(this).fetchPhoto(photoRequest)
                    .addOnSuccessListener { fetchPhotoResponse ->
                        val photoUrl = saveBitmapToFile(fetchPhotoResponse.bitmap)
                        photoUrls.add(photoUrl)

                        loadedPhotos++
                        if (loadedPhotos == photoMetadatas.size) {
                            parkImagesRecyclerView.adapter = ImagesAdapter(photoUrls)
                        }
                    }
            }
        }

        // Hide irrelevant views for Places
        parkWeatherInfoTextView.visibility = View.GONE
        parkActivitiesTextView.visibility = View.GONE
    }

    // Helper function to save bitmap as file and return URL
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(cacheDir, "place_photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.toURI().toString()
    }
}




