package com.example.cityexplorerchallenge.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.cityexplorerchallenge.domain.model.UserLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AppLocationManager(
    private val context: Context
) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Checks if the app has either fine or coarse location permission.
    fun hasLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationPermission || coarseLocationPermission
    }

    // Returns the current device location, or a demo location if location is not available.
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationOrFallback(): UserLocation {
        if (!hasLocationPermission()) {
            return DEFAULT_DEMO_LOCATION
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                val currentLocation = if (location != null) {
                    UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isFallback = false
                    )
                } else {
                    DEFAULT_DEMO_LOCATION
                }

                if (continuation.isActive) {
                    continuation.resume(currentLocation)
                }
            }.addOnFailureListener {
                if (continuation.isActive) {
                    continuation.resume(DEFAULT_DEMO_LOCATION)
                }
            }

            // Cancels the location request if the coroutine is cancelled.
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    companion object {
        // Kraków Main Square demo location used when real location is unavailable.
        val DEFAULT_DEMO_LOCATION = UserLocation(
            latitude = 50.06143,
            longitude = 19.93658,
            isFallback = true
        )
    }
}