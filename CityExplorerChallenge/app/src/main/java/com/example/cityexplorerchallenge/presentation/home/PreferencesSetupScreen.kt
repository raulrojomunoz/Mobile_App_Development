package com.example.cityexplorerchallenge.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cityexplorerchallenge.domain.model.PlaceCategory
import com.example.cityexplorerchallenge.ui.theme.AppBackground
import com.example.cityexplorerchallenge.ui.theme.PrimaryGreen
import com.example.cityexplorerchallenge.ui.theme.SecondaryGold
import com.example.cityexplorerchallenge.ui.theme.TextSecondary

private val visiblePreferenceCategories = listOf(
    PlaceCategory.CULTURE,
    PlaceCategory.HISTORY,
    PlaceCategory.NATURE,
    PlaceCategory.FOOD,
    PlaceCategory.COFFEE,
    PlaceCategory.SPORT
)

private val distanceOptions = listOf(
    800,
    1500,
    2500,
    3500
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesSetupScreen(
    preferredCategories: Set<PlaceCategory>,
    maxDistanceMeters: Int,
    onTogglePreferredCategory: (PlaceCategory) -> Unit,
    onMaxDistanceChange: (Int) -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Before we start",
            color = PrimaryGreen,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tell us what kind of places you prefer. Your first challenge will be generated using this profile.",
            color = TextSecondary
        )

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
                    text = "Choose your interests",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "Select at least one category.",
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category preferences selected by the user.
                Text(
                    text = "Preferred categories",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visiblePreferenceCategories.forEach { category ->
                        PreferenceChip(
                            text = category.displayName,
                            selected = category in preferredCategories,
                            onClick = {
                                onTogglePreferredCategory(category)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Maximum distance preferred for generated challenges.
                Text(
                    text = "Maximum distance",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "Current preference: $maxDistanceMeters m",
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distanceOptions.forEach { distance ->
                        PreferenceChip(
                            text = "$distance m",
                            selected = maxDistanceMeters == distance,
                            onClick = {
                                onMaxDistanceChange(distance)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    onClick = onContinueClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryGold,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PreferenceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                color = if (selected) Color.White else PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PrimaryGreen,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = PrimaryGreen
        )
    )
}