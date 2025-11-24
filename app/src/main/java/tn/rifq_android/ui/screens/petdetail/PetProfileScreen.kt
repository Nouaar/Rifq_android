package tn.rifq_android.ui.screens.petdetail

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.ui.utils.PetUtils
import tn.rifq_android.viewmodel.pet.PetDetailViewModel
import tn.rifq_android.viewmodel.pet.PetDetailViewModelFactory
import tn.rifq_android.viewmodel.pet.PetDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfileScreen(navController: NavHostController, petId: String? = null) {
    val context = LocalContext.current
    val viewModel: PetDetailViewModel = viewModel(
        factory = PetDetailViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            PetProfileContent(
                navController = navController,
                pet = state.pet,
                onDeleteClick = { showDeleteDialog = true }
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
            if (petId.isNullOrBlank()) {
                ErrorScreen(
                    message = "Invalid pet ID",
                    onRetry = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeletePetDialog(
            petName = (uiState as? PetDetailUiState.Success)?.pet?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                petId?.let { viewModel.deletePet(it) }
                showDeleteDialog = false
                navController.popBackStack()
            }
        )
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
private fun ErrorScreen(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Profile",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetProfileContent(
    navController: NavHostController,
    pet: tn.rifq_android.data.model.pet.Pet,
    onDeleteClick: () -> Unit
) {

    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = pet.name,
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .graphicsLayer { this.alpha = alpha }
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }


            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (!pet.photo.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(pet.photo),
                            contentDescription = "${pet.name}'s photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = PetUtils.getPetEmoji(pet.species),
                            fontSize = 60.sp
                        )
                    }

                    Text(
                        text = pet.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "${pet.breed ?: "Unknown"} • ${formatAge(pet.age)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }


            item {
                OutlinedButton(
                    onClick = { navController.navigate("medical_history/${pet.id}") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, VetCanyon),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VetCanyon),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = "MEDICAL HISTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBox(
                        title = "Weight",
                        value = pet.weight?.let { "$it kg" } ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Height",
                        value = pet.height?.let { "$it cm" } ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            item {
                InfoSection(title = "Basic Info") {
                    InfoRow(label = "Age", value = formatAge(pet.age))
                    InfoRow(label = "Breed", value = pet.breed ?: "—")
                    InfoRow(label = "Gender", value = pet.gender ?: "—")
                    InfoRow(label = "Color", value = pet.color ?: "—")
                    InfoRow(label = "Microchip ID", value = pet.microchipId ?: "—")
                }
            }


            item {
                InfoSection(title = "Health Status") {
                    HealthCard(
                        icon = "✓",
                        text = "All Vaccinations Up-to-date",
                        color = Color.Green
                    )


                    pet.medicalHistory?.currentMedications?.forEach { medication ->
                        HealthCard(
                            icon = "⚠",
                            text = medication,
                            color = Color(0xFFFF9500)
                        )
                    }
                }
            }


            item {
                CalendarSection(pet = pet, navController = navController)
            }


            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("edit_pet/${pet.id}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.4.dp, VetCanyon),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VetCanyon)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EDIT PET INFO",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.4.dp, Color.Red),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DELETE PET",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun HealthCard(icon: String, text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 14.sp,
                color = color
            )
        }

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun CalendarSection(
    pet: tn.rifq_android.data.model.pet.Pet,
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UPCOMING EVENTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            TextButton(
                onClick = { navController.navigate("calendar/${pet.id}") },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VetCanyon
                )
            }
        }

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
                    text = "No upcoming events",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DeletePetDialog(
    petName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Pet") },
        text = {
            Text("Are you sure you want to delete $petName? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatAge(age: Int?): String {
    return when (age) {
        null -> "Unknown age"
        0 -> "Less than 1 year"
        1 -> "1 year old"
        else -> "$age years old"
    }
}
