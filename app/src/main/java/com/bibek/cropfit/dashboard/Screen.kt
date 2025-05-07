package com.bibek.cropfit.dashboard

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Fields : Screen("field_screen")
    object Profile : Screen("profile_screen")
    object FieldForm : Screen("field_form")

    @Serializable
    data class FieldFormWithData(val fieldJson: String) :
        Screen("field_form/${android.net.Uri.encode(fieldJson)}")
}