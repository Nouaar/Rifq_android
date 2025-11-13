package tn.rifq_android.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch
import tn.rifq_android.data.model.pet.Pet
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.data.storage.UserManager
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.ProfileUiState
import tn.rifq_android.viewmodel.ProfileViewModel
import tn.rifq_android.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.UserDeleted) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle notification */ }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = OrangeAccent
                        )
                    }
                    IconButton(onClick = { viewModel.loadProfile() }) {
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
        },
        containerColor = PageBackground
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image Section
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(PetAvatarBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(80.dp),
                            tint = BlueAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name and Title
                    Text(
                        state.user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = TextPrimary
                    )

                    Text(
                        state.user.role.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            number = "${state.pets.size}",
                            label = "Pets",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            number = "0",
                            label = "Appointments",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Account Info Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Account Info",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard(label = "Email", value = state.user.email)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoCard(label = "Phone", value = state.user.phone ?: "Not set")
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoCard(label = "Balance", value = "${state.user.balance ?: 0} DT")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // My Pets Section
                    if (state.pets.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "My Pets",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            state.pets.forEachIndexed { index, pet ->
                                PetCard(
                                    pet = pet,
                                    backgroundColor = if (index % 2 == 0) PetAvatarBrown else PetAvatarTan
                                )
                                if (index < state.pets.size - 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Settings Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SettingsCard(icon = "🔔", label = "Notifications")
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsCard(icon = "🌐", label = "Language")
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsCard(icon = "🔒", label = "Privacy")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Log Out Button
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val tokenManager = TokenManager(context)
                                val userManager = UserManager(context)
                                tokenManager.clearTokens()
                                userManager.clearUserId()
                                onLogout()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OrangeAccent
                        ),
                        border = BorderStroke(2.dp, OrangeAccent)
                    ) {
                        Text(
                            "LOG OUT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            ProfileUiState.Idle -> {}

            ProfileUiState.UserDeleted -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun StatCard(number: String, label: String, modifier: Modifier = Modifier) {
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
                text = number,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun InfoCard(label: String, value: String) {
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
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangeAccent
            )
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    backgroundColor: Color
) {
    // Determine emoji based on pet species
    val petEmoji = when (pet.species.lowercase()) {
        "dog" -> "🐕"
        "cat" -> "🐈"
        "bird" -> "🐦"
        "fish" -> "🐠"
        "rabbit" -> "🐰"
        "hamster" -> "🐹"
        else -> "🐾"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to pet detail */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet avatar with emoji
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = petEmoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${pet.breed ?: pet.species} • ${pet.age ?: "Unknown"} ${if (pet.age == 1) "year" else "years"}",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "View pet",
                tint = TextSecondary
            )
        }
    }
}

@Composable
fun SettingsCard(icon: String, label: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle settings click */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
