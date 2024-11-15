package com.example.trailblaze.ui.Map

import com.example.trailblaze.nps.Park
import com.google.android.libraries.places.api.model.Place

sealed class LocationItem {
    data class PlaceItem(val place: Place) : LocationItem()
    data class ParkItem(val park: Park) : LocationItem()
}