package com.example.raulrojodatapersistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Flower::class], version = 2)
abstract class FlowerDatabase : RoomDatabase() {
    abstract fun flowerDAO(): FlowerDAO
}