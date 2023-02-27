package com.example.weather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R

class CitiesWeatherAdapter(listDays: List<CityItem>) :
    RecyclerView.Adapter<CitiesWeatherAdapter.ViewHolder>() {
    private val listCities: List<CityItem>

    var onItemSelect: ((Int, String) -> Unit)? = null


    init {
        listCities = listDays
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.city_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cityItem: CityItem = listCities[position]
        holder.textViewName.text = cityItem.cityName
        holder.view.setOnClickListener { v: View? ->
            if (onItemSelect != null) {
                onItemSelect?.invoke(cityItem.id, cityItem.cityName)
            }
        }
    }

    override fun getItemCount(): Int {
        return listCities.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView
        val view: View

        init {
            textViewName = itemView.findViewById<TextView>(R.id.textViewName)
            view = itemView
        }
    }
}
