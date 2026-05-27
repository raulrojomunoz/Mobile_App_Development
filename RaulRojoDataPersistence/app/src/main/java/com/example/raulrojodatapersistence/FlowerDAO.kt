package com.example.raulrojodatapersistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FlowerDAO {
    @Insert
    fun insertAll(flower: Flower)

    @Delete
    fun delete(flower: Flower)

    @Query("SELECT * FROM flower")
    fun getAll(): List<Flower>

    @Query("SELECT * FROM flower WHERE flowerID IN (:flowerIDs)")
    fun loadAllByIds(flowerIDs: IntArray): List<Flower>
}
