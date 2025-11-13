package tn.rifq_android.ui.screens.petdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import tn.rifq_android.data.model.pet.Pet
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.PetDetailViewModel
import tn.rifq_android.viewmodel.PetDetailViewModelFactory
import tn.rifq_android.viewmodel.PetDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(navController: NavHostController, petId: String? = null) {
    val context = LocalContext.current
    val viewModel: PetDetailViewModel = viewModel(
        factory = PetDetailViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(petId) {
        if (!petId.isNullOrBlank()) {
            viewModel.loadPetDetails(petId)
        }
    }

    when (val state = uiState) {
        is PetDetailUiState.Loading -> {
            LoadingScreen()
        }
        is PetDetailUiState.Success -> {
            PetDetailContent(
                navController = navController,
                pet = state.pet
            )
        }
        is PetDetailUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { petId?.let { viewModel.loadPetDetails(it) } },
                onBack = { navController.popBackStack() }
            )
        }
        else -> {
            // Idle or invalid petId
            if (petId.isNullOrBlank()) {
                ErrorScreen(
                    message = "Invalid pet ID",
                    onRetry = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pet Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.KeyboardArrowLeft, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderBackground)
            )
        },
        containerColor = PageBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDetailContent(navController: NavHostController, pet: Pet) {
    Scaffold(
        topBar = { PetDetailTopBar(navController) },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(getPetColor(pet.species)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getPetEmoji(pet.species), fontSize = 70.sp)
                    }

                    Text(
                        text = pet.name,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = buildString {
                            append(pet.breed ?: pet.species.replaceFirstChar { it.uppercase() })
                            pet.age?.let { append(" • $it ${if (it == 1) "year" else "years"} old") }
                        },
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.navigate("medical_history") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, OrangeAccent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangeAccent
                    )
                ) {
                    Text(text = "📋", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MEDICAL HISTORY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PetStatCard(
                        statValue = pet.weight?.let { "$it kg" } ?: "N/A",
                        statLabel = "Weight",
                        modifier = Modifier.weight(1f)
                    )
                    PetStatCard(
                        statValue = pet.height?.let { "$it cm" } ?: "N/A",
                        statLabel = "Height",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Basic Info",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    pet.gender?.let {
                        PetInfoCard(infoLabel = "Gender", infoValue = it.replaceFirstChar { char -> char.uppercase() })
                    }
                    pet.breed?.let {
                        PetInfoCard(infoLabel = "Breed", infoValue = it)
                    }
                    pet.color?.let {
                        PetInfoCard(infoLabel = "Color", infoValue = it)
                    }
                    pet.microchipId?.let {
                        PetInfoCard(infoLabel = "Microchip ID", infoValue = it)
                    }

                    // Show message if no info available
                    if (pet.gender == null && pet.breed == null && pet.color == null && pet.microchipId == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "No additional information available",
                                modifier = Modifier.padding(20.dp),
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Health Status",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8F5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Healthy",
                                tint = GreenHealthy,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "All Vaccinations Up-to-date",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenHealthy
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "⚠", fontSize = 20.sp, color = Color(0xFFE67C00))
                            Text(
                                text = "Medication Active (Amoxicilline)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE67C00)
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { /* Edit pet info */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, OrangeAccent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangeAccent
                    )
                ) {
                    Text(text = "✏️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EDIT PET INFO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetDetailTopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                "Pet Profile",
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun PetStatCard(statValue: String, statLabel: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = statValue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )
            Text(
                text = statLabel,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PetInfoCard(infoLabel: String, infoValue: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = infoLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = infoValue,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangeAccent
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
