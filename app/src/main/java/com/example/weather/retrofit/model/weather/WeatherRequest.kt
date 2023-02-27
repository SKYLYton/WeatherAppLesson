package com.example.weather.retrofit.model.weather

data class WeatherRequest (
    val coord: Coord,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val name: String
)