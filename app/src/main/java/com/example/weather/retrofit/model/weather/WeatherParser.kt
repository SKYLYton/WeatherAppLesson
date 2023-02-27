package com.example.weather.retrofit.model.weather

import com.google.gson.Gson

class WeatherParser(private val result: String) {
    val weatherRequest: WeatherRequest

    init {
        val gson = Gson()
        weatherRequest = gson.fromJson(result, WeatherRequest::class.java)
    }
}