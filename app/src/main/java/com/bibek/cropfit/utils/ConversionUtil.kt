package com.bibek.cropfit.utils

object ConversionUtil {
    fun convertP2O5ToPhosphorus(p2o5: String?): Double? {
        return try {
            val numericValue = p2o5?.filter { it.isDigit() || it == '.' }?.toDouble()
            ((numericValue ?: 0.0) * 0.44 * 100.0).toInt() / 100.0
        } catch (e: Exception) {
            null
        }
    }
}