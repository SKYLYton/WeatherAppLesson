package com.example.weather.retrofit.model.city

import com.example.weather.retrofit.model.weather.Coord

data class CityModel (
    val id: Int = 0,
    val name: String,
    val state: String,
    val country: String,
    val coord: Coord
)