package com.bibek.cropfit.fields

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bibek.cropfit.dashboard.Screen
import com.bibek.cropfit.login.ui.theme.Red
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldsScreen(navController: NavController) {
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val fieldCreated = savedStateHandle?.getStateFlow<Boolean>("fieldCreated", initialValue = false)
    val fieldsState = remember { mutableStateOf<Resource<MutableList<Field>>>(Resource.Loading) }

    val context = LocalContext.current
    val userId = Firebase.auth.currentUser?.uid ?: ""

    var fieldToDelete = remember { mutableStateOf<Field?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    fun loadFields() {
        fetchFields(fieldsState)
    }

    // Load on first composition
    LaunchedEffect(Unit) {
        loadFields()
    }

    // Reload when back pressed with new field
    LaunchedEffect(fieldCreated) {
        if (fieldCreated?.value == true) {
            savedStateHandle.remove<Boolean>("fieldCreated")
            loadFields()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), title = { Text("My Fields") })
    }, floatingActionButton = {
        if (fieldsState.value is Resource.Success) {
            FloatingActionButton(
                onClick = { navController.navigate(route = Screen.FieldForm.route) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
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
                    Text(text = state.error, color = MaterialTheme.colorScheme.error)
                }
            }

            is Resource.Success -> {
                val fields = state.data

                if (fields.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No fields available. Please tap on + button below to add your fields.",
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(fields.size) { index ->
                            val field = fields[index]
                            FieldItem(field = field, onClick = {
                                val json = Uri.encode(Json.encodeToString(field))
                                navController.navigate(Screen.FieldFormWithData(json).route)
                            }, onDeleteClick = {
                                fieldToDelete.value = field
                            }, onFavouriteClick = {
                                isUpdating = true
                                val updatedField = field.copy(isFavourite = !field.isFavourite)
                                updateFavourite(updatedField, onSuccess = {
                                    isUpdating = false
                                    Toast.makeText(
                                        context, "Field updated successfully", Toast.LENGTH_SHORT
                                    ).show()
                                    fields[index] = updatedField
                                }, onError = {
                                    isUpdating = false
                                    Toast.makeText(
                                        context,
                                        "Failed to update the field. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                            })
                        }
                    }
                }
            }
        }

        if (isDeleting) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Deleting...") },
                text = { Text("Please wait while your field is being delete.") })
        }

        if (isUpdating) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("Updating...") },
                text = { Text("Please wait while your field is being update.") })
        }

        // Delete confirmation dialog
        fieldToDelete.value?.let { field ->
            AlertDialog(
                onDismissRequest = { fieldToDelete.value = null },
                title = { Text("Delete Field") },
                text = { Text("Are you sure you want to delete '${field.name}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        isDeleting = true
                        deleteField(fieldId = field.id, onSuccess = {
                            isDeleting = false
                            Toast.makeText(context, "Field deleted", Toast.LENGTH_SHORT).show()
                            fieldToDelete.value = null
                            loadFields()
                        }, onError = {
                            isDeleting = false
                            Toast.makeText(
                                context, "Failed to delete field", Toast.LENGTH_SHORT
                            ).show()
                            fieldToDelete.value = null
                        })
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fieldToDelete.value = null }) {
                        Text("Cancel")
                    }
                })
        }
    }
}


private fun fetchFields(
    fieldsState: MutableState<Resource<MutableList<Field>>>
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
                fieldsState.value = Resource.Success(fields.toMutableList())
            }.addOnFailureListener { e ->
                fieldsState.value = Resource.Error("Error fetching fields: ${e.localizedMessage}")
            }
    } catch (e: Exception) {
        fieldsState.value = Resource.Error("Error fetching fields: ${e.localizedMessage}")
    }
}

@Composable
fun FieldItem(
    onClick: (() -> Unit)? = null,
    field: Field,
    onFavouriteClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {}, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(text = field.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("Latitude: ")
                        }
                        withStyle(style = SpanStyle(color = Color.Gray)) {
                            append(field.center.latitude.toString())
                        }
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("\nLongitude: ")
                        }
                        withStyle(style = SpanStyle(color = Color.Gray)) {
                            append(field.center.longitude.toString())
                        }
                    })
            }
            Column(modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
                onDeleteClick?.let {
                    IconButton(
                        onClick = it
                    ) {
                        Image(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            colorFilter = ColorFilter.tint(Red)
                        )
                    }
                }
                onFavouriteClick?.let {
                    IconButton(
                        onClick = it
                    ) {
                        Image(
                            imageVector = if (field.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Delete",
                            colorFilter = ColorFilter.tint(Red)
                        )
                    }
                }
            }
        }
    }
}

@Serializable
data class Field(
    val id: String = "",
    val name: String = "",
    val center: LatLngSerializable = LatLngSerializable(0.0, 0.0),
    val points: List<LatLngSerializable> = listOf(),
    val timestamp: Long = 0L,
    val isFavourite: Boolean = false
)

@Serializable
data class LatLngSerializable(
    val latitude: Double, val longitude: Double
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)

    companion object {
        fun fromLatLng(latLng: LatLng): LatLngSerializable =
            LatLngSerializable(latLng.latitude, latLng.longitude)
    }
}

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

@Preview(showBackground = true)
@Composable
fun FieldItemPreview() {
    FieldItem(
        field = Field().copy(
            name = "Sample Field",
            center = LatLngSerializable(0.11123, 535345.34534),
        )
    )
}

fun deleteField(fieldId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    Firebase.firestore.collection("users").document(userId).collection("fields").document(fieldId)
        .delete().addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e.localizedMessage ?: "Unknown error") }
}

fun updateFavourite(field: Field, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    Firebase.firestore.collection("users").document(userId).collection("fields").document(field.id)
        .update("favourite", field.isFavourite).addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e.localizedMessage ?: "Unknown error") }
}