package com.example.weather.room.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [City::class], version = 1)
abstract class CitiesDatabase : RoomDatabase() {
    abstract val educationDao: CitiesDao
}