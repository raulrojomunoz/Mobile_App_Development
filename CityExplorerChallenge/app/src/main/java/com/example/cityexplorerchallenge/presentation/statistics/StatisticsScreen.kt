package com.example.cityexplorerchallenge.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.LinearProgressIndicator
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val statisticsCategoryOrder = listOf(
    PlaceCategory.CULTURE,
    PlaceCategory.HISTORY,
    PlaceCategory.NATURE,
    PlaceCategory.SPORT,
    PlaceCategory.COFFEE,
    PlaceCategory.FOOD
)

@Composable
fun StatisticsScreen(
    completedChallenges: List<Challenge>,
    userPreferences: UserPreferences
) {
    val completedOnly = completedChallenges.filter { challenge ->
        challenge.status == ChallengeStatus.COMPLETED
    }

    val totalCompleted = completedOnly.size

    val completedToday = completedOnly.count { challenge ->
        isToday(challenge.completedAt ?: challenge.createdAt)
    }

    val totalDistanceKm = completedOnly
        .sumOf { challenge ->
            challenge.distanceMeters
        } / 1000.0

    val categoryCounts = completedOnly
        .groupingBy { challenge ->
            challenge.category
        }
        .eachCount()

    val mostExploredCategory = categoryCounts
        .maxByOrNull { entry ->
            entry.value
        }
        ?.key

    val usedCategoriesCount = statisticsCategoryOrder.count { category ->
        (categoryCounts[category] ?: 0) > 0
    }

    val diversityScore =
        ((usedCategoriesCount.toDouble() / statisticsCategoryOrder.size.toDouble()) * 100)
            .roundToInt()

    val maxCategoryCount = categoryCounts.values.maxOrNull() ?: 0

    val categoryDominancePercentage = if (totalCompleted > 0) {
        ((maxCategoryCount.toDouble() / totalCompleted.toDouble()) * 100).roundToInt()
    } else {
        0
    }

    val balanceStatus = when {
        totalCompleted == 0 -> "No data yet"
        categoryDominancePercentage >= 60 -> "Needs more variety"
        diversityScore >= 70 -> "Good category diversity"
        else -> "Moderate diversity"
    }

    val balanceColor = when (balanceStatus) {
        "Good category diversity" -> SuccessGreen
        "Needs more variety" -> ErrorRed
        else -> SecondaryGold
    }

    val preferredCompleted = completedOnly.count { challenge ->
        challenge.category in userPreferences.preferredCategories
    }

    val outsidePreferencesCompleted = totalCompleted - preferredCompleted

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
            text = "Statistics",
            color = PrimaryGreen,
            fontWeight = FontWeight.Bold
        )

        StatsCard(title = "Explorer Summary") {
            StatRow(
                label = "Total completed challenges",
                value = totalCompleted.toString()
            )

            StatRow(
                label = "Completed today",
                value = completedToday.toString()
            )

            StatRow(
                label = "Total explored distance",
                value = String.format(Locale.getDefault(), "%.2f km", totalDistanceKm)
            )

            StatRow(
                label = "Most explored category",
                value = mostExploredCategory?.displayName ?: "None"
            )
        }

        StatsCard(title = "Category Balance") {
            Text(
                text = "This section evaluates if the challenge generator is keeping the experience varied or repeating one category too often.",
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            StatRow(
                label = "Diversity score",
                value = "$diversityScore%"
            )

            StatRow(
                label = "Balance status",
                value = balanceStatus,
                valueColor = balanceColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            statisticsCategoryOrder.forEach { category ->
                val count = categoryCounts[category] ?: 0

                val progress = if (totalCompleted > 0) {
                    count.toFloat() / totalCompleted.toFloat()
                } else {
                    0f
                }

                CategoryProgressRow(
                    categoryName = category.displayName,
                    count = count,
                    total = totalCompleted,
                    progress = progress
                )
            }
        }

        StatsCard(title = "Preferences vs Exploration") {
            Text(
                text = "User preferences guide the recommendations, but the app can also suggest other categories to maintain variety.",
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            StatRow(
                label = "Selected preferences",
                value = formatPreferredCategories(userPreferences)
            )

            StatRow(
                label = "Completed from preferences",
                value = preferredCompleted.toString()
            )

            StatRow(
                label = "Completed outside preferences",
                value = outsidePreferencesCompleted.toString()
            )
        }

        StatsCard(title = "Generator Evaluation") {
            BulletText(
                text = "A higher diversity score means more categories have been explored."
            )

            BulletText(
                text = "If one category dominates the history, the generator should prioritize less explored categories."
            )

            BulletText(
                text = "Completed history is used to avoid repeating the same type of activity continuously."
            )

            BulletText(
                text = "This makes the recommendation system adaptive instead of using a fixed list."
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun StatsCard(
    title: String,
    content: @Composable () -> Unit
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
private fun StatRow(
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
private fun CategoryProgressRow(
    categoryName: String,
    count: Int,
    total: Int,
    progress: Float
) {
    Column(
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryName,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (total > 0) {
                    "$count / $total"
                } else {
                    "0 / 0"
                },
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = {
                progress.coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = PrimaryGreen,
            trackColor = AppBackground
        )
    }
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

private fun formatPreferredCategories(
    userPreferences: UserPreferences
): String {
    return statisticsCategoryOrder
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

private fun isToday(
    timeMillis: Long
): Boolean {
    val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = formatter.format(Date())
    val targetDate = formatter.format(Date(timeMillis))

    return today == targetDate
}