package com.bibek.cropfit.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.bibek.cropfit.dashboard.ui.theme.CropFitTheme
import com.bibek.cropfit.fieldForm.FieldFormScreen
import com.bibek.cropfit.fields.FieldsScreen
import com.bibek.cropfit.home.HomeScreen
import com.bibek.cropfit.profile.ProfileScreen

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CropFitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(
                        name = "Android", modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val title: String, val icon: ImageVector, val route: String
)

@Composable
fun DashboardScreen(name: String, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController) }) { innerPadding ->

        val graph = navController.createGraph(startDestination = Screen.Home.route) {
            composable(route = Screen.Home.route) {
                HomeScreen()
            }
            composable(route = Screen.Fields.route) {
                FieldsScreen(navController)
            }
            composable(route = Screen.Profile.route) {
                ProfileScreen()
            }
            composable(route = Screen.FieldForm.route) {
                FieldFormScreen(navController)
            }
        }
        NavHost(
            navController = navController, graph = graph, modifier = Modifier.padding(innerPadding)
        )

    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }

    val navigationItems = listOf(
        NavigationItem(
            title = "Home", icon = Icons.Default.Home, route = Screen.Home.route
        ),
        NavigationItem(
            title = "Fields", icon = Icons.Default.Add, route = Screen.Fields.route
        ),
        NavigationItem(
            title = "Profile", icon = Icons.Default.Person, route = Screen.Profile.route
        ),
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index, onClick = {
                selectedNavigationIndex.intValue = index
                navController.navigate(item.route)
            }, icon = {
                Icon(imageVector = item.icon, contentDescription = item.title)
            }, label = {
                Text(
                    item.title,
                    color = if (index == selectedNavigationIndex.intValue) Color.Black
                    else Color.Gray
                )
            }, colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.surface,
                indicatorColor = MaterialTheme.colorScheme.primary
            )

            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CropFitTheme {
        DashboardScreen("Android")
    }
}