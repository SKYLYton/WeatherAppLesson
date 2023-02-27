package com.example.weather.retrofit.model

import com.example.weather.retrofit.model.weather.WeatherRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeather {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("units") units: String?, @Query("lang") lang: String?,
        @Query("id") cityId: Int, @Query("appid") apiKey: String?
    ): Call<WeatherRequest>

    @GET("data/2.5/weather")
    fun getWeather(
        @Query("units") units: String?, @Query("lang") lang: String?,
        @Query("lat") lat: Double, @Query("lon") lon: Double, @Query("appid") apiKey: String?
    ): Call<WeatherRequest>
}