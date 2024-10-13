package com.example.trailblaze

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Object to manage Retrofit instance (singleton pattern)
object RetrofitInstance {
    val api: NPSApi by lazy {
        // Build Retrofit instance
        Retrofit.Builder()
            .baseUrl("https://developer.nps.gov/api/v1/")               // Base URL for NPS API
            .addConverterFactory(GsonConverterFactory.create())         // Converter for JSON parsing
            .build()
            .create(NPSApi::class.java)                                 // Create an implementation of NPSApi
    }
}
