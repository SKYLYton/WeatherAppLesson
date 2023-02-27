package com.example.weather.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"])])
data class City(
    @field:ColumnInfo(name = "name") var name: String,
    @field:ColumnInfo(
        name = "temperature"
    ) var temperature: Float,
    @field:ColumnInfo(name = "pressure") var pressure: Int,
    @field:ColumnInfo(
        name = "wind"
    ) var wind: Float
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

}