package com.example.cityexplorerchallenge.presentation.navigation

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cityexplorerchallenge.data.local.AppDatabase
import com.example.cityexplorerchallenge.data.local.toDomain
import com.example.cityexplorerchallenge.data.local.toEntity
import com.example.cityexplorerchallenge.data.remote.PlaceRemoteDataSource
import com.example.cityexplorerchallenge.domain.engine.ChallengeGenerator
import com.example.cityexplorerchallenge.domain.model.Challenge
import com.example.cityexplorerchallenge.domain.model.ChallengeStatus
import com.example.cityexplorerchallenge.domain.model.Place
import com.example.cityexplorerchallenge.domain.model.PlaceCategory
import com.example.cityexplorerchallenge.domain.model.UserPreferences
import com.example.cityexplorerchallenge.location.AppLocationManager
import com.example.cityexplorerchallenge.presentation.details.ChallengeDetailsScreen
import com.example.cityexplorerchallenge.presentation.history.HistoryScreen
import com.example.cityexplorerchallenge.presentation.home.HomeScreen
import com.example.cityexplorerchallenge.presentation.home.PreferencesSetupScreen
import com.example.cityexplorerchallenge.presentation.map.MapScreen
import com.example.cityexplorerchallenge.presentation.statistics.StatisticsScreen
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // App-level UI state.
    var demoCompletionMode by remember {
        mutableStateOf(true)
    }

    var completionMessage by remember {
        mutableStateOf<String?>(null)
    }

    var activeChallenge by remember {
        mutableStateOf<Challenge?>(null)
    }

    // Local preferences storage.
    val preferencesStorage = remember {
        context.getSharedPreferences(
            "city_explorer_user_preferences",
            Context.MODE_PRIVATE
        )
    }

    var hasCompletedPreferencesSetup by remember {
        mutableStateOf(
            preferencesStorage.getBoolean("hasCompletedPreferencesSetup", false)
        )
    }

    var preferredCategories by remember {
        mutableStateOf(
            preferencesStorage
                .getStringSet("preferredCategories", null)
                ?.mapNotNull { categoryName ->
                    runCatching {
                        PlaceCategory.valueOf(categoryName)
                    }.getOrNull()
                }
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }
                ?: setOf(
                    PlaceCategory.CULTURE,
                    PlaceCategory.HISTORY,
                    PlaceCategory.NATURE
                )
        )
    }

    var maxDistanceMeters by remember {
        mutableStateOf(
            preferencesStorage.getInt("maxDistanceMeters", 1500)
        )
    }

    val savePreferences = {
        preferencesStorage.edit()
            .putBoolean("hasCompletedPreferencesSetup", true)
            .putStringSet(
                "preferredCategories",
                preferredCategories.map { it.name }.toSet()
            )
            .putInt("maxDistanceMeters", maxDistanceMeters)
            .apply()

        hasCompletedPreferencesSetup = true
    }

    val userPreferences = UserPreferences(
        preferredCategories = preferredCategories,
        maxDistanceMeters = maxDistanceMeters
    )

    // Location setup.
    val appLocationManager = remember {
        AppLocationManager(context)
    }

    var userLocation by remember {
        mutableStateOf(AppLocationManager.DEFAULT_DEMO_LOCATION)
    }

    var locationStatus by remember {
        mutableStateOf("Using demo location")
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        coroutineScope.launch {
            userLocation = if (hasLocationPermission) {
                appLocationManager.getCurrentLocationOrFallback()
            } else {
                AppLocationManager.DEFAULT_DEMO_LOCATION
            }

            locationStatus = if (userLocation.isFallback) {
                "Using demo location"
            } else {
                "Using your current location"
            }

            activeChallenge = null
        }
    }

    LaunchedEffect(Unit) {
        if (appLocationManager.hasLocationPermission()) {
            userLocation = appLocationManager.getCurrentLocationOrFallback()

            locationStatus = if (userLocation.isFallback) {
                "Using demo location"
            } else {
                "Using your current location"
            }

            activeChallenge = null
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Local database setup.
    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val challengeDao = remember {
        database.challengeDao()
    }

    val completedChallenges by challengeDao
        .getAllChallenges()
        .map { entities ->
            entities.map { it.toDomain() }
        }
        .collectAsState(initial = emptyList())

    val completedToday = completedChallenges.count { challenge ->
        isToday(challenge.completedAt ?: challenge.createdAt)
    }

    // Challenge generation and remote places setup.
    val challengeGenerator = remember {
        ChallengeGenerator()
    }

    val placeRemoteDataSource = remember {
        PlaceRemoteDataSource()
    }

    val demoPlaces = remember {
        listOf(
            Place(
                id = "place_1",
                name = "Historical Museum",
                category = PlaceCategory.HISTORY,
                latitude = 50.06210,
                longitude = 19.93720,
                distanceMeters = 0.0
            ),
            Place(
                id = "place_2",
                name = "City Park",
                category = PlaceCategory.NATURE,
                latitude = 50.06400,
                longitude = 19.93500,
                distanceMeters = 0.0
            ),
            Place(
                id = "place_3",
                name = "Local Coffee Spot",
                category = PlaceCategory.COFFEE,
                latitude = 50.06050,
                longitude = 19.93480,
                distanceMeters = 0.0
            ),
            Place(
                id = "place_4",
                name = "Cultural Center",
                category = PlaceCategory.CULTURE,
                latitude = 50.05880,
                longitude = 19.93890,
                distanceMeters = 0.0
            ),
            Place(
                id = "place_5",
                name = "Food Market",
                category = PlaceCategory.FOOD,
                latitude = 50.06600,
                longitude = 19.94000,
                distanceMeters = 0.0
            )
        )
    }

    var nearbyPlaces by remember {
        mutableStateOf<List<Place>>(emptyList())
    }

    var placesStatus by remember {
        mutableStateOf("Loading nearby places...")
    }

    // Loads real nearby places from OpenStreetMap through the Overpass API.
    LaunchedEffect(userLocation, maxDistanceMeters) {
        placesStatus = "Loading nearby places..."

        val apiRadius = maxDistanceMeters.coerceAtLeast(2500)

        val placesFromApi = placeRemoteDataSource.getNearbyPlaces(
            userLocation = userLocation,
            radiusMeters = apiRadius
        )

        if (placesFromApi.isNotEmpty()) {
            nearbyPlaces = placesFromApi
            placesStatus = "Nearby places loaded: ${placesFromApi.size}"
        } else {
            nearbyPlaces = demoPlaces
            placesStatus = "Using demo places"
        }

        activeChallenge = null
        completionMessage = null
    }

    // Generates the first challenge only after preferences and nearby places are ready.
    LaunchedEffect(
        userLocation,
        completedChallenges.size,
        nearbyPlaces.size,
        hasCompletedPreferencesSetup,
        preferredCategories,
        maxDistanceMeters
    ) {
        if (
            hasCompletedPreferencesSetup &&
            activeChallenge == null &&
            nearbyPlaces.isNotEmpty()
        ) {
            activeChallenge = challengeGenerator.generateChallenge(
                userLocation = userLocation,
                nearbyPlaces = nearbyPlaces.ifEmpty { demoPlaces },
                completedChallenges = completedChallenges,
                userPreferences = userPreferences
            )
        }
    }

    val bottomScreens = listOf(
        Screen.Home,
        Screen.Map,
        Screen.History,
        Screen.Statistics
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color(0xFFF8F7F3),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = bottomScreens.any { screen ->
                currentDestination?.hierarchy?.any {
                    it.route == screen.route
                } == true
            }

            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp)),
                        containerColor = Color(0xFF101A33),
                        tonalElevation = 0.dp
                    ) {
                        bottomScreens.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route
                            } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }

                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = when (screen) {
                                            Screen.Home -> Icons.Outlined.Home
                                            Screen.Map -> Icons.Outlined.Map
                                            Screen.History -> Icons.Outlined.MenuBook
                                            Screen.Statistics -> Icons.Outlined.BarChart
                                            Screen.Details -> Icons.Outlined.Description
                                            Screen.PreferencesSetup -> Icons.Outlined.Home
                                        },
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(text = screen.title)
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = Color.White.copy(alpha = 0.75f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.75f),
                                    indicatorColor = Color(0xFF384261)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (hasCompletedPreferencesSetup) {
                Screen.Home.route
            } else {
                Screen.PreferencesSetup.route
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.PreferencesSetup.route) {
                PreferencesSetupScreen(
                    preferredCategories = preferredCategories,
                    maxDistanceMeters = maxDistanceMeters,
                    onTogglePreferredCategory = { category ->
                        preferredCategories = if (category in preferredCategories) {
                            if (preferredCategories.size > 1) {
                                preferredCategories - category
                            } else {
                                preferredCategories
                            }
                        } else {
                            preferredCategories + category
                        }
                    },
                    onMaxDistanceChange = { distance ->
                        maxDistanceMeters = distance
                    },
                    onContinueClick = {
                        savePreferences()

                        completionMessage = null

                        activeChallenge = challengeGenerator.generateChallenge(
                            userLocation = userLocation,
                            nearbyPlaces = nearbyPlaces.ifEmpty { demoPlaces },
                            completedChallenges = completedChallenges,
                            userPreferences = userPreferences
                        )

                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.PreferencesSetup.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    activeChallenge = activeChallenge,
                    completedToday = completedToday,
                    totalCompleted = completedChallenges.size,
                    locationStatus = locationStatus,
                    placesStatus = placesStatus,
                    onGenerateNewChallenge = {
                        if (activeChallenge?.status != ChallengeStatus.ACTIVE) {
                            completionMessage = null

                            val previousChallenge = activeChallenge

                            activeChallenge = challengeGenerator.generateChallenge(
                                userLocation = userLocation,
                                nearbyPlaces = nearbyPlaces.ifEmpty { demoPlaces },
                                completedChallenges = completedChallenges,
                                userPreferences = userPreferences,
                                previousActiveChallenge = previousChallenge
                            )
                        }
                    },
                    onOpenMapClick = {
                        navController.navigate(Screen.Map.route)
                    },
                    onDetailsClick = {
                        navController.navigate(Screen.Details.route)
                    }
                )
            }

            composable(Screen.Map.route) {
                MapScreen(
                    activeChallenge = activeChallenge,
                    userLocation = userLocation,
                    completionMessage = completionMessage,
                    demoCompletionMode = demoCompletionMode,
                    onDemoModeChange = { enabled ->
                        demoCompletionMode = enabled
                        completionMessage = null
                    },
                    onCheckCompletion = {
                        activeChallenge?.let { challenge ->
                            val completionThresholdMeters = 150
                            val canCompleteChallenge =
                                challenge.distanceMeters <= completionThresholdMeters ||
                                        demoCompletionMode

                            if (canCompleteChallenge) {
                                val completedChallenge = challenge.copy(
                                    status = ChallengeStatus.COMPLETED,
                                    completedAt = System.currentTimeMillis()
                                )

                                activeChallenge = completedChallenge

                                completionMessage = if (demoCompletionMode) {
                                    "Demo mode: challenge completed for presentation."
                                } else {
                                    "Great job! This challenge has been completed."
                                }

                                coroutineScope.launch {
                                    challengeDao.insertChallenge(
                                        completedChallenge.toEntity()
                                    )
                                }
                            } else {
                                completionMessage =
                                    "Real mode: you are still ${challenge.distanceMeters.roundToInt()} m away from the target."
                            }
                        }
                    }
                )
            }

            composable(Screen.Details.route) {
                ChallengeDetailsScreen(
                    activeChallenge = activeChallenge,
                    locationStatus = locationStatus,
                    placesStatus = placesStatus,
                    nearbyPlacesCount = nearbyPlaces.size,
                    userPreferences = userPreferences,
                    demoCompletionMode = demoCompletionMode,
                    totalCompleted = completedChallenges.size
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    completedChallenges = completedChallenges,
                    onClearHistory = {
                        coroutineScope.launch {
                            challengeDao.clearHistory()
                            completionMessage = null
                            activeChallenge = null
                        }
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    completedChallenges = completedChallenges,
                    userPreferences = userPreferences
                )
            }
        }
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