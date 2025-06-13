package com.bibek.cropfit.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bibek.cropfit.dashboard.Screen
import com.bibek.cropfit.home.FieldsUiState.Success
import com.bibek.cropfit.utils.FirebaseMLUtil
import com.bibek.cropfit.utils.FirebaseMLUtil.cropLabels
import org.tensorflow.lite.Interpreter
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(

            text = "🌾 CropFit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ready to find the best crop for your field today?",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "🌟 Choose Your Favorite Field",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (state.value) {
            is FieldsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is FieldsUiState.Error -> {
                val message = (state as FieldsUiState.Error).message
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Error: $message", color = Color.Red)
                }
            }

            is FieldsUiState.Success -> {
                val currentState = state.value as Success
                val fields = currentState.fields

                if (fields.isEmpty()) {
                    Text("No fields found.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fields.filter { it.isFavourite }.size) { index ->
                            val field = fields[index]
                            ElevatedCard(
                                onClick = {
                                    viewModel.predictCropForField(field)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                ListItem(
                                    headlineContent = { Text(field.name) },
                                    supportingContent = {
                                        Text("Lat: ${field.center.latitude}")
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.Info, contentDescription = null)
                                    })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "⚡ Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        ActionCard("📍", "Map My Field", onClick = {
                            navController.navigate(Screen.Fields.route)
                        })
                    }
                    item {
                        ActionCard("📊", "Crop Stats", onClick = {
                            FirebaseMLUtil.downloadModel(onSuccess = {
                                Log.i("Bibek :: downloadModel  ", "Success")
                                it.file?.let { file ->
                                    Log.i("Bibek ::   ", "Predicting")


                                    testModelWithSampleData(file)

                                    // Papaya
//                                    val data = arrayOf(
//                                        35f,
//                                        68f,
//                                        45f,
//                                        42.93605359f,
//                                        90.09448142f,
//                                        6.612429546f,
//                                        234.8466111f
//                                    )
                                    // Apple
//                                    val data = arrayOf(
//                                        10f,
//                                        140f,
//                                        197f,
//                                        22.16939473f,
//                                        90.27185592f,
//                                        6.229498836f,
//                                        124.4683112f


//                                    )
// Coconut
                                    val data = arrayOf(
                                        8f,
                                        28f,
                                        30f,
                                        25.51618488f,
                                        94.33465411f,
                                        6.015672239f,
                                        135.1272491f


//                                    )


                                        // ChickPea
//                                    val data = arrayOf(
//                                        40f,
//                                        72f,
//                                        77f,
//                                        17.02498456f,
//                                        16.98861173f,
//                                        7.485996067f,
//                                        88.55123143f
//
//                                    )
                                        // Coffee
//                                    val data = arrayOf(
//                                        108f,
//                                        35f,
//                                        25f,
//                                        23.98143338f,
//                                        61.10935084f,
//                                        6.971963169f,
//                                        161.5279095f
                                    )
                                    val prediction = FirebaseMLUtil.predict(
//                                        file, 71f, 54f, 16f, 22.61f, 63.69f, 5.74f, 87.75f
//                                        file,
//                                        71f,
//                                        54f,
//                                        16f,
//                                        22.61359953f,
//                                        63.69070564f,
//                                        5.7499144210000015f,
//                                        87.75953857f
                                        file,
                                        data[0],
                                        data[1],
                                        data[2],
                                        data[3],
                                        data[4],
                                        data[5],
                                        data[6],

                                        )
                                    Log.i("Bibek :: Prediction Result is  ", prediction)

                                    // Example test case
                                    val result1 = FirebaseMLUtil.predict(
                                        modelFile = file,
                                        nitrogen = 83f,
                                        phosphorus = 45f,
                                        potassium = 60f,
                                        temperature = 28.5f,
                                        humidity = 65.8f,
                                        ph = 6.2f,
                                        rainfall = 220f
                                    )
                                    Log.i(
                                        "Bibek :: Predicted crop (Rice):", "$result1"
                                    )  // Should likely predict "Rice"

// Edge case test
                                    val result2 = FirebaseMLUtil.predict(
                                        modelFile = file,
                                        nitrogen = 70f,
                                        phosphorus = 35f,
                                        potassium = 40f,
                                        temperature = 26f,
                                        humidity = 68f,
                                        ph = 4.5f,
                                        rainfall = 120f
                                    )
                                    Log.i(
                                        "Bibek :: Predicted crop: ", "$result2"
                                    )  // Test acidic soil condition
                                }
                            }, onError = {
                                Log.e("Bibek :: onError ::  ", it)
                            })
                        })
                    }
                    item { ActionCard("🧪", "Analyze Soil", featureAvailable = false) }
                    item { ActionCard("🌦️", "Weather", featureAvailable = false) }
                }


                if (currentState.isLoading && currentState.message?.isNotEmpty() == true) {
                    AlertDialog(onDismissRequest = {}, confirmButton = {}, title = {
                        Column {
                            Text("Please  Wait...")
                            Spacer(modifier = Modifier.height(20.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }, text = { Text(currentState.message) })
                } else if (currentState.prediction?.isNotEmpty() == true) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Prediction Done.") },
                        text = { Text("The best crop to plant is ${currentState.prediction}.") },
                        confirmButton = {
                            Text(
                                "Done", modifier = Modifier.clickable(
                                onClick = {
                                    viewModel.clearPredictions()
                                }))
                        },
                    )
                } else {
                    Log.d("Here", "Here")
                }
            }
        }
    }


    when (state) {
        is Success -> {

        }
    }
}

