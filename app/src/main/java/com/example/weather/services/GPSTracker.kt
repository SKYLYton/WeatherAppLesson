package com.example.weather.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng

class GPSTracker(private val mContext: Context) {

    var onLocationUpdate: ((Location, LatLng) -> Unit)? = null


    private var canGetLocation = false
    private var location: Location? = null
    var latitude = 0.0
        private set
    var longitude = 0.0
        private set
    private val locationManager: LocationManager?
    fun getLocation(): Location {
        if (location == null) {
            init()
        }
        return location!!
    }

    fun init() {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        val provider = locationManager!!.getBestProvider(criteria, true) ?: return
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(
            provider,
            MIN_TIME_BW_UPDATES,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
            locationListener
        )
        canGetLocation = true
    }

    private val locationListener = LocationListener { location ->
        this@GPSTracker.location = location
        latitude = location.latitude
        longitude = location.longitude
        onLocationUpdate?.invoke(location, LatLng(latitude, longitude))
    }

    init {
        locationManager = mContext
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        init()
    }

    fun stopUsingGPS() {
        locationManager?.removeUpdates(locationListener)
        canGetLocation = false
    }

    val isGpsEnable: Boolean
        get() = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    val isLocationKnown: Boolean
        get() = location != null


    fun canGetLocation(): Boolean {
        return canGetLocation
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 20
        private const val MIN_TIME_BW_UPDATES: Long = 5000
    }
}