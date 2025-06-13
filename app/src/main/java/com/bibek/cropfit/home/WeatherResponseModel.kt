package com.bibek.cropfit.home

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseModel(
    val location: LocationModel, val current: CurrentModel
)

@Serializable
data class LocationModel(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Long,
    val localtime: String
)

@Serializable
data class CurrentModel(
    val last_updated_epoch: Long,
    val last_updated: String,
    val temp_c: Float,
    val temp_f: Float,
    val is_day: Int,
    val condition: ConditionModel,
    val wind_mph: Float,
    val wind_kph: Float,
    val wind_degree: Int,
    val wind_dir: String,
    val pressure_mb: Float,
    val pressure_in: Float,
    val precip_mm: Float,  // <-- Rainfall in mm
    val precip_in: Float,
    val humidity: Int,
    val cloud: Int,
    val feelslike_c: Float,
    val feelslike_f: Float,
    val windchill_c: Float,
    val windchill_f: Float,
    val heatindex_c: Float,
    val heatindex_f: Float,
    val dewpoint_c: Float,
    val dewpoint_f: Float,
    val vis_km: Float,
    val vis_miles: Float,
    val uv: Float,
    val gust_mph: Float,
    val gust_kph: Float
)

@Serializable
data class ConditionModel(
    val text: String, val icon: String, val code: Int
)
