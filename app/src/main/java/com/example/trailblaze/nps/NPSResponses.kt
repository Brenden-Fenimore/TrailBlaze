package com.example.trailblaze.nps

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
    val activities: List<Activies>,
    val contacts: Contacts,
    val entrancePasses: List<Passes>,
    val images: List<Images>,
    val stateCode: String,
    val weatherInfo: String,
    val operatingHours: List<OperatingHours>
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

data class Activies(
    val id: String,
    val name: String
)

data class OperatingHours(
    val name: String,
    val description: String,
    val standardHours: StandardHours,
    val exceptions: List<Exceptions>
)

data class StandardHours(
    val sunday: String,
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String
)

data class Exceptions(
    val name: String,
    val startDate: String,
    val endDate: String,
    val exceptionHours: ExceptionHours
)

data class ExceptionHours(
    val sunday: String,
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String
)

data class Contacts(
    val phoneNumbers: List<PhoneNumber>,
    val emailAddresses: List<EmailAddress>
)

data class PhoneNumber(
    val phoneNumber: String,
    val description: String,
    val extension: String,
    val type: ContactType
)

data class EmailAddress(
    val emailAddress: String,
    val description: String
)

enum class ContactType { Voice, Fax, TTY }

data class Passes(
    val cost: String,
    val description: String,
    val title: String
)