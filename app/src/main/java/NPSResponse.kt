package com.example.trailblaze

// Data classes for mapping the API response
data class NPSResponse(
    val total: String,              // Total number of parks
    val data: List<Park>            // List of parks
)

// Data class for each park in the response
data class Park(
    val fullName: String,           // Full name of the park
    val description: String,        // Description of the park
    val latitude: String?,           // Latitude of the park
    val longitude: String?           // Longitude of the park
)
