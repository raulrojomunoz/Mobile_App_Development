package com.example.cityexplorerchallenge.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.ui.theme.AppBackground
import com.example.cityexplorerchallenge.ui.theme.ErrorRed
import com.example.cityexplorerchallenge.ui.theme.PrimaryGreen
import com.example.cityexplorerchallenge.ui.theme.SuccessGreen
import com.example.cityexplorerchallenge.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    completedChallenges: List<Challenge>,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Challenge History",
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (completedChallenges.isNotEmpty()) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClearHistory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                )
            ) {
                Text(text = "Clear History")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (completedChallenges.isEmpty()) {
            EmptyHistoryCard()
        } else {
            completedChallenges.forEach { challenge ->
                HistoryChallengeCard(challenge = challenge)

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "No completed challenges yet",
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Completed challenges will appear here.",
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun HistoryChallengeCard(
    challenge: Challenge
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                text = "[${challenge.status.displayName}] ${challenge.title}",
                fontWeight = FontWeight.Bold,
                color = statusColor(challenge.status)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Place: ${challenge.targetPlace.name}",
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Category: ${challenge.category.displayName}",
                color = TextSecondary
            )

            Text(
                text = "Distance: ${challenge.distanceMeters.roundToInt()} m",
                color = TextSecondary
            )

            Text(
                text = "Completed: ${formatDate(challenge.completedAt ?: challenge.createdAt)}",
                color = TextSecondary
            )
        }
    }
}

private fun statusColor(
    status: ChallengeStatus
): Color {
    return when (status) {
        ChallengeStatus.COMPLETED -> SuccessGreen
        ChallengeStatus.EXPIRED -> ErrorRed
        ChallengeStatus.ACTIVE -> TextSecondary
    }
}

private fun formatDate(
    timeMillis: Long
): String {
    val formatter = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    )

    return formatter.format(Date(timeMillis))
}