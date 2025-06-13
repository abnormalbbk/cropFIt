package com.bibek.cropfit.home

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bibek.cropfit.fields.Field
import com.bibek.cropfit.fields.LatLngSerializable
import com.bibek.cropfit.utils.ConversionUtil
import com.bibek.cropfit.utils.FirebaseMLUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class FieldsUiState {
    object Loading : FieldsUiState()
    data class Success(
        val fields: List<Field>,
        val message: String? = null,
        val prediction: String? = null,
        val isLoading: Boolean = false
    ) : FieldsUiState()

    data class Error(val message: String) : FieldsUiState()
}

class HomeViewModel : ViewModel() {
    private val _uiState = mutableStateOf<FieldsUiState>(FieldsUiState.Loading)
    val uiState: State<FieldsUiState> = _uiState
    var fields = mutableListOf<Field>()
    val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchFields()
    }

    private fun fetchFields() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firestore.collection("users").document(userId).collection("fields").get()
            .addOnSuccessListener { result ->
                val fields = result.documents.map { document ->
                    // Custom deserialization

                    val name = document.getString("name") ?: ""
                    val centerMap = document.get("center") as? Map<*, *> ?: mapOf<String, Double>()
                    val pointsList = document.get("points") as? List<Map<String, Double>>
                        ?: listOf<Map<String, Double>>()
                    val timestamp = document.getLong("timestamp") ?: 0L

                    // Convert the center map to LatLng
                    val center = LatLngSerializable(
                        centerMap["lat"].toString().toDouble(),
                        centerMap["lng"].toString().toDouble()
                    )

                    // Convert the points to LatLng
                    val points = pointsList.map {
                        LatLngSerializable(
                            it["lat"]?.toString()?.toDouble() ?: 0.0, it["lng"] ?: 0.0
                        )
                    }

                    val isFavourite = document.getBoolean("favourite") ?: false

                    Field(document.id, name, center, points, timestamp, isFavourite)
                }
                this.fields = fields.toMutableList()
                _uiState.value = FieldsUiState.Success(this.fields)
            }.addOnFailureListener { e ->
                _uiState.value = FieldsUiState.Error(e.localizedMessage ?: "Unknown error")
            }
    }

    fun predictCropForField(field: Field) {
        _uiState.value = FieldsUiState.Success(
            this.fields, isLoading = true, message = "Getting weather and soil data for the field"
        )
        viewModelScope.launch {
            val lat = field.center.latitude
            val lon = field.center.longitude
            val soilDataDeferred = async<SoilResponseModel?> {
                fetchSoilData(lat, lon)
            }
            val weatherResponseDeferred = async<WeatherResponseModel?> {
                getWeather(lat, lon)
            }
            val responses = awaitAll(soilDataDeferred, weatherResponseDeferred)


            val soilResponse = responses[0] as SoilResponseModel?
            soilResponse?.let {
                Log.d("SoilData", it.toString())
            }

            val weatherResponse = responses[1] as WeatherResponseModel?
            weatherResponse?.let {
                it as WeatherResponseModel?
                Log.d(
                    "Weather",
                    "Temp: ${it.current.temp_c}°C, Humidity: ${it.current.humidity}}%, Rainfall: ${it.current.precip_mm} mm"
                )
            }

            _uiState.value = FieldsUiState.Success(
                fields, isLoading = true, message = "Making the predictions ..."
            )
            FirebaseMLUtil.downloadModel(onSuccess = {
                it.file?.let { modelFile ->
                    Log.d("Bibek ;: HomeViewModel", "Data Received now predicting")
                    val suggestedCrop = FirebaseMLUtil.predict(
                        modelFile,
                        nitrogen = soilResponse?.total_nitrogen?.toFloat() ?: 0F,
                        potassium = soilResponse?.potassium?.toFloat() ?: 0F,
                        phosphorus = ConversionUtil.convertP2O5ToPhosphorus(soilResponse?.p2o5.toString())
                            ?.toFloat() ?: 0F,
                        ph = soilResponse?.ph?.toFloat() ?: 0F,
                        temperature = weatherResponse?.current?.temp_c?.toFloat() ?: 0F,
                        humidity = weatherResponse?.current?.humidity?.toFloat() ?: 0F,
                        rainfall = weatherResponse?.current?.precip_mm?.toFloat() ?: 0F

                    )
                    Log.d("Bibek :: Suggested Crop is", suggestedCrop)
                    _uiState.value =
                        FieldsUiState.Success(fields, isLoading = false, prediction = suggestedCrop)
                }
            }, onError = {
                Log.e("Bibek :: onError", it)
            })
        }
    }

    suspend fun fetchSoilData(lat: Double, lon: Double): SoilResponseModel? {
        return try {
            val response: SoilResponseModel =
                client.get("https://soil.narc.gov.np/soil/api/?lat=$lat&lon=$lon").body()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponseModel {
        val apiKey = "23cb5c5e0ccb4883a7e82101251205"
        val url = "https://api.weatherapi.com/v1/current.json?key=$apiKey&q=$lat,$lon"

        val response: WeatherResponseModel = client.get(url).body()

        val temperature = response.current.temp_c
        val humidity = response.current.humidity
        val rainfall = response.current.precip_mm

        return response
    }

    fun clearPredictions() {
        _uiState.value =
            FieldsUiState.Success(fields, isLoading = false)
    }
}