package com.bibek.cropfit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.bibek.cropfit.dashboard.DashboardActivity
import com.bibek.cropfit.login.LoginScreen
import com.bibek.cropfit.ui.theme.CropFitTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CropFitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        navigateToDashboard()
                    } else {
                        LoginScreen(modifier = Modifier.padding(innerPadding), onLoginSuccess = {
                            it?.uid ?: ""
                            it?.email ?: ""
                            Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        })
                    }

                }
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
