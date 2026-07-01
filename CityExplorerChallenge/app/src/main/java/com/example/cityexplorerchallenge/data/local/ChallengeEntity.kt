package com.example.cityexplorerchallenge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_history")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val placeId: String,
    val placeName: String,
    val placeLatitude: Double,
    val placeLongitude: Double,
    val distanceMeters: Double,
    val status: String,
    val generatedReason: String,
    val createdAt: Long,
    val completedAt: Long?
)