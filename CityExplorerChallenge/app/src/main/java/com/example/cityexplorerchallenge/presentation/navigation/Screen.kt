package com.example.cityexplorerchallenge.presentation.navigation

sealed class Screen(
    val route: String,
    val title: String
) {
    data object PreferencesSetup : Screen(
        route = "preferences_setup",
        title = "Preferences"
    )

    data object Home : Screen(
        route = "home",
        title = "Home"
    )

    data object Map : Screen(
        route = "map",
        title = "Map"
    )

    data object Details : Screen(
        route = "details",
        title = "Details"
    )

    data object History : Screen(
        route = "history",
        title = "History"
    )

    data object Statistics : Screen(
        route = "statistics",
        title = "Stats"
    )
}