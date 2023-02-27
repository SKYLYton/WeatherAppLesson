package com.example.weather.fragments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Parcel(
    var cityName: String? = null,
    var cityId: Int = 0,
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var isCord: Boolean = false
) : Parcelable