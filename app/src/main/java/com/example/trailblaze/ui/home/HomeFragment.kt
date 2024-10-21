package com.example.trailblaze.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trailblaze.databinding.FragmentHomeBinding
import com.example.trailblaze.nps.NPSResponse
import com.example.trailblaze.nps.ParksAdapter
import com.example.trailblaze.nps.RetrofitInstance
import com.example.trailblaze.settings.SettingsScreenActivity
import com.example.trailblaze.ui.MenuActivity
import com.example.trailblaze.ui.parks.ParkDetailActivity
import com.example.trailblaze.ui.profile.UserListActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.trailblaze.ui.profile.UserAdapter
import com.example.trailblaze.ui.profile.User




class HomeFragment : Fragment() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersAdapter: UserAdapter
    private var userList: List<User> = listOf()

    private var _binding: FragmentHomeBinding? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var parksRecyclerView: RecyclerView   // RecyclerView to display parks
    private lateinit var parksAdapter: ParksAdapter         // Adapter for RecyclerView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.menuButton.setOnClickListener {
            val intent = Intent(activity, MenuActivity::class.java)
            startActivity(intent)
        }

        binding.settingsbtn.setOnClickListener {
            val intent = Intent(activity, SettingsScreenActivity::class.java)
            startActivity(intent)
        }

        binding.addFriend.setOnClickListener {
            val intent = Intent(activity, UserListActivity::class.java)
            startActivity(intent)
        }



        // RecyclerView setup
        parksRecyclerView = binding.parksRecyclerView
        parksRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Initialize adapter with an empty list
        parksAdapter = ParksAdapter(emptyList()) { park ->
            // Handle park item clicks
            val intent = Intent(context, ParkDetailActivity::class.java).apply {
                putExtra("PARK_INDEX", parksAdapter.parksList.indexOf(park))
            }
            startActivity(intent)
        }

        // Set adapter to RecyclerView
        parksRecyclerView.adapter = parksAdapter

        // Fetch parks data
        fetchParksData()

        // Username fetching logic
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            fetchUsername()
        } else {
            binding.homepageusername.text = "<UserName>"
        }

        // Initialize RecyclerView
        usersRecyclerView = binding.usersRecyclerView
        usersRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Set up the adapter
        usersAdapter = UserAdapter(userList) { user ->
            // Handle user click, for example: navigate to user profile
        }
        usersRecyclerView.adapter = usersAdapter

        // Load users (you would need to implement this)
        loadUsers()
        return root
    }

    private fun fetchUsername() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get the current user's ID

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") // Fetch the username
                        binding.homepageusername.text = username // Set the username in the TextView
                    } else {
                        // Handle the case where the document does not exist
                        binding.homepageusername.text = "Username not found"
                    }
                }
                .addOnFailureListener {
                }
        } else {
            // Handle the case where userId is null (not logged in)
            binding.homepageusername.text = "Not logged in"
        }
    }



    private fun fetchParksData() {
        RetrofitInstance.api.getParks(10).enqueue(object : Callback<NPSResponse> {
            override fun onResponse(call: Call<NPSResponse>, response: Response<NPSResponse>) {
                if (response.isSuccessful) {
                    val parks = response.body()?.data // Update the RecyclerView with fetched parks data
                    if (parks != null) {
                        parksAdapter.updateData(parks) // Update the adapter with the fetched park data
                    } else {
                        Log.e("HomeFragment", "Parks data is null")
                    }
                } else {
                    Log.e("HomeFragment", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NPSResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching parks: ${t.message}")
                // Handle failure case (e.g., show a Toast message)
            }
        })
    }
    private fun loadUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = mutableListOf<User>()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    users.add(user) // Add the user to the list
                }
                // Update the adapter with the fetched users
                usersAdapter.updateUserList(users)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error fetching users: ${exception.message}")
                // Handle the error (e.g., show a Toast message)
            }
    }




    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}