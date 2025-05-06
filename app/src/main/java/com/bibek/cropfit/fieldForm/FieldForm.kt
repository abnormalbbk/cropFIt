package com.bibek.cropfit.fieldForm

import android.location.Location
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FieldFormScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionState =
        rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 14f)
    }
    val markerStates = remember { mutableStateListOf<MarkerState>() }
    var selectedMarkerState by remember { mutableStateOf<MarkerState?>(null) }

    val fieldName = remember { mutableStateOf("") }
    val error = remember { mutableStateOf(false) }

    // Upload states
    var isUploading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isFieldAdded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (permissionState.status == PermissionStatus.Granted) {
            getUserLocation(fusedLocationClient, cameraPositionState)
        }
    }

    BackHandler {
        // Set result if needed
        navController.previousBackStackEntry?.savedStateHandle?.set(
            "fieldCreated", isFieldAdded
        ) // or true if condition met

        // Navigate back
        navController.popBackStack()

    }
    Column(modifier = Modifier.fillMaxSize()) {
        if (permissionState.status != PermissionStatus.Granted) {
            PermissionRequired(permissionState) {
                getUserLocation(fusedLocationClient, cameraPositionState)
            }
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                markerStates.add(MarkerState(position = latLng))
            }) {
            markerStates.forEach { state ->
                Marker(
                    state = state, title = "Marker", draggable = true, onClick = {
                        selectedMarkerState = state
                        true
                    })
            }

            if (markerStates.size >= 3) {
                Polygon(
                    points = markerStates.map { it.position },
                    fillColor = Color(0x5500FF00),
                    strokeColor = Color.Green,
                    strokeWidth = 3f
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = fieldName.value,
                onValueChange = {
                    fieldName.value = it
                    error.value = false
                },
                isError = error.value,
                label = { Text("Field Name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (fieldName.value.isBlank()) {
                    error.value = true
                } else if (markerStates.size >= 3) {
                    val center = computeCenter(markerStates.map { it.position })
                    isUploading = true

                    uploadFieldToFirestore(
                        name = fieldName.value,
                        center = center,
                        points = markerStates.map { it.position },
                        onSuccess = {
                            isUploading = false
                            showSuccessDialog = true
                            fieldName.value = ""
                            isFieldAdded = true
                            markerStates.clear()
                        },
                        onFailure = {
                            isUploading = false
                            showErrorDialog = true
                        })
                }
            }) {
                Text("Done")
            }
        }

        if (error.value) {
            Text(
                "Field name cannot be empty",
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    selectedMarkerState?.let { marker ->
        AlertDialog(
            onDismissRequest = { selectedMarkerState = null },
            title = { Text("Delete marker?") },
            confirmButton = {
                TextButton(onClick = {
                    markerStates.remove(marker)
                    selectedMarkerState = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMarkerState = null }) {
                    Text("Cancel")
                }
            })
    }

    if (isUploading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Uploading...") },
            text = { Text("Please wait while your field is being saved.") })
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text("Field saved successfully.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "fieldCreated", true
                    )
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            })
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text("Something went wrong while saving.") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            })
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequired(
    permissionState: com.google.accompanist.permissions.PermissionState, onGranted: () -> Unit
) {
    LaunchedEffect(permissionState) {
        if (permissionState.status.shouldShowRationale) {
            // Optionally show a message explaining why permission is required
        } else if (permissionState.status == PermissionStatus.Granted) {
            onGranted()
        }
    }

    // Request the permission if not granted yet
    if (permissionState.status != PermissionStatus.Granted) {
        Button(onClick = { permissionState.launchPermissionRequest() }) {
            Text("Request Location Permission")
        }
    }
}

// Function to get user's location and set camera
fun getUserLocation(
    fusedLocationClient: FusedLocationProviderClient, cameraPositionState: CameraPositionState
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            val userLatLng = LatLng(it.latitude, it.longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 16f)
        }
    }
}

fun computeCenter(points: List<LatLng>): LatLng {
    val lat = points.map { it.latitude }.average()
    val lng = points.map { it.longitude }.average()
    return LatLng(lat, lng)
}

fun uploadFieldToFirestore(
    name: String, center: LatLng, points: List<LatLng>, onSuccess: () -> Unit, onFailure: () -> Unit
) {
    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onFailure()

    val fieldData = hashMapOf(
        "name" to name,
        "center" to hashMapOf("lat" to center.latitude, "lng" to center.longitude),
        "points" to points.map { mapOf("lat" to it.latitude, "lng" to it.longitude) },
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("users").document(userId).collection("fields").add(fieldData)
        .addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure() }
}


@Preview
@Composable
fun FieldFormScreenPreview() {
    FieldFormScreen(rememberNavController())
}
