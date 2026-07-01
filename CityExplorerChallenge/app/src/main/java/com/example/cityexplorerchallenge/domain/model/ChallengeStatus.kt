package com.example.cityexplorerchallenge.domain.model

enum class ChallengeStatus(
    val displayName: String
) {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    EXPIRED("Expired")
}