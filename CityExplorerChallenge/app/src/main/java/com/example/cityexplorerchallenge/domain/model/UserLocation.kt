package com.example.cityexplorerchallenge.domain.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val isFallback: Boolean = false
)