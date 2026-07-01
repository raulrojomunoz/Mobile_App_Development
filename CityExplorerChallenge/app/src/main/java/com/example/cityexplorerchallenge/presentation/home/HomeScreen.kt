package com.example.cityexplorerchallenge.presentation.home

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.ui.theme.AppBackground
import com.example.cityexplorerchallenge.ui.theme.PrimaryGreen
import com.example.cityexplorerchallenge.ui.theme.SecondaryGold
import com.example.cityexplorerchallenge.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    activeChallenge: Challenge?,
    completedToday: Int,
    totalCompleted: Int,
    locationStatus: String,
    placesStatus: String,
    onGenerateNewChallenge: () -> Unit,
    onOpenMapClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val canGenerateNewChallenge =
        activeChallenge == null || activeChallenge.status != ChallengeStatus.ACTIVE

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
            text = "City Explorer",
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        Text(
            text = "Discover nearby places through smart challenges.",
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = locationStatus,
            color = SecondaryGold,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = placesStatus,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActiveChallengeCard(
            activeChallenge = activeChallenge,
            canGenerateNewChallenge = canGenerateNewChallenge,
            onGenerateNewChallenge = onGenerateNewChallenge,
            onOpenMapClick = onOpenMapClick,
            onDetailsClick = onDetailsClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProgressCard(
            completedToday = completedToday,
            totalCompleted = totalCompleted
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun ActiveChallengeCard(
    activeChallenge: Challenge?,
    canGenerateNewChallenge: Boolean,
    onGenerateNewChallenge: () -> Unit,
    onOpenMapClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Active Challenge",
                color = SecondaryGold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = activeChallenge?.title ?: "No challenge available",
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activeChallenge?.description
                    ?: "The app could not generate a challenge yet.",
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Category: ${activeChallenge?.category?.displayName ?: "--"}",
                color = TextSecondary
            )

            Text(
                text = "Distance: ${activeChallenge?.distanceMeters?.roundToInt() ?: "--"} m",
                color = TextSecondary
            )

            Text(
                text = "Status: ${activeChallenge?.status?.displayName ?: "--"}",
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(22.dp))

            // A new challenge can only be generated when there is no active challenge.
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onGenerateNewChallenge,
                enabled = canGenerateNewChallenge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryGold,
                    contentColor = Color.White,
                    disabledContainerColor = SecondaryGold.copy(alpha = 0.45f),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = if (canGenerateNewChallenge) {
                        "Generate New Challenge"
                    } else {
                        "Complete current challenge first"
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenMapClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    )
                ) {
                    Text(text = "Open Map")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onDetailsClick
                ) {
                    Text(
                        text = "Details",
                        color = PrimaryGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(
    completedToday: Int,
    totalCompleted: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Progress",
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Completed today: $completedToday",
                color = TextSecondary
            )

            Text(
                text = "Total completed: $totalCompleted",
                color = TextSecondary
            )
        }
    }
}