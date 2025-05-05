package com.bibek.cropfit.dashboard

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Fields : Screen("field_screen")
    object Profile : Screen("profile_screen")
}