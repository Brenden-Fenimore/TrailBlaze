package com.example.trailblaze.ui.parks

sealed class DetailSource {
    data class ParkDetail(val parkCode: String) : DetailSource()
    data class PlaceDetail(val placeId: String) : DetailSource()
}