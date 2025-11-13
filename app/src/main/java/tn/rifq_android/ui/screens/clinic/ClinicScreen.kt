package tn.rifq_android.ui.screens.clinic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*

// Data class for vet
data class Vet(
    val id: String,
    val name: String,
    val specialties: String,
    val rating: String,
    val reviewCount: String,
    val distance: String,
    val status: String,
    val avatarBg: Color,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicScreen(navController: NavHostController) {
    var selectedFilter by remember { mutableStateOf("Specialty") }

    // TODO: Use VetViewModel to fetch vets from backend API
    // Example: val viewModel: VetViewModel = viewModel(factory = VetViewModelFactory(LocalContext.current))
    // val vets by viewModel.vets.collectAsState()
    val vets = emptyList<Vet>() // Replace with dynamic data from backend API

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Find a Vet",
                showBackButton = false,
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
                }
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Find a Vet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        label = "Specialty",
                        selected = selectedFilter == "Specialty",
                        onClick = { selectedFilter = "Specialty" }
                    )
                    FilterChip(
                        label = "Distance",
                        selected = selectedFilter == "Distance",
                        onClick = { selectedFilter = "Distance" }
                    )
                    FilterChip(
                        label = "24/7",
                        selected = selectedFilter == "24/7",
                        onClick = { selectedFilter = "24/7" }
                    )
                }
            }

            items(vets) { vet ->
                VetCard(vet, navController)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ChipSelectedBg,
            selectedLabelColor = ChipSelectedText,
            containerColor = ChipUnselectedBg,
            labelColor = ChipUnselectedText
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) ChipSelectedBg else Color(0xFFE0E0E0),
            selectedBorderColor = ChipSelectedBg,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun VetCard(vet: Vet, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("vet_profile/${vet.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(vet.avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = vet.emoji, fontSize = 32.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = vet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = vet.specialties, fontSize = 13.sp, color = TextSecondary)
                        Text(text = "•", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "${vet.rating}★", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "(${vet.reviewCount})", fontSize = 13.sp, color = TextSecondary)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "📍", fontSize = 12.sp)
                        Text(text = vet.distance, fontSize = 13.sp, color = RedLocation)
                        Text(text = "•", fontSize = 13.sp, color = TextSecondary)
                        if (vet.status == "24/7") {
                            Text(text = "🏥", fontSize = 12.sp)
                        }
                        Text(
                            text = vet.status,
                            fontSize = 13.sp,
                            color = if (vet.status == "24/7") TextSecondary else Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "View details",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

