package com.example.raulrojobmicalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raulrojobmicalculator.ui.theme.RaulRojoBMICalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BMIScreen()
        }
    }
}

@Composable
fun BMIScreen() {
    val weight: MutableState<String> = remember { mutableStateOf("") }
    val height: MutableState<String> = remember { mutableStateOf("") }
    val bmiResult: MutableState<String> = remember { mutableStateOf("") }
    val diagnosis: MutableState<String> = remember { mutableStateOf("") }

    val diagnosisColor = when (diagnosis.value) {
        "Underweight" -> Color(0xFF42A5F5)
        "Healthy" -> Color(0xFF2E7D32)
        "Overweight" -> Color(0xFFFF9800)
        "Obesity" -> Color(0xFFD32F2F)
        "Please enter valid values" -> Color.Red
        else -> Color.Black
    }

    val diagnosisIcon = when (diagnosis.value) {
        "Underweight" -> "😟"
        "Healthy" -> "😊"
        "Overweight" -> "😐"
        "Obesity" -> "🚨"
        "Please enter valid values" -> "⚠️"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "BMI Calculator",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = weight.value,
            onValueChange = {
                weight.value = it
                bmiResult.value = ""
                diagnosis.value = ""
            },
            placeholder = { Text(text = "Type your weight in KG") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = height.value,
            onValueChange = {
                height.value = it
                bmiResult.value = ""
                diagnosis.value = ""
            },
            placeholder = { Text(text = "Type your height in M") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val weightValue = weight.value.toDoubleOrNull()
            val heightValue = height.value.toDoubleOrNull()

            if (weightValue == null || heightValue == null || weightValue <= 0.0 || heightValue <= 0.0) {
                bmiResult.value = ""
                diagnosis.value = "Please enter valid values"
            } else {
                val bmi = calculateBMI(weightValue, heightValue)
                bmiResult.value = "%.2f".format(bmi)
                diagnosis.value = getDiagnosis(bmi)
            }
        }) {
            Text(text = "Calculate my BMI")
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (bmiResult.value.isNotEmpty()) {
            Text(
                text = "Your BMI is",
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bmiResult.value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (diagnosis.value.isNotEmpty()) {
            Text(
                text = diagnosisIcon,
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = diagnosis.value,
                color = diagnosisColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun calculateBMI(weight: Double, height: Double): Double {
    return weight / (height * height)
}

fun getDiagnosis(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Underweight"
        bmi <= 24.9 -> "Healthy"
        bmi <= 29.9 -> "Overweight"
        else -> "Obesity"
    }
}