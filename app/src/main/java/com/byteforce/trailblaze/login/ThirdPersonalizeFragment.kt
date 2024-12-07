package com.byteforce.trailblaze.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatCheckBox
import com.byteforce.trailblaze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ThirdPersonalizeFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the layout
        val view = inflater.inflate(R.layout.fragment_third_personalize, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        view.findViewById<Button>(R.id.continuebtn3).setOnClickListener {
            //navigate to ThirdFragment
            (activity as? PersonalizeActivity)?.loadFragment(FourthPersonalizeFragment())
        }

        // Setup checkbox listeners
        view.findViewById<AppCompatCheckBox>(R.id.interaction1).setOnCheckedChangeListener { _, isChecked ->
            updatePrivacySetting(!isChecked) // Set isPrivateAccount to opposite of checkbox state
        }

        view.findViewById<AppCompatCheckBox>(R.id.interaction2).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateVisibilitySetting("leaderboardVisible", true)
            }
        }

        view.findViewById<AppCompatCheckBox>(R.id.interaction4).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateVisibilitySetting("shareLocationVisible", true)
            }
        }

        return view
    }

    private fun updateVisibilitySetting(setting: String, value: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .set(mapOf(setting to value), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FifthPersonalize", "$setting updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("FifthPersonalize", "Error updating $setting: ", exception)
            }
    }
    private fun updatePrivacySetting(isPrivate: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .set(mapOf("isPrivateAccount" to isPrivate), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("ThirdPersonalize", "Privacy setting updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("ThirdPersonalize", "Error updating privacy setting: ", exception)
            }
    }

}
