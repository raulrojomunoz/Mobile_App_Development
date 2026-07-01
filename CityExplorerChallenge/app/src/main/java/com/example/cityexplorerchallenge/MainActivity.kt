package com.example.cityexplorerchallenge

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cityexplorerchallenge.presentation.navigation.AppNavigation
import com.example.cityexplorerchallenge.ui.theme.CityExplorerChallengeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT
            )
        )

        setContent {
            CityExplorerChallengeTheme {
                AppNavigation()
            }
        }
    }
}