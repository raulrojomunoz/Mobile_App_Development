package com.example.cityexplorerchallenge.domain.engine

import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.domain.model.Place
import com.example.cityexplorerchallenge.domain.model.PlaceCategory
import com.example.cityexplorerchallenge.domain.model.UserLocation
import com.example.cityexplorerchallenge.domain.model.UserPreferences
import java.util.Calendar
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class ChallengeGenerator {

    private companion object {
        const val EARTH_RADIUS_METERS = 6371000.0
        const val CATEGORY_SCORE_TOLERANCE = 20
        const val BEST_PLACE_OPTIONS = 3
        const val MAX_GENERATION_REASONS = 8
    }

    fun generateChallenge(
        userLocation: UserLocation,
        nearbyPlaces: List<Place>,
        completedChallenges: List<Challenge>,
        userPreferences: UserPreferences,
        previousActiveChallenge: Challenge? = null,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Challenge? {
        if (nearbyPlaces.isEmpty()) {
            return null
        }

        val completedPlaceIds = completedChallenges
            .map { challenge ->
                challenge.targetPlace.id
            }
            .toSet()

        val completedCategoryCounts = completedChallenges
            .groupingBy { challenge ->
                challenge.category
            }
            .eachCount()

        val orderedCompletedChallenges = completedChallenges
            .sortedByDescending { challenge ->
                challenge.completedAt ?: challenge.createdAt
            }

        val recentlyCompletedCategory = orderedCompletedChallenges
            .firstOrNull()
            ?.category

        val recentCategories = orderedCompletedChallenges
            .take(5)
            .map { challenge ->
                challenge.category
            }

        val placesWithDistance = nearbyPlaces
            .map { place ->
                val distance = calculateDistanceMeters(
                    startLatitude = userLocation.latitude,
                    startLongitude = userLocation.longitude,
                    endLatitude = place.latitude,
                    endLongitude = place.longitude
                )

                place.copy(distanceMeters = distance)
            }
            .filter { place ->
                place.name.isNotBlank()
            }

        if (placesWithDistance.isEmpty()) {
            return null
        }

        /*
         * Completed places are not removed immediately.
         *
         * If completed places were removed too early, categories with fewer real
         * OpenStreetMap results could disappear quickly. Instead, the generator
         * keeps all valid places and penalizes completed places later.
         */
        val candidatePlaces = placesWithDistance

        val availableCategories = candidatePlaces
            .map { place ->
                place.category
            }
            .filter { category ->
                category != PlaceCategory.UNKNOWN
            }
            .toSet()
            .ifEmpty {
                candidatePlaces.map { place ->
                    place.category
                }.toSet()
            }

        if (availableCategories.isEmpty()) {
            return null
        }

        val minCompletedCount = availableCategories.minOf { category ->
            completedCategoryCounts[category] ?: 0
        }

        val scoredCategories = availableCategories.map { category ->
            calculateCategoryScore(
                category = category,
                candidatePlaces = candidatePlaces,
                completedCategoryCounts = completedCategoryCounts,
                minCompletedCount = minCompletedCount,
                recentlyCompletedCategory = recentlyCompletedCategory,
                recentCategories = recentCategories,
                userPreferences = userPreferences,
                previousActiveChallenge = previousActiveChallenge,
                currentTimeMillis = currentTimeMillis
            )
        }

        val orderedCategoryResults = scoredCategories
            .sortedByDescending { result ->
                result.score
            }

        val bestCategoryScore = orderedCategoryResults
            .firstOrNull()
            ?.score
            ?: return null

        /*
         * The generator does not always pick only the absolute highest score.
         * It keeps a small group of strong options to make the experience less repetitive.
         */
        val bestCategoryOptions = orderedCategoryResults.filter { result ->
            result.score >= bestCategoryScore - CATEGORY_SCORE_TOLERANCE
        }

        val selectedCategoryResult = bestCategoryOptions.random(
            Random(currentTimeMillis)
        )

        val selectedCategory = selectedCategoryResult.category

        val placesInSelectedCategory = candidatePlaces
            .filter { place ->
                place.category == selectedCategory
            }

        if (placesInSelectedCategory.isEmpty()) {
            return null
        }

        val scoredPlaces = placesInSelectedCategory.map { place ->
            val placeScore = calculatePlaceScore(
                place = place,
                completedPlaceIds = completedPlaceIds,
                userPreferences = userPreferences,
                previousActiveChallenge = previousActiveChallenge,
                currentTimeMillis = currentTimeMillis
            )

            ScoredPlace(
                place = place,
                score = selectedCategoryResult.score + placeScore.score,
                reasons = (selectedCategoryResult.reasons + placeScore.reasons)
                    .distinct()
                    .take(MAX_GENERATION_REASONS)
            )
        }

        val bestPlaces = scoredPlaces
            .sortedByDescending { scoredPlace ->
                scoredPlace.score
            }
            .take(BEST_PLACE_OPTIONS)

        val selectedPlace = bestPlaces.random(
            Random(currentTimeMillis + selectedCategory.name.hashCode())
        )

        return Challenge(
            id = UUID.randomUUID().toString(),
            title = createChallengeTitle(selectedPlace.place),
            description = createChallengeDescription(selectedPlace.place),
            category = selectedPlace.place.category,
            targetPlace = selectedPlace.place,
            distanceMeters = selectedPlace.place.distanceMeters,
            status = ChallengeStatus.ACTIVE,
            generatedReason = selectedPlace.reasons,
            createdAt = currentTimeMillis
        )
    }

    private fun calculateCategoryScore(
        category: PlaceCategory,
        candidatePlaces: List<Place>,
        completedCategoryCounts: Map<PlaceCategory, Int>,
        minCompletedCount: Int,
        recentlyCompletedCategory: PlaceCategory?,
        recentCategories: List<PlaceCategory>,
        userPreferences: UserPreferences,
        previousActiveChallenge: Challenge?,
        currentTimeMillis: Long
    ): CategoryScoreResult {
        var score = 100
        val reasons = mutableListOf<String>()

        val completedCount = completedCategoryCounts[category] ?: 0
        val categoryOveruse = completedCount - minCompletedCount

        val placesInCategory = candidatePlaces.filter { place ->
            place.category == category
        }

        /*
         * Rule 1: User preferences.
         * Preferences increase the score, but they do not exclude other categories.
         */
        if (category in userPreferences.preferredCategories) {
            score += 40
            reasons.add("This category matches the user's preferences.")
        } else {
            score += 15
            reasons.add("This category is included to keep the challenge experience diverse.")
        }

        /*
         * Rule 2: Category balancing.
         * Categories completed less often receive a strong priority boost.
         */
        if (completedCount == minCompletedCount) {
            score += 260
            reasons.add("This category has been completed less often, so it receives higher priority.")
        } else {
            score -= categoryOveruse * 220
            reasons.add("This category has appeared more often, so the app lowers its priority.")
        }

        /*
         * Rule 3: Recently completed category penalty.
         */
        if (category == recentlyCompletedCategory) {
            score -= 180
            reasons.add("This category was completed recently, so it is strongly penalized.")
        }

        /*
         * Rule 4: Previous active challenge penalty.
         */
        if (previousActiveChallenge?.category == category) {
            score -= 140
            reasons.add("The previous active challenge used this category, so another category is preferred.")
        }

        /*
         * Rule 5: Recent streak prevention.
         */
        val recentCount = recentCategories.count { recentCategory ->
            recentCategory == category
        }

        when {
            recentCount >= 3 -> {
                score -= 260
                reasons.add("This category appeared too many times recently, so the app avoids a streak.")
            }

            recentCount == 2 -> {
                score -= 180
                reasons.add("This category appeared multiple times recently, so it receives a strong penalty.")
            }

            recentCount == 1 -> {
                score -= 70
                reasons.add("This category appeared recently, so it receives a small penalty.")
            }
        }

        /*
         * Rule 6: Distance availability.
         */
        val placesInsidePreferredDistance = placesInCategory.count { place ->
            place.distanceMeters <= userPreferences.maxDistanceMeters
        }

        if (placesInsidePreferredDistance > 0) {
            score += 60
            reasons.add("There are places in this category inside the user's preferred distance.")
        } else {
            score -= 60
            reasons.add("There are no places in this category inside the preferred distance.")
        }

        /*
         * Rule 7: Food domination control.
         * Food places are common in OpenStreetMap, so this rule prevents them from dominating.
         */
        if (category == PlaceCategory.FOOD) {
            val foodCount = completedCategoryCounts[PlaceCategory.FOOD] ?: 0

            if (foodCount > minCompletedCount) {
                score -= 220
                reasons.add("Food places are common nearby, so the app prevents this category from dominating.")
            }

            val recentFoodCount = recentCategories.count { recentCategory ->
                recentCategory == PlaceCategory.FOOD
            }

            if (recentFoodCount >= 2) {
                score -= 220
                reasons.add("Food appeared several times recently, so the app prioritizes other activities.")
            }
        }

        /*
         * Rule 8: Time-based recommendation.
         */
        val hour = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
        }.get(Calendar.HOUR_OF_DAY)

        when (hour) {
            in 6..11 -> {
                if (category == PlaceCategory.COFFEE || category == PlaceCategory.NATURE) {
                    score += 20
                    reasons.add("The time of day is suitable for coffee or nature challenges.")
                }
            }

            in 12..17 -> {
                if (category == PlaceCategory.CULTURE || category == PlaceCategory.HISTORY) {
                    score += 20
                    reasons.add("The afternoon is suitable for cultural or historical exploration.")
                }
            }

            in 18..22 -> {
                if (category == PlaceCategory.FOOD || category == PlaceCategory.COFFEE) {
                    score += 10
                    reasons.add("The evening is suitable for food or coffee challenges.")
                }
            }
        }

        /*
         * Rule 9: Unknown category penalty.
         */
        if (category == PlaceCategory.UNKNOWN) {
            score -= 300
            reasons.add("Unknown categories receive very low priority.")
        }

        /*
         * Rule 10: Small dynamic tie-breaker.
         */
        score += Random(
            currentTimeMillis + category.name.hashCode()
        ).nextInt(0, 10)

        return CategoryScoreResult(
            category = category,
            score = score,
            reasons = reasons
        )
    }

    private fun calculatePlaceScore(
        place: Place,
        completedPlaceIds: Set<String>,
        userPreferences: UserPreferences,
        previousActiveChallenge: Challenge?,
        currentTimeMillis: Long
    ): ScoreResult {
        var score = 0
        val reasons = mutableListOf<String>()

        /*
         * Rule 11: Distance score.
         */
        when {
            place.distanceMeters <= 500 -> {
                score += 80
                reasons.add("The place is very close to the user's current location.")
            }

            place.distanceMeters <= userPreferences.maxDistanceMeters -> {
                score += 60
                reasons.add("The place is within the user's preferred maximum distance.")
            }

            place.distanceMeters <= userPreferences.maxDistanceMeters * 1.5 -> {
                score += 20
                reasons.add("The place is slightly outside the preferred distance but still reachable.")
            }

            else -> {
                score -= 70
                reasons.add("The place is far from the user's current location.")
            }
        }

        /*
         * Rule 12: Exact place repetition penalty.
         * Completed places are allowed, but they receive a lower priority.
         */
        if (place.id in completedPlaceIds) {
            score -= 45
            reasons.add("This exact place was completed before, so it receives a lower priority.")
        } else {
            score += 85
            reasons.add("This exact place has not been completed before.")
        }

        /*
         * Rule 13: Previous destination penalty.
         */
        if (previousActiveChallenge?.targetPlace?.id == place.id) {
            score -= 180
            reasons.add("This was the previous active destination, so it is avoided.")
        }

        /*
         * Rule 14: Exploration value.
         */
        if (
            place.category == PlaceCategory.CULTURE ||
            place.category == PlaceCategory.HISTORY ||
            place.category == PlaceCategory.NATURE ||
            place.category == PlaceCategory.SPORT
        ) {
            score += 25
            reasons.add("This category supports a varied city exploration experience.")
        }

        /*
         * Rule 15: Small place-level tie-breaker.
         */
        score += Random(
            currentTimeMillis + place.id.hashCode().toLong()
        ).nextInt(0, 8)

        return ScoreResult(
            score = score,
            reasons = reasons
        )
    }

    private fun createChallengeTitle(
        place: Place
    ): String {
        return when (place.category) {
            PlaceCategory.CULTURE -> "Visit a cultural place nearby"
            PlaceCategory.HISTORY -> "Discover a historical place"
            PlaceCategory.NATURE -> "Explore a nearby green area"
            PlaceCategory.FOOD -> "Find a food place around you"
            PlaceCategory.COFFEE -> "Visit a nearby coffee spot"
            PlaceCategory.SPORT -> "Explore a sport-related place"
            PlaceCategory.UNKNOWN -> "Discover an interesting nearby place"
        }
    }

    private fun createChallengeDescription(
        place: Place
    ): String {
        val distance = place.distanceMeters.roundToInt()

        return "Go to ${place.name}, located approximately $distance meters from your current location."
    }

    private fun calculateDistanceMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val startLatRad = Math.toRadians(startLatitude)
        val endLatRad = Math.toRadians(endLatitude)

        val deltaLat = Math.toRadians(endLatitude - startLatitude)
        val deltaLon = Math.toRadians(endLongitude - startLongitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(startLatRad) * cos(endLatRad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    private data class ScoredPlace(
        val place: Place,
        val score: Int,
        val reasons: List<String>
    )

    private data class CategoryScoreResult(
        val category: PlaceCategory,
        val score: Int,
        val reasons: List<String>
    )

    private data class ScoreResult(
        val score: Int,
        val reasons: List<String>
    )
}