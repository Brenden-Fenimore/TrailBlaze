package com.example.trailblaze

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NPSApi {
    @GET("parks")
    fun getParks(
        @Query("limit") limit: Int,
        @Query("api_key") apiKey: String = "cQvtQCUyKgWjZQHWMJsXwXjDs7ZALBbUKTFgC9As"
    ): Call<NPSResponse>
}
