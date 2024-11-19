package com.example.trailblaze.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.trailblaze.MainActivity
import com.example.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FifthPersonalizeFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fifth_personalize, container, false)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.yesBtn).setOnClickListener {
            // Set watcher visibility to true before navigating
            setWatcherVisibility(true) {
                navigateToMainActivity()
            }
        }

        view.findViewById<Button>(R.id.contBtn).setOnClickListener {
            navigateToMainActivity()
        }

        return view
    }

    private fun setWatcherVisibility(visible: Boolean, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .set(mapOf("watcherVisible" to visible), SetOptions.merge())
                .addOnSuccessListener {
                    onComplete()
                }
                .addOnFailureListener { exception ->
                    Log.e("FifthPersonalize", "Error updating watcher visibility: ", exception)
                    onComplete()
                }
        } else {
            onComplete()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
    }
}