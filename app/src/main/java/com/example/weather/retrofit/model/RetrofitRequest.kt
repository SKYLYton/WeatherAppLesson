package com.example.weather.retrofit.model

import com.example.weather.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitRequest private constructor() {
    val openWeather: OpenWeather

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(Constants.URL_CONNECTION)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        openWeather = retrofit.create(OpenWeather::class.java)
    }

    companion object {
        val instance: RetrofitRequest = RetrofitRequest()
    }
}