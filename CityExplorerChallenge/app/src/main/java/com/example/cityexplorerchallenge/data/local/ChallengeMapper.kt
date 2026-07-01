package com.example.cityexplorerchallenge.data.local

import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.domain.model.Place
import com.example.cityexplorerchallenge.domain.model.PlaceCategory

fun Challenge.toEntity(): ChallengeEntity {
    return ChallengeEntity(
        id = id,
        title = title,
        description = description,
        category = category.name,
        placeId = targetPlace.id,
        placeName = targetPlace.name,
        placeLatitude = targetPlace.latitude,
        placeLongitude = targetPlace.longitude,
        distanceMeters = distanceMeters,
        status = status.name,
        generatedReason = generatedReason.joinToString(separator = "||"),
        createdAt = createdAt,
        completedAt = completedAt
    )
}

fun ChallengeEntity.toDomain(): Challenge {
    val placeCategory = runCatching {
        PlaceCategory.valueOf(category)
    }.getOrDefault(PlaceCategory.UNKNOWN)

    val challengeStatus = runCatching {
        ChallengeStatus.valueOf(status)
    }.getOrDefault(ChallengeStatus.ACTIVE)

    val place = Place(
        id = placeId,
        name = placeName,
        category = placeCategory,
        latitude = placeLatitude,
        longitude = placeLongitude,
        distanceMeters = distanceMeters
    )

    return Challenge(
        id = id,
        title = title,
        description = description,
        category = placeCategory,
        targetPlace = place,
        distanceMeters = distanceMeters,
        status = challengeStatus,
        generatedReason = generatedReason
            .split("||")
            .filter { it.isNotBlank() },
        createdAt = createdAt,
        completedAt = completedAt
    )
}