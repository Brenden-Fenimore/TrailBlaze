package com.example.trailblaze

import com.example.trailblaze.ParksAdapter
import com.example.trailblaze.RetrofitInstance
import com.example.trailblaze.NPSResponse
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trailblaze.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding   // ViewBinding for the main layout
    private lateinit var recyclerView: RecyclerView     // RecyclerView to display parks
    private lateinit var parksAdapter: ParksAdapter     // Adapter to display park data


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hide the ActionBar
        supportActionBar?.hide()

            // Inflate the layout using ViewBinding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            navView.setupWithNavController(navController)

            // Initialize RecyclerView and adapter
            recyclerView = findViewById(R.id.recyclerView)                  // Manually find RecyclerView and set it up
            parksAdapter = ParksAdapter(emptyList())                        // Initialize with an empty list
            recyclerView.layoutManager = LinearLayoutManager(this)  // Set vertical layout for RecyclerView
            recyclerView.adapter = parksAdapter                             // Assign adapter to RecyclerView

            // Call the API to fetch park data
            RetrofitInstance.api.getParks(10).enqueue(object : Callback<NPSResponse> {
                override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                    if (response.isSuccessful) {
                        // Update the RecyclerView with parks data from the API
                        val parks = response.body()?.data
                        parks?.let {
                            // Pass the park data to the RecyclerView adapter to display it
                            parksAdapter.updateData(it)
                        }
                    } else {
                        showErrorToast("Failed to load parks: ${response.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                    // Log the error and show a message to the user
                    Log.e("MainActivity", "API call failed", t)
                    showErrorToast("Failed to load parks. Check your internet connection.")
                }
            })
        }
    // Helper function to display a Toast with error message
    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
