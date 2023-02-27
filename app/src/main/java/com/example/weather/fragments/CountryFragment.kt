package com.example.weather.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.weather.Constants
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.SelectedLocation
import com.example.weather.bottomsheet.BottomSheetCreator
import com.example.weather.databinding.BottomDialogNotificationBinding
import com.example.weather.databinding.FragmentCountryBinding
import com.example.weather.retrofit.model.RetrofitRequest
import com.example.weather.retrofit.model.weather.WeatherRequest
import com.example.weather.room.model.CitiesSource
import com.example.weather.room.model.City
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CountryFragment : Fragment() {

    private var _binding: FragmentCountryBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var editor: SharedPreferences.Editor? = null
    private lateinit var selectedLocation: SelectedLocation
    private lateinit var mainActivity: MainActivity
    private val isCord = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        sharedPreferences = requireContext().getSharedPreferences(Constants.MAIN_SHARED_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        selectedLocation = mainActivity.selectedLocation
        init()
        return binding.root
    }

    private fun init() {
        binding.imageButton.setOnClickListener {
            mainActivity.pushFragments(
                SearchFragment(),
                true,
                null
            )
        }
        if (selectedLocation.isCord) {
            RetrofitRequest.instance
                .openWeather.getWeather(
                    Constants.API_UNITS_METRIC, Locale.getDefault().country,
                    selectedLocation.lat, selectedLocation.lng, Constants.API_KEY
                ).enqueue(callback)
        } else {
            RetrofitRequest.instance.openWeather.getWeather(
                    Constants.API_UNITS_METRIC, Locale.getDefault().country,
                    selectedLocation.cityId, Constants.API_KEY
                ).enqueue(callback)
        }
    }

    var callback: Callback<WeatherRequest> = object : Callback<WeatherRequest> {
        override fun onResponse(call: Call<WeatherRequest>, response: Response<WeatherRequest>) {
            val weatherRequest: WeatherRequest? = response.body()
            if (response.isSuccessful) {
                weatherRequest?.let { weatherRequest->
                    val cityName: String = weatherRequest.name
                    selectedLocation.cityName = cityName
                    binding.textViewCountry.text = cityName
                    mainActivity.setCountryText(cityName)
                    binding.textViewType.text = firstUpperCase(
                        weatherRequest.weather[0].description
                    )
                    binding.textViewTemp.text = java.lang.String.valueOf(weatherRequest.main.temp)
                    binding.textViewPress.text = java.lang.String.valueOf(
                        weatherRequest.main.pressure
                    )
                    binding.textViewWind.text = java.lang.String.valueOf(weatherRequest.wind.speed)
                    CitiesSource.instance.replaceCity(
                        City(
                            cityName,
                            weatherRequest.main.temp,
                            weatherRequest.main.pressure,
                            weatherRequest.wind.speed
                        )
                    )
                }
            } else {
                BottomSheetCreator.show(requireContext(), getString(R.string.toast_request_error))
            }
        }

        override fun onFailure(call: Call<WeatherRequest>, t: Throwable) {
            BottomSheetCreator.show(requireContext(), getString(R.string.toast_request_no_data))
        }
    }

    fun firstUpperCase(word: String?): String {
        return if (word == null || word.isEmpty()) "" else word.substring(0, 1)
            .uppercase(Locale.getDefault()) + word.substring(1)
    }
}