package com.example.weather

import android.app.Application
import androidx.room.Room
import com.example.weather.room.model.CitiesDao
import com.example.weather.room.model.CitiesDatabase

class App : Application() {
    // База данных
    private lateinit var db: CitiesDatabase

    init {
        // Сохраняем объект приложения (для Singleton’а)
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        // Строим базу
        Thread {
            db = Room.databaseBuilder(
                applicationContext,
                CitiesDatabase::class.java,
                "education_database"
            )
                .build()
        }.start()
    }

    // Получаем EducationDao для составления запросов
    val citiesDao: CitiesDao
        get() = db.educationDao

    companion object {
        // Получаем объект приложения
        lateinit var instance: App
            private set
    }
}