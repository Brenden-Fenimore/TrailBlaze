package com.example.trailblaze.nps

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

    @GET("parks") // Change this line to use the parkCode as a path variable
    fun getParkDetails(
        @Query("parkCode") parkCode: String, // Use @Path instead of @Query
        @Query("api_key") apiKey: String = "cQvtQCUyKgWjZQHWMJsXwXjDs7ZALBbUKTFgC9As"
    ): Call<NPSResponse>

    @GET("parks")
    fun getParksbyQuery(
        @Query("q") searchTerm: String? = null,
        @Query("api_key") apiKey: String = "cQvtQCUyKgWjZQHWMJsXwXjDs7ZALBbUKTFgC9As"
    ): Call<NPSResponse>
}