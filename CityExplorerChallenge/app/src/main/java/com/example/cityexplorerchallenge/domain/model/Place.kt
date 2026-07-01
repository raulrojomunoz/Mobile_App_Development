package com.example.cityexplorerchallenge.domain.model

data class Place(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double
)