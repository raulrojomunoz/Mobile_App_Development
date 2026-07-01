package com.example.cityexplorerchallenge.presentation.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.domain.model.UserLocation
import com.example.cityexplorerchallenge.ui.theme.AppBackground
import com.example.cityexplorerchallenge.ui.theme.PrimaryGreen
import com.example.cityexplorerchallenge.ui.theme.SecondaryGold
import com.example.cityexplorerchallenge.ui.theme.SuccessGreen
import com.example.cityexplorerchallenge.ui.theme.TextSecondary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    activeChallenge: Challenge?,
    userLocation: UserLocation,
    completionMessage: String?,
    demoCompletionMode: Boolean,
    onDemoModeChange: (Boolean) -> Unit,
    onCheckCompletion: () -> Unit
) {
    val isCompleted = activeChallenge?.status == ChallengeStatus.COMPLETED

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
            text = "Map",
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )

        MapCard(
            activeChallenge = activeChallenge,
            userLocation = userLocation
        )

        TargetCard(
            activeChallenge = activeChallenge,
            completionMessage = completionMessage,
            demoCompletionMode = demoCompletionMode,
            isCompleted = isCompleted,
            onDemoModeChange = onDemoModeChange,
            onCheckCompletion = onCheckCompletion
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun MapCard(
    activeChallenge: Challenge?,
    userLocation: UserLocation
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ChallengeOsmMap(
                modifier = Modifier.fillMaxSize(),
                activeChallenge = activeChallenge,
                userLocation = userLocation
            )
        }
    }
}

@Composable
private fun TargetCard(
    activeChallenge: Challenge?,
    completionMessage: String?,
    demoCompletionMode: Boolean,
    isCompleted: Boolean,
    onDemoModeChange: (Boolean) -> Unit,
    onCheckCompletion: () -> Unit
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
                text = "Target",
                fontWeight = FontWeight.Bold,
                color = SecondaryGold
            )

            Text(
                text = activeChallenge?.targetPlace?.name ?: "No active challenge",
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )

            Text(
                text = "Challenge: ${activeChallenge?.title ?: "--"}",
                color = TextSecondary
            )

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
                color = if (isCompleted) SuccessGreen else TextSecondary,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
            )

            // Demo Mode allows completion during presentation without physically reaching the target.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Demo Mode",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (demoCompletionMode) {
                            "Enabled for presentation"
                        } else {
                            "Real distance validation"
                        },
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = demoCompletionMode,
                    onCheckedChange = onDemoModeChange,
                    enabled = !isCompleted
                )
            }

            if (!demoCompletionMode && !isCompleted) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Real mode: you must be close to the target to complete it.",
                    color = SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isCompleted) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "Great job! This challenge has been completed.",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = onCheckCompletion,
                enabled = activeChallenge != null && !isCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    disabledContainerColor = SuccessGreen,
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = if (isCompleted) {
                        "Challenge Completed"
                    } else {
                        "Check Completion"
                    }
                )
            }

            if (completionMessage != null) {
                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = completionMessage,
                    color = if (isCompleted) SuccessGreen else SecondaryGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChallengeOsmMap(
    modifier: Modifier = Modifier,
    activeChallenge: Challenge?,
    userLocation: UserLocation
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        Configuration.getInstance().userAgentValue =
            "CityExplorerChallenge/1.0 Android Student Project"

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()

        onDispose {
            mapView.onPause()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView
        },
        update = { map ->
            val userPoint = GeoPoint(
                userLocation.latitude,
                userLocation.longitude
            )

            val targetPoint = activeChallenge?.let { challenge ->
                GeoPoint(
                    challenge.targetPlace.latitude,
                    challenge.targetPlace.longitude
                )
            }

            map.overlays.clear()

            // User location marker.
            val userMarker = Marker(map).apply {
                position = userPoint
                title = "Your location"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            map.overlays.add(userMarker)

            if (targetPoint != null && activeChallenge != null) {
                // Target marker and route line.
                val targetMarker = Marker(map).apply {
                    position = targetPoint
                    title = activeChallenge.targetPlace.name
                    snippet = activeChallenge.title
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }

                val routeLine = Polyline().apply {
                    setPoints(listOf(userPoint, targetPoint))
                    outlinePaint.strokeWidth = 6f
                }

                map.overlays.add(routeLine)
                map.overlays.add(targetMarker)

                map.controller.setCenter(targetPoint)
                map.controller.setZoom(15.0)
            } else {
                map.controller.setCenter(userPoint)
                map.controller.setZoom(15.0)
            }

            map.invalidate()
        }
    )
}