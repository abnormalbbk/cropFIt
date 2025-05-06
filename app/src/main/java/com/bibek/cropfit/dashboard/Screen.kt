package com.bibek.cropfit.dashboard

import kotlinx.serialization.Serializable

sealed class Screen(val route: String) {
    @Serializable
    object Home : Screen("home_screen")

    @Serializable
    object Fields : Screen("field_screen")

    @Serializable
    object Profile : Screen("profile_screen")

    @Serializable
    object FieldForm : Screen("field_form")
}