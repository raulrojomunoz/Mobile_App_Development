package com.example.cityexplorerchallenge.domain.model

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val category: PlaceCategory,
    val targetPlace: Place,
    val distanceMeters: Double,
    val status: ChallengeStatus,
    val generatedReason: List<String>,
    val createdAt: Long,
    val completedAt: Long? = null
)