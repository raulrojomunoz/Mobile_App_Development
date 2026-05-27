package com.example.raulrojodatapersistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Flower(
    @PrimaryKey(autoGenerate = true) val flowerID: Int,
    @ColumnInfo(name = "polish_name") val polishName: String?,
    @ColumnInfo(name = "english_name") val englishName: String?,
    @ColumnInfo(name = "spanish_name") val spanishName: String?
){
    constructor(polishName: String?, englishName: String?, spanishName: String?) :
            this(0, polishName, englishName, spanishName)
}
