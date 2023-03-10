package com.muhammed.sword.weather.domain.weather

data class CurrentWeatherModel(
    val temperatureCelsius: Double,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val weatherType: WeatherType?,
) {
    constructor() : this(0.0, 0.0, 0.0, null)
}