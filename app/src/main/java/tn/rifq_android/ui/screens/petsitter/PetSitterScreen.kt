package tn.rifq_android.ui.screens.petsitter

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
import tn.rifq_android.ui.theme.*

data class PetSitter(
    val id: String,
    val name: String,
    val rating: String,
    val reviewCount: String,
    val distance: String,
    val price: String,
    val availability: String,
    val emoji: String,
    val avatarBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSitterScreen(navController: NavHostController) {
    // TODO: Use PetSitterViewModel to fetch sitters from backend API
    // Example: val viewModel: PetSitterViewModel = viewModel(factory = PetSitterViewModelFactory(LocalContext.current))
    // val sitters by viewModel.sitters.collectAsState()
    val sitters = emptyList<PetSitter>() // Replace with dynamic data from backend API

    Scaffold(
        topBar = { PetSitterTopBar(navController) },
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
                    text = "Find a Pet Sitter",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(sitters) { sitter ->
                PetSitterCard(sitter, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetSitterTopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                "Pet Sitters",
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
        actions = {
            IconButton(onClick = { /* Filter */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Filter",
                    tint = OrangeAccent
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun PetSitterCard(sitter: PetSitter, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to sitter details */ },
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
                        .background(sitter.avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = sitter.emoji, fontSize = 32.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = sitter.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "${sitter.rating}★", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "(${sitter.reviewCount})", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "•", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "📍", fontSize = 12.sp)
                        Text(text = sitter.distance, fontSize = 13.sp, color = RedLocation)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = sitter.price,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                        Text(text = "•", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = sitter.availability,
                            fontSize = 13.sp,
                            color = if (sitter.availability == "Available") Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View details",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
