package com.bibek.cropfit.utils

import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object FirebaseMLUtil {

    val cropLabels = arrayOf(
        "apple",        // 0
        "banana",       // 1
        "blackgram",    // 2
        "chickpea",     // 3
        "coconut",      // 4
        "coffee",       // 5
        "cotton",       // 6
        "grapes",       // 7
        "jute",         // 8
        "kidneybeans",  // 9
        "lentil",       // 10
        "maize",        // 11
        "mango",        // 12
        "mothbeans",    // 13
        "mungbean",     // 14
        "muskmelon",    // 15
        "orange",       // 16
        "papaya",       // 17
        "pigeonpeas",   // 18
        "pomegranate",  // 19
        "rice",         // 20
        "watermelon"    // 21
    )


    fun downloadModel(onSuccess: (model: CustomModel) -> Unit, onError: (message: String) -> Unit) {
        val conditions = CustomModelDownloadConditions.Builder().requireWifi().build()
        FirebaseModelDownloader.getInstance(

        ).getModel("crop_prediction", DownloadType.LATEST_MODEL, conditions).addOnCompleteListener {
            onSuccess.invoke(it.result)
        }.addOnCanceledListener {
            onError.invoke("Download is cancelled")
        }
    }

    fun predict(
        modelFile: File,
        nitrogen: Float,
        phosphorus: Float,
        potassium: Float,
        temperature: Float,
        humidity: Float,
        ph: Float,
        rainfall: Float
    ): String {
        val interpreter = Interpreter(loadMappedFile(modelFile))
        val input = arrayOf(
            floatArrayOf(
                nitrogen, phosphorus, potassium, temperature, humidity, ph, rainfall
            )
        )
        val output = Array(1) { FloatArray(22) }

        interpreter.run(input, output)
        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] }
        return predictedIndex?.let { cropLabels[it] } ?: "Could not predict"
    }

    fun loadMappedFile(modelFile: File): MappedByteBuffer {
        val inputStream = FileInputStream(modelFile)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
    }
}

