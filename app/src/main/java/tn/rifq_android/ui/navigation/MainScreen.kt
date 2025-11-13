package tn.rifq_android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tn.rifq_android.ui.components.MyPetsBottomNavBar
import tn.rifq_android.ui.screens.home.HomeScreen
import tn.rifq_android.ui.screens.profile.ProfileScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            MyPetsBottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "mypets",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("mypets") {
                HomeScreen(navController = navController)
            }

            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout
                )
            }

            // Placeholder routes for other bottom nav items
            composable("clinic") {
                // TODO: Implement ClinicScreen
                PlaceholderScreen(title = "Clinic")
            }

            composable("join") {
                // TODO: Implement JoinScreen
                PlaceholderScreen(title = "Join")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title Screen - Coming Soon", style = MaterialTheme.typography.headlineMedium)
    }
}

