package com.example.trailblaze.com.example.trailblaze.nps

import com.example.trailblaze.nps.NPSResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Interface defining the API endpoints
interface NPSApi {
    @GET("parks")
    fun getParks(
        @Query("limit") limit: Int,                // Query parameter to limit the number of results
        @Query("api_key") apiKey: String = "cQvtQCUyKgWjZQHWMJsXwXjDs7ZALBbUKTFgC9As"       // API key for authentication
    ): Call<NPSResponse>                           // Call that returns NPSResponse
}