fun testModelWithSampleData(modelFile: File) {
    // Test cases: (input, expectedLabel)
    val testCases = listOf(
        Pair(floatArrayOf(83f, 45f, 60f, 28.5f, 65.8f, 6.2f, 220f), "rice"),
        Pair(floatArrayOf(90f, 42f, 43f, 25.8f, 71.2f, 6.8f, 110f), "maize"),
        Pair(floatArrayOf(115f, 58f, 20f, 32.3f, 62.5f, 7.5f, 85f), "cotton"),
        Pair(floatArrayOf(72f, 35f, 40f, 22.1f, 80.4f, 5.8f, 180f), "coffee"),
        Pair(floatArrayOf(88f, 40f, 65f, 27.0f, 75.0f, 6.0f, 200f), "banana")
    )

    val interpreter = Interpreter(FirebaseMLUtil.loadMappedFile(modelFile))
    val output = Array(1) { FloatArray(22) } // Adjust size (22 classes)

    testCases.forEach { (input, expectedLabel) ->
        interpreter.run(arrayOf(input), output)
        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] }
        val predictedLabel = cropLabels[predictedIndex ?: -1] ?: "unknown"

        Log.d("ModelTest", "Input: ${input.joinToString()}")
        Log.d("ModelTest", "Expected: $expectedLabel | Predicted: $predictedLabel")

        if (predictedLabel == expectedLabel) {
            Log.d("ModelTest", "✅ PASSED")
        } else {
            Log.d("ModelTest", "❌ FAILED")
        }
    }
    interpreter.close()
}

@Composable
fun ActionCard(
    emoji: String, label: String, onClick: (() -> Unit)? = null, featureAvailable: Boolean = true
) {
    Box() {
        ElevatedCard(
            onClick = { if (featureAvailable) onClick?.invoke() },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
//            enabled = featureAvailable
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = emoji, fontSize = 28.sp)
                Spacer(Modifier.height(6.dp))
                Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
        }

        if (!featureAvailable) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .matchParentSize()
                    .offset(x = 8.dp, y = (-8).dp)
                    .background(
                        color = Color.Red.copy(alpha = 0.4f), shape = RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Unavailable",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}