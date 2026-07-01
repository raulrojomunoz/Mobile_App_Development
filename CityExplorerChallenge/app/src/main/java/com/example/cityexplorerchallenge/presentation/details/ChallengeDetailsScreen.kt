package com.example.cityexplorerchallenge.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.domain.model.PlaceCategory
import com.example.cityexplorerchallenge.domain.model.UserPreferences
import com.example.cityexplorerchallenge.ui.theme.AppBackground
import com.example.cityexplorerchallenge.ui.theme.ErrorRed
import com.example.cityexplorerchallenge.ui.theme.PrimaryGreen
import com.example.cityexplorerchallenge.ui.theme.SecondaryGold
import com.example.cityexplorerchallenge.ui.theme.SuccessGreen
import com.example.cityexplorerchallenge.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ChallengeDetailsScreen(
    activeChallenge: Challenge?,
    locationStatus: String,
    placesStatus: String,
    nearbyPlacesCount: Int,
    userPreferences: UserPreferences,
    demoCompletionMode: Boolean,
    totalCompleted: Int
) {
    val preferredCategoriesText = formatPreferredCategories(userPreferences)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Challenge Details",
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        if (activeChallenge == null) {
            DetailsCard(
                title = "No active challenge"
            ) {
                Text(
                    text = "Generate a challenge first to see the recommendation details.",
                    color = TextSecondary
                )
            }
        } else {
            DetailsCard(
                title = "Active Challenge"
            ) {
                Text(
                    text = activeChallenge.title,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeChallenge.description,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                DetailRow(
                    label = "Target place",
                    value = activeChallenge.targetPlace.name
                )

                DetailRow(
                    label = "Category",
                    value = activeChallenge.category.displayName
                )

                DetailRow(
                    label = "Distance",
                    value = "${activeChallenge.distanceMeters.roundToInt()} m"
                )

                DetailRow(
                    label = "Status",
                    value = activeChallenge.status.displayName,
                    valueColor = statusColor(activeChallenge.status)
                )
            }

            DetailsCard(
                title = "Why this challenge?"
            ) {
                if (activeChallenge.generatedReason.isEmpty()) {
                    Text(
                        text = "No generation reasons available.",
                        color = TextSecondary
                    )
                } else {
                    activeChallenge.generatedReason.forEach { reason ->
                        BulletText(text = reason)
                    }
                }
            }
        }

        DetailsCard(
            title = "Generation input"
        ) {
            DetailRow(
                label = "Location",
                value = locationStatus
            )

            DetailRow(
                label = "Places source",
                value = placesStatus
            )

            DetailRow(
                label = "Nearby candidates",
                value = "$nearbyPlacesCount places"
            )

            DetailRow(
                label = "User preferences",
                value = preferredCategoriesText
            )

            DetailRow(
                label = "Maximum distance",
                value = "${userPreferences.maxDistanceMeters} m"
            )

            DetailRow(
                label = "Completed challenges",
                value = "$totalCompleted"
            )

            DetailRow(
                label = "Completion mode",
                value = if (demoCompletionMode) {
                    "Demo mode"
                } else {
                    "Real distance validation"
                },
                valueColor = if (demoCompletionMode) {
                    SecondaryGold
                } else {
                    PrimaryGreen
                }
            )
        }

        DetailsCard(
            title = "Generation logic"
        ) {
            BulletText(
                text = "The app loads nearby places from OpenStreetMap using the Overpass API."
            )

            BulletText(
                text = "Places are grouped and balanced by category before generating a challenge."
            )

            BulletText(
                text = "User preferences influence the recommendation, but they do not remove variety."
            )

            BulletText(
                text = "The generator checks distance, category, previous completions, recent repetition, and time of day."
            )

            BulletText(
                text = "Completed history is used to avoid repeating the same type of activity too often."
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun DetailsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                color = SecondaryGold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextSecondary
) {
    Text(
        text = label,
        color = PrimaryGreen,
        fontWeight = FontWeight.Bold
    )

    Text(
        modifier = Modifier.padding(bottom = 10.dp),
        text = value,
        color = valueColor
    )
}

@Composable
private fun BulletText(
    text: String
) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = "• $text",
        color = TextSecondary
    )
}

private fun statusColor(
    status: ChallengeStatus
): Color {
    return when (status) {
        ChallengeStatus.ACTIVE -> SecondaryGold
        ChallengeStatus.COMPLETED -> SuccessGreen
        ChallengeStatus.EXPIRED -> ErrorRed
    }
}

private fun formatPreferredCategories(
    userPreferences: UserPreferences
): String {
    val categoryOrder = listOf(
        PlaceCategory.CULTURE,
        PlaceCategory.HISTORY,
        PlaceCategory.NATURE,
        PlaceCategory.FOOD,
        PlaceCategory.COFFEE,
        PlaceCategory.SPORT
    )

    return categoryOrder
        .filter { category ->
            category in userPreferences.preferredCategories
        }
        .joinToString(separator = ", ") { category ->
            category.displayName
        }
        .ifBlank {
            "No preferences selected"
        }
}