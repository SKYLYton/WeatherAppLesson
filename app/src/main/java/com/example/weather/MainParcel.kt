package com.example.weather

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MainParcel(
    var isDark: Boolean,
    var currentTab: String,
    var currentCountry: String,
) : Parcelable