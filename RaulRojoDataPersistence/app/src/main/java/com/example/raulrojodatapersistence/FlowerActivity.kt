package com.example.raulrojodatapersistence

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.example.raulrojodatapersistence.ui.theme.RaulRojoDataPersistenceTheme

class FlowerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = buildFlowerDatabase()
        val flowerDAO = getFlowerDAO(db)
        addFlower("Roza","Rose", "Rosa", flowerDAO)
        listAllFlowers(flowerDAO)

    }

    private fun listAllFlowers(flowerDAO: FlowerDAO) {
        val flowers: List<Flower> = flowerDAO.getAll()
        for (flower in flowers) {
            Log.i("FlowerApp", flower.toString())
        }
    }

    private fun addFlower(polishName: String?, englishName: String?, spanishName: String?, flowerDAO: FlowerDAO) {
        val flower = Flower(polishName = polishName, englishName = englishName, spanishName = spanishName)
        flowerDAO.insertAll(flower)
    }


    private fun getFlowerDAO(db: FlowerDatabase): FlowerDAO {
        val flowerDAO = db.flowerDAO()
        return flowerDAO
    }
    private fun buildFlowerDatabase():FlowerDatabase {
        val db = Room.databaseBuilder(
            applicationContext,
            FlowerDatabase::class.java, "flowerdatabase"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration(true)
            .build()
        return db
    }
}
