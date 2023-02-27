package com.example.weather

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.databinding.ActivityMapsBinding
import com.example.weather.services.GPSTracker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var mMap: GoogleMap
    private var gpsTracker: GPSTracker? = null
    private var currentLocation: Marker? = null
    private var accessLocation = false
    private var isGPSLocationOn = true

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment: SupportMapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initGPSTracker()
        initControls()
        accessLocation = checkPermission()
    }

    private fun initControls() {
        binding.floatingActionButtonChoose.setOnClickListener(View.OnClickListener {
            currentLocation?.let { currentLocation ->
                val intent = Intent()
                intent.putExtra(Constants.EXTRA_LAT, currentLocation.position.latitude)
                intent.putExtra(Constants.EXTRA_LNG, currentLocation.position.longitude)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
        binding.floatingActionButtonLocation.setOnClickListener(View.OnClickListener {
            gpsTracker?.let { gpsTracker ->
                isGPSLocationOn = true
                if (!gpsTracker.isLocationKnown) {
                    mMap.clear()
                    return@OnClickListener
                }
                val latLng = LatLng(gpsTracker.latitude, gpsTracker.longitude)
                binding.floatingActionButtonChoose.visibility = View.VISIBLE
                binding.floatingActionButtonLocation.visibility = View.GONE
                if (currentLocation == null) {
                    currentLocation = mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    )
                } else {
                    currentLocation!!.position = latLng
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
            }
        })
    }

    private fun initGPSTracker() {
        gpsTracker = GPSTracker(applicationContext)
        gpsTracker?.onLocationUpdate = { location: Location, latLng: LatLng ->
            if (isGPSLocationOn) {
                if (currentLocation == null) {
                    binding.floatingActionButtonChoose.visibility = View.VISIBLE
                    binding.floatingActionButtonLocation.visibility = View.GONE
                    currentLocation = mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    )
                } else {
                    currentLocation!!.position = latLng
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener { latLng ->
            binding.floatingActionButtonLocation.visibility = View.VISIBLE
            isGPSLocationOn = false
            if (currentLocation == null) {
                currentLocation = mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                )
            } else {
                currentLocation!!.setPosition(latLng)
            }
        }
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                binding.floatingActionButtonLocation.visibility = View.VISIBLE
                isGPSLocationOn = false
            }

            override fun onMarkerDrag(marker: Marker) {/* no-op */}
            override fun onMarkerDragEnd(marker: Marker) {/* no-op */}
        })
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSIONS_REQUEST_ACCESS_LOCATION
        )
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (grantResults.size == 2 &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)
            ) {
                gpsTracker?.init()
                accessLocation = true
            }
        }
    }

    protected override fun onStop() {
        super.onStop()
        gpsTracker?.stopUsingGPS()
    }

    protected override fun onResume() {
        super.onResume()
        if (gpsTracker?.canGetLocation() == false) {
            gpsTracker?.init()
        }
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_ACCESS_LOCATION = 0
    }
}