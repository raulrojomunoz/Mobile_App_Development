package com.example.lab_1raul

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab_1raul.ui.theme.Lab1RaulTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyTexts()
        }
    }
}

@Composable
private fun MyTexts(){
    Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 16.dp, end = 16.dp),
    verticalArrangement = Arrangement.Bottom,
    horizontalAlignment = Alignment.End
) {
    Text(
        text = "Hello There",
        color = Color.Red,
        fontSize = 20.sp,
        letterSpacing = 3.sp
    )
    Text(
        text = "I'm doing so great",
        color = Color.Blue,
        fontSize = 20.sp,
        letterSpacing = 3.sp
    )
    Text(
        text = "And android is so cool",
        color = Color.Magenta,
        fontSize = 20.sp,
        letterSpacing = 3.sp
    )
}
}