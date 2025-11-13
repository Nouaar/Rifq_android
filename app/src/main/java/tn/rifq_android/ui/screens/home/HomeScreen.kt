package tn.rifq_android.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.ProfileViewModel
import tn.rifq_android.viewmodel.ProfileViewModelFactory
import tn.rifq_android.viewmodel.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
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
            HomeScreenContent(
                navController = navController,
                pets = state.pets,
                userName = state.user.name
            )
        }
        is ProfileUiState.Error -> {
            ErrorScreen(message = state.message, onRetry = { viewModel.loadProfile() })
        }
        is ProfileUiState.UserDeleted -> {
            // User deleted, should navigate to login
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        else -> {
            // Idle state
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = OrangeAccent)
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    navController: NavHostController,
    pets: List<tn.rifq_android.data.model.pet.Pet>,
    userName: String
) {
    Scaffold(
        topBar = { MyPetsTopBar() },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Your Pets header with Add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Pets",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedButton(
                        onClick = { navController.navigate("add_pet") },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.5.dp, OrangeAccent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OrangeAccent
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add pet",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add a pet",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Pet carousel at the top
            item {
                if (pets.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🐾",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No pets yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your first pet to get started",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(pets) { pet ->
                            SmallPetCard(
                                pet = pet,
                                onClick = { navController.navigate("pet_detail/${pet.id}") }
                            )
                        }
                    }
                }
            }



            // Quick Actions section
            item {
                Text(
                    text = "Quick Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            item {
                QuickActionsGrid(navController = navController)
            }

            // Veterinary section
            item {
                Text(
                    text = "Veterinary",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            // TODO: Replace with dynamic vet data from backend API
            // items(vets) { vet ->
            //     VetListItem(vet, onClick = { navController.navigate("vet_profile/${vet.id}") })
            // }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPetsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "My pets",
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = Color(0xFF1C1C1E)
            )
        },
        actions = {
            IconButton(onClick = { /* notifications */ }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = OrangeAccent
                )
            }
            IconButton(onClick = { /* settings */ }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun SmallPetCard(pet: tn.rifq_android.data.model.pet.Pet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(getPetColor(pet.species)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getPetEmoji(pet.species),
                    fontSize = 40.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = pet.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Text(
                text = pet.breed ?: pet.species.replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

// Helper functions to get pet emoji and color based on species
private fun getPetEmoji(species: String): String {
    return when (species.lowercase()) {
        "dog" -> "🐕"
        "cat" -> "🐈"
        "bird" -> "🐦"
        "fish" -> "🐠"
        "rabbit" -> "🐰"
        "hamster" -> "🐹"
        else -> "🐾"
    }
}

private fun getPetColor(species: String): Color {
    return when (species.lowercase()) {
        "dog" -> PetAvatarBrown
        "cat" -> PetAvatarTan
        "bird" -> Color(0xFFADD8E6)
        "fish" -> Color(0xFF87CEEB)
        "rabbit" -> Color(0xFFFFB6C1)
        "hamster" -> Color(0xFFFFA07A)
        else -> PetAvatarBrown
    }
}

enum class BadgeKind { Success, Warning, Danger }

@Composable
private fun BadgeSmall(text: String, kind: BadgeKind) {
    val (bg, fg) = when (kind) {
        BadgeKind.Success -> Pair(Color(0xFFD1F4E7), Color(0xFF0F9D58))
        BadgeKind.Warning -> Pair(Color(0xFFFFF4E5), Color(0xFFE67C00))
        BadgeKind.Danger -> Pair(Color(0xFFFFE8E8), Color(0xFFD32F2F))
    }
    Box(
        modifier = Modifier
            .background(bg, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickActionsGrid(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                icon = "🤖",
                label = "Chat AI",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("chat_ai") }
            QuickActionCard(
                icon = "🩺",
                label = "Find Vet",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("clinic") }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                icon = "📅",
                label = "Calendar",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("calendar") }
            QuickActionCard(
                icon = "🧑‍🍼",
                label = "Pet Sitter",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("petsitter") }
        }
    }
}

@Composable
private fun QuickActionCard(icon: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun VetListItem(name: String, schedule: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (name == "John Smith") TimelineLine else PetAvatarTan
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (name == "John Smith") "👨‍⚕️" else "👩‍⚕️", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = schedule,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = TextSecondary
            )
        }
    }
}
