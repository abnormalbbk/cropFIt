package com.bibek.cropfit.fields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bibek.cropfit.dashboard.Screen
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(navController: NavController) {
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val fieldCreated = savedStateHandle?.getStateFlow<Boolean>("fieldCreated", initialValue = false)

    LocalContext.current
    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val fieldsState = remember { mutableStateOf<Resource<List<Field>>>(Resource.Loading) }

    LaunchedEffect(fieldCreated) {
        if (fieldCreated?.value == true) {
            savedStateHandle.remove<Boolean>("fieldCreated")
            fetchFields(fieldsState = fieldsState)

        }
    }


    LaunchedEffect(Unit) {
        fetchFields(fieldsState)
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = {
                Text("My Fields")
            })
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = {
                navController.navigate(route = Screen.FieldForm.route)
            },
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Default.Add, contentDescription = "Add"
            )
        }
    }) { innerPadding ->
        when (val state = fieldsState.value) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.error, color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is Resource.Success -> {
                val fields = state.data

                if (fields.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No fields available")
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(fields.size) { index ->
                            FieldItem(field = fields[index])
                        }
                    }
                }
            }
        }
    }
}

private fun fetchFields(
    fieldsState: MutableState<Resource<List<Field>>>
) {
    try {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val db = Firebase.firestore

        db.collection("users").document(userId).collection("fields").get()
            .addOnSuccessListener { result ->
                val fields = result.documents.map { document ->
                    // Custom deserialization
                    val name = document.getString("name") ?: ""
                    val centerMap = document.get("center") as? Map<*, *> ?: mapOf<String, Double>()
                    val pointsList = document.get("points") as? List<Map<String, Double>>
                        ?: listOf<Map<String, Double>>()
                    val timestamp = document.getLong("timestamp") ?: 0L

                    // Convert the center map to LatLng
                    val center = LatLng(
                        centerMap["lat"].toString().toDouble(),
                        centerMap["lng"].toString().toDouble()
                    )

                    // Convert the points to LatLng
                    val points = pointsList.map {
                        LatLng(
                            it["lat"]?.toString()?.toDouble() ?: 0.0, it["lng"] ?: 0.0
                        )
                    }

                    Field(name, center, points, timestamp)
                }
                fieldsState.value = Resource.Success(fields)
            }.addOnFailureListener { e ->
                fieldsState.value = Resource.Error("Error fetching fields: ${e.localizedMessage}")
            }
    } catch (e: Exception) {
        fieldsState.value = Resource.Error("Error fetching fields: ${e.localizedMessage}")
    }
}

@Composable
fun FieldItem(field: Field) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = field.name, style = MaterialTheme.typography.titleLarge)
            Text("Latitude: ${field.center.latitude}, Longitude: ${field.center.longitude}")
        }
    }
}

data class Field(
    val name: String = "",
    val center: LatLng = LatLng(0.0, 0.0),
    val points: List<LatLng> = listOf(),
    val timestamp: Long = 0L
)

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: String) : Resource<Nothing>()
}

@Preview(showBackground = true)
@Composable
fun FieldsScreenPreview() {
    FieldsScreen(navController = rememberNavController())
}