package tn.rifq_android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun MyPetsBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "mypets",
            onClick = {
                if (currentRoute != "mypets") {
                    navController.navigate("mypets") {
                        popUpTo("mypets") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "My Pets") },
            label = { Text("My pets") }
        )
        NavigationBarItem(
            selected = currentRoute == "clinic",
            onClick = {
                if (currentRoute != "clinic") {
                    navController.navigate("clinic") {
                        popUpTo("mypets") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Clinic") },
            label = { Text("Clinic") }
        )
        NavigationBarItem(
            selected = currentRoute == "join",
            onClick = {
                if (currentRoute != "join") {
                    navController.navigate("join") {
                        popUpTo("mypets") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Join") },
            label = { Text("Join") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo("mypets") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
