package com.example.trailblaze.ui.UserPreferences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider

class UserPreferencesViewModel {
    val city = MutableLiveData<String>()
    val state = MutableLiveData<String>()
    val zip = MutableLiveData<String>()
    val distance = MutableLiveData<Double>() // Assuming distance is a Double
}