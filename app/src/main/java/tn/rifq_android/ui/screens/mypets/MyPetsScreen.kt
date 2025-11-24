package tn.rifq_android.ui.screens.mypets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.rifq_android.ui.components.*
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.profile.ProfileViewModel
import tn.rifq_android.viewmodel.profile.ProfileViewModelFactory
import tn.rifq_android.viewmodel.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPetsScreen(
    navController: NavHostController,
    themePreference: tn.rifq_android.data.storage.ThemePreference
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            LoadingScreen()
        }
        is ProfileUiState.Success -> {
            MyPetsContent(
                navController = navController,
                pets = state.pets,
                themePreference = themePreference,
                onRefresh = { viewModel.loadProfile() }
            )
        }
        is ProfileUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.loadProfile() }
            )
        }
        else -> {

        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = VetCanyon)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = TextPrimary
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = VetCanyon)
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPetsContent(
    navController: NavHostController,
    pets: List<tn.rifq_android.data.model.pet.Pet>,
    themePreference: tn.rifq_android.data.storage.ThemePreference,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopNavBar(
                title = "My Pets",
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            if (pets.isNotEmpty()) {
                item {
                    CalendarSection(
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }


            if (pets.isEmpty()) {
                item {
                    EmptyPetsView(
                        onAddPetClick = { navController.navigate("add_pet") }
                    )
                }
            } else {

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Pets",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        TextButton(
                            onClick = { navController.navigate("add_pet") },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = VetCanyon,
                                containerColor = VetCanyon.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add pet",
                                modifier = Modifier.size(16.dp),
                                tint = VetCanyon
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Pet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }


                items(pets) { pet ->
                    PetRow(
                        name = pet.name,
                        breed = pet.breed ?: pet.species.replaceFirstChar { it.uppercase() },
                        age = pet.age?.let { "$it years" } ?: "Unknown age",
                        color = VetCanyon,
                        modifier = Modifier.padding(horizontal = 18.dp),
                        onClick = { navController.navigate("pet_detail/${pet.id}") }
                    )
                }
            }


            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun CalendarSection(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "THIS WEEK",
            actionLabel = "View All",
            onActionClick = { navController.navigate("calendar") }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Calendar",
                    modifier = Modifier.size(24.dp),
                    tint = TextSecondary
                )
                Text(
                    text = "No events this week",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun formatAge(age: Int?): String {
    return when (age) {
        null -> "Unknown age"
        0 -> "Less than 1 year"
        1 -> "1 year old"
        else -> "$age years old"
    }
}
