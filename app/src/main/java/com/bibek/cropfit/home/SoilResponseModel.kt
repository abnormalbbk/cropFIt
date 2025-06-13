package com.bibek.cropfit.home

import kotlinx.serialization.Serializable

@Serializable
data class SoilResponseModel(
    val coord: Coord,
    val ph: String,
    val organic_matter: String,
    val total_nitrogen: String,
    val potassium: String,
    val p2o5: String,
    val boron: String,
    val zinc: String,
    val sand: String,
    val clay: String,
    val slit: String,
    val parentsoil: String,
    val province: String,
    val district: String,
    val palika: String,
    val fertilizer: Fertilizer
)

@Serializable
data class Coord(
    val lon: Double, val lat: Double, val elevation: Double
)

@Serializable
data class Fertilizer(
    val Maize: CropType, val Rice: CropType, val Wheat: WheatType
)

@Serializable
data class CropType(
    val OPV: FertilizerDetail, val Hybrid: FertilizerDetail
)

@Serializable
data class WheatType(
    val OPV: FertilizerDetail
)

@Serializable
data class FertilizerDetail(
    val MOP: String,
    val DAP: String,
    val UREA1: String,
    val UREA2: String,
    val UREA3: String,
    val organic: String,
    val zinc: String,
    val boron: String
)
