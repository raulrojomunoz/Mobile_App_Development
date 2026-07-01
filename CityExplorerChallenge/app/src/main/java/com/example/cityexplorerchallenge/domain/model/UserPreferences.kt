package com.example.cityexplorerchallenge.domain.model

data class UserPreferences(
    val preferredCategories: Set<PlaceCategory> = setOf(
        PlaceCategory.CULTURE,
        PlaceCategory.HISTORY,
        PlaceCategory.NATURE
    ),
    val maxDistanceMeters: Int = 1500
)