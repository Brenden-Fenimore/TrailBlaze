package com.example.trailblaze.nps

import android.app.Activity

// Data classes for mapping the API response
data class NPSResponse(
    val total: String,              // Total number of parks
    val data: List<Park>            // List of parks
)

// Data class for each park in the response
data class Park(
    val parkCode: String,
    val fullName: String,           // Full name of the park
    val description: String,        // Description of the park
    val latitude: String?,           // Latitude of the park
    val longitude: String?,
    val states: String,
    val addresses: List<Address>,
//    val activities: List<String>,
//    val contacts: String,
//    val entrancePasses: String,
    val images: List<Images>
//    val stateCode: String,
//    val weatherInfo: String,
//    val operatingHours: String

)

data class Address(
    val countryCode: String,
    val city: String,
    val provinceTerritoryCode: String,
    val postalCode: String,
    val line1: String,
    val stateCode: String,
    val line2: String,
    val line3: String
)

data class Images(
    val credit: String,
    val altText: String,
    val title: String,
    val caption: String,
    val url: String
)