package com.example.weather.room.model

import androidx.room.*


@Dao
interface CitiesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: City)

    @Update
    fun updateCity(city: City)

    @Delete
    fun deleteCity(city: City)

    @Query("DELETE FROM city WHERE id = :id")
    fun deleteCityById(id: Long)

    @Query("DELETE FROM city WHERE id = (SELECT id FROM city LIMIT 1)")
    fun deleteFirstRow()

    @get:Query("SELECT * FROM city")
    val allCities: List<City>

    @Query("SELECT * FROM city WHERE id = :id")
    fun getCityById(id: Long): City

    @Query("DELETE FROM city WHERE id = (SELECT id FROM city WHERE name = :name)")
    fun deleteCityByName(name: String)

    @get:Query("SELECT COUNT() FROM city")
    val countCities: Long

}