package com.example.raulrojoicalculatorwithstaticlayouts

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.raulrojoicalculatorwithstaticlayouts.databinding.ActivityMainBinding
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentInput = ""
    private var firstNumber = 0.0
    private var operator = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.displayText = "0"

        setNumberButtons()
        setOperatorButtons()

        //enableEdgeToEdge()
        //setContentView(R.layout.activity_main)
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //    insets
        //}
    }

    private fun setNumberButtons() {
        binding.btn0.setOnClickListener { appendToDisplay("0") }
        binding.btn1.setOnClickListener { appendToDisplay("1") }
        binding.btn2.setOnClickListener { appendToDisplay("2") }
        binding.btn3.setOnClickListener { appendToDisplay("3") }
        binding.btn4.setOnClickListener { appendToDisplay("4") }
        binding.btn5.setOnClickListener { appendToDisplay("5") }
        binding.btn6.setOnClickListener { appendToDisplay("6") }
        binding.btn7.setOnClickListener { appendToDisplay("7") }
        binding.btn8.setOnClickListener { appendToDisplay("8") }
        binding.btn9.setOnClickListener { appendToDisplay("9") }
        binding.btnDot.setOnClickListener { appendDot() }
    }

    private fun setOperatorButtons() {
        binding.btnAdd.setOnClickListener { setOperator("+") }
        binding.btnSub.setOnClickListener { setOperator("-") }
        binding.btnDiv.setOnClickListener { setOperator("/") }
        binding.btnMul.setOnClickListener { setOperator("*") }

        binding.btnClear.setOnClickListener {
            currentInput = ""
            firstNumber = 0.0
            operator = ""
            binding.displayText = "0"
        }

        binding.btnEquals.setOnClickListener { resolveOperation() }
    }

    private fun appendToDisplay(value: String) {
        currentInput = if (currentInput == "0") {
            value
        } else {
            currentInput + value
        }

        binding.displayText = currentInput
    }

    private fun appendDot() {
        if (!currentInput.contains(".")) {
            currentInput = if (currentInput.isEmpty()) {
                "0."
            } else {
                currentInput + "."
            }
            binding.displayText = currentInput
        }
    }

    private fun setOperator(op: String) {
        if (currentInput.isEmpty()) {
            Toast.makeText(this, "Enter a number first", Toast.LENGTH_SHORT).show()
            return
        }

        firstNumber = currentInput.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
            return
        }

        operator = op
        currentInput = ""
    }

    private fun resolveOperation() {
        if (operator.isEmpty()) {
            Toast.makeText(this, "Choose an operation first", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentInput.isEmpty()) {
            Toast.makeText(this, "Enter the second number", Toast.LENGTH_SHORT).show()
            return
        }

        val secondNumber = currentInput.toDoubleOrNull()
        if (secondNumber == null) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
            return
        }

        val result = when (operator) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "*" -> firstNumber * secondNumber
            "/" -> {
                if (secondNumber == 0.0) {
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show()
                    return
                }
                firstNumber / secondNumber
            }
            else -> {
                Toast.makeText(this, "Unknown operation", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (result.isNaN() || result.isInfinite()) {
            Toast.makeText(this, "Invalid operation", Toast.LENGTH_SHORT).show()
            return
        }

        currentInput = if (result % 1.0 == 0.0) {
            result.toInt().toString()
        } else {
            result.toString()
        }

        binding.displayText = currentInput
        operator = ""
    }

}
