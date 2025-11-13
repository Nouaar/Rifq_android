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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.rifq_android.ui.theme.*

data class Pet(val id: String = "", val name: String, val breed: String, val status: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val pets = listOf(
        Pet("max", "Max", "Doberman", "Healthy"),
        Pet("luna", "Luna", "Siamese", "Due Soon")
    )

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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pets) { pet ->
                        SmallPetCard(
                            pet = pet,
                            onClick = { navController.navigate("pet_profile/${pet.id}") }
                        )
                    }
                }
            }

            // Featured pet summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pet avatar
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(PetAvatarBrown),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🐕",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Max",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "✓ Up-to-date | 1 med | 2.8kg",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BadgeSmall(text = "Healthy", kind = BadgeKind.Success)
                                BadgeSmall(text = "Due Soon", kind = BadgeKind.Warning)
                            }
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

            items(
                listOf(
                    Pair("John Smith", "Mon-Wed, 9 am - 6 pm"),
                    Pair("Lil Kim", "Tue-Fri, 9 am - 6 pm")
                )
            ) { vet ->
                VetListItem(
                    name = vet.first,
                    schedule = vet.second,
                    onClick = { /* navigate to vet details */ }
                )
            }

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
private fun SmallPetCard(pet: Pet, onClick: () -> Unit) {
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
                    .background(
                        if (pet.name == "Max") PetAvatarBrown else PetAvatarTan
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pet.name == "Max") "🐕" else "🐈",
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
                text = pet.breed,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
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
