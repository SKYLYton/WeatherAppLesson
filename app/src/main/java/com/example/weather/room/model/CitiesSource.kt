package com.example.weather.room.model

import android.app.Activity
import com.example.weather.App


class CitiesSource private constructor() {
    private var onDataLoadedListener: OnDataLoadedListener? = null

    interface OnDataLoadedListener {
        fun onCountCities(count: Long)
    }

    fun setOnDataLoadedListener(onDataLoadedListener: OnDataLoadedListener?) {
        this.onDataLoadedListener = onDataLoadedListener
    }

    private val citiesDao: CitiesDao = App.instance.citiesDao
    private val thread: Thread? = null
    var cities: List<City> = emptyList()
        private set

    init {
        Thread { loadCities() }.start()
    }

    fun loadCities() {
        cities = citiesDao.allCities
    }

    fun getCountCities(activity: Activity) {
        if (onDataLoadedListener == null) {
            return
        }
        Thread {
            val count: Long = countCitiesFromDB
            activity.runOnUiThread(Runnable { onDataLoadedListener!!.onCountCities(count) })
        }.start()
    }

    val countCitiesFromDB: Long
        get() = citiesDao.countCities

    fun replaceCity(city: City) {
        Thread {
            citiesDao.deleteCityByName(city.name)
            citiesDao.insertCity(city)
            if (countCitiesFromDB > 3) {
                citiesDao.deleteFirstRow()
            }
            loadCities()
        }.start()
    }

    fun addCities(city: City) {
        Thread {
            citiesDao.insertCity(city)
            loadCities()
        }.start()
    }

    fun updateCities(city: City) {
        Thread {
            citiesDao.updateCity(city)
            loadCities()
        }.start()
    }

    fun removeCities(id: Long) {
        Thread {
            citiesDao.deleteCityById(id)
            loadCities()
        }.start()
    }

    companion object {
        var instance: CitiesSource
            private set

        init {
            instance = CitiesSource()
        }
    }
}