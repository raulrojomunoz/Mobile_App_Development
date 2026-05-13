package com.example.raulrojolayoutsandactions

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.raulrojolayoutsandactions.databinding.ActivityMainBinding
import kotlin.math.log

class MainActivity : AppCompatActivity() {
//    private lateinit var binding:
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

        setContentView(R.layout.activity_actions)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.action)){ v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setWriteToLogCatButtonListener()
    }

    private fun setWriteToLogCatButtonListener() {
        binding.   .setOnClickListener {
            Log.i("MyAppList", "Message From the listener")
        }
    }

    fun writeToLogCat(view: View) {
        Log.i("MyApp","Message from my App")
    }
}