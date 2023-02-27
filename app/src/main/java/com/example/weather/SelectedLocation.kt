package com.example.weather

class SelectedLocation {
    var cityName: String? = null
    var cityId = 0
    var lat = 0.0
    var lng = 0.0
    var isCord = false

    constructor() {}
    constructor(lat: Double, lng: Double, isCord: Boolean, cityName: String?) {
        this.lat = lat
        this.lng = lng
        this.isCord = isCord
        this.cityName = cityName
    }

    constructor(cityName: String?, cityId: Int) {
        this.cityName = cityName
        this.cityId = cityId
    }
}