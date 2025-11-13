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
import tn.rifq_android.ui.components.BottomNavBar
import tn.rifq_android.ui.screens.home.HomeScreen
import tn.rifq_android.ui.screens.profile.ProfileScreen
import tn.rifq_android.ui.screens.pet.AddPetScreen
import tn.rifq_android.ui.screens.clinic.ClinicScreen
import tn.rifq_android.ui.screens.calendar.CalendarScreen
import tn.rifq_android.ui.screens.join.JoinScreen
import tn.rifq_android.ui.screens.chat.ChatAIScreen
import tn.rifq_android.ui.screens.petsitter.PetSitterScreen
import tn.rifq_android.ui.screens.petdetail.PetDetailScreen
import tn.rifq_android.ui.screens.medical.MedicalHistoryScreen
import tn.rifq_android.ui.screens.medical.MedicalScreen

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
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
                ClinicScreen(navController = navController)
            }

            composable("join") {
                JoinScreen(navController = navController)
            }

            composable("add_pet") {
                AddPetScreen(navController = navController)
            }

            composable("calendar") {
                CalendarScreen(navController = navController)
            }

            composable("chat_ai") {
                ChatAIScreen(navController = navController)
            }

            composable("petsitter") {
                PetSitterScreen(navController = navController)
            }

            composable("pet_detail/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                PetDetailScreen(navController = navController, petId = petId)
            }

            composable("medical_history") {
                MedicalHistoryScreen(navController = navController)
            }

            composable("medical") {
                MedicalScreen(navController = navController)
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

