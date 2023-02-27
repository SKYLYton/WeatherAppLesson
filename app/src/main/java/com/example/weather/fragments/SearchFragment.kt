package com.example.weather.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.Constants
import com.example.weather.MainActivity
import com.example.weather.MapsActivity
import com.example.weather.R
import com.example.weather.adapters.CitiesWeatherAdapter
import com.example.weather.adapters.CityItem
import com.example.weather.databinding.FragmentCountryBinding
import com.example.weather.databinding.FragmentSearchBinding
import com.example.weather.retrofit.model.city.CitiesModel
import com.example.weather.retrofit.model.city.CityModel
import com.example.weather.room.model.CitiesSource
import com.example.weather.room.model.City
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding
        get() = _binding!!

    private val cityItems: MutableList<CityItem> = ArrayList<CityItem>()
    private lateinit var mainActivity: MainActivity
    private lateinit var citiesWeatherAdapter: CitiesWeatherAdapter
    private var cityModelList: List<CityModel> = emptyList()
    private lateinit var editor: SharedPreferences.Editor
    private var thread: Thread? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        init()
        return binding.root
    }

    private fun init() {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(Constants.MAIN_SHARED_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        citiesWeatherAdapter = initDaysWeatherList()
        binding.editTextCountry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                cityItems.clear()
                for (i in cityModelList.indices) {
                    if (cityModelList[i].country == Constants.LANGUAGE_RU &&
                        cityModelList[i].name.toLowerCase(Locale.ROOT).contains(
                            s.toString().lowercase(
                                Locale.getDefault()
                            )
                        )
                    ) {
                        cityItems.add(
                            CityItem(
                                cityModelList[i].name,
                                cityModelList[i].id
                            )
                        )
                    }
                }
                citiesWeatherAdapter.notifyDataSetChanged()
            }
        })
        binding.floatingActionButton.setOnClickListener { v: View? -> mainActivity.onBackPressed() }
        binding.floatingActionButtonMap.setOnClickListener {
            startActivityForResult(
                Intent(
                    context, MapsActivity::class.java
                ), RESULT_CHOOSE_LOCATION_CODE
            )
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initDaysWeatherList(): CitiesWeatherAdapter {
        val itemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        requireContext().getDrawable(R.drawable.separator)?.let { itemDecoration.setDrawable(it) }
        binding.recyclerView.addItemDecoration(itemDecoration)
        val citiesWeatherAdapter = CitiesWeatherAdapter(cityItems)
        val list: List<City> = CitiesSource.instance.cities

        thread = Thread {
            val gson = Gson()
            cityModelList = gson.fromJson(cities, CitiesModel::class.java).cities.filter { it.country == "RU" }

            list.forEach {city ->
                cityItems.add(CityItem(city.name, cityModelList.find { it.name == city.name }?.id ?: 0))
            }
            
            mainActivity.runOnUiThread {
                binding.recyclerView.adapter =
                    citiesWeatherAdapter
            }
        }
        thread!!.start()
        citiesWeatherAdapter.onItemSelect = { cityId, cityName ->
            editor.putString(Constants.SHARED_COUNTRY_NAME, cityName)
            editor.putInt(Constants.SHARED_COUNTRY_ID, cityId)
            editor.putBoolean(Constants.SHARED_TYPE_CORD, false)
            editor.apply()
            mainActivity.changeCountry(cityId, cityName)
            mainActivity.onBackPressed()
        }
        return citiesWeatherAdapter
    }

    private val cities: String
        private get() {
            val buf = StringBuilder()
            var json: InputStream? = null
            try {
                json = mainActivity.assets.open(Constants.FILE_CITIES_NAME)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val bufferIn = BufferedReader(InputStreamReader(json, StandardCharsets.UTF_8))
            var str: String? = null
            while (true) {
                try {
                    if (bufferIn.readLine().also { str = it } == null) break
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                buf.append(str)
            }
            try {
                bufferIn.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return buf.toString()
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RESULT_CHOOSE_LOCATION_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val lat: Double = data.getDoubleExtra(Constants.EXTRA_LAT, 0.0)
                val lng: Double = data.getDoubleExtra(Constants.EXTRA_LNG, 0.0)
                editor.putFloat(Constants.SHARED_LAT, lat.toFloat())
                editor.putFloat(Constants.SHARED_LNG, lng.toFloat())
                editor.putBoolean(Constants.SHARED_TYPE_CORD, true)
                editor.commit()
                Thread(Runnable {
                    val gcd = Geocoder(mainActivity, Locale.getDefault())
                    var addresses: List<Address> = emptyList()

                    try {
                        gcd.getFromLocation(lat, lng, 1)?.let {
                            addresses = it
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return@Runnable
                    }
                    if (addresses.isNotEmpty()) {
                        editor.putString(Constants.SHARED_COUNTRY_NAME, addresses[0].locality)
                        editor.commit()
                    }
                }).start()
                mainActivity.changeCountry(lat, lng)
                mainActivity.onBackPressed()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val RESULT_CHOOSE_LOCATION_CODE = 0
    }
}