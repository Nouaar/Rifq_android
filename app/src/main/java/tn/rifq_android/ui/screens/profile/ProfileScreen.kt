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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.data.storage.ThemePreference
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.data.storage.UserManager
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.profile.ProfileAction
import tn.rifq_android.viewmodel.profile.ProfileUiState
import tn.rifq_android.viewmodel.profile.ProfileViewModel
import tn.rifq_android.viewmodel.profile.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val themePreference = remember { ThemePreference(context) }
    val isDarkMode by themePreference.isDarkMode.collectAsState(initial = false)

    // Dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.UserDeleted) {
            onLogout()
        }
    }

    // Handle action results
    LaunchedEffect(actionState) {
        when (actionState) {
            is ProfileAction.Success -> {
                android.widget.Toast.makeText(
                    context,
                    (actionState as ProfileAction.Success).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                showEditDialog = false
                showDeleteDialog = false
                viewModel.resetActionState()
            }
            is ProfileAction.Error -> {
                android.widget.Toast.makeText(
                    context,
                    (actionState as ProfileAction.Error).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                viewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Profile",
                showBackButton = false,
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
                }
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

                        // Edit Profile Button
                        SettingsCard(
                            icon = "✏️",
                            label = "Edit Profile",
                            onClick = { showEditDialog = true }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Dark Mode Toggle
                        ThemeToggleCard(
                            isDarkMode = isDarkMode,
                            onToggle = { enabled ->
                                coroutineScope.launch {
                                    themePreference.setDarkMode(enabled)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Delete Account Button
                        SettingsCard(
                            icon = "🗑️",
                            label = "Delete Account",
                            onClick = { showDeleteDialog = true },
                            isDestructive = true
                        )

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

        // Edit Profile Dialog
        if (showEditDialog && uiState is ProfileUiState.Success) {
            EditProfileDialog(
                user = (uiState as ProfileUiState.Success).user,
                onDismiss = { showEditDialog = false },
                onSave = { name, email, photoFile ->
                    viewModel.updateProfileWithImage(
                        name = name,
                        email = email,
                        photoFile = photoFile
                    )
                }
            )
        }

        // Delete Account Dialog
        if (showDeleteDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    viewModel.deleteAccount()
                },
                onDismiss = { showDeleteDialog = false }
            )
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
fun SettingsCard(
    icon: String,
    label: String,
    onClick: () -> Unit = {},
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                color = if (isDestructive) androidx.compose.ui.graphics.Color.Red else TextPrimary
            )
        }
    }
}

@Composable
fun ThemeToggleCard(isDarkMode: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDarkMode) "🌙" else "☀️",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isDarkMode) "Dark Mode" else "Light Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OrangeAccent,
                    checkedTrackColor = OrangeAccent.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = TextSecondary.copy(alpha = 0.3f)
                )
            )
        }
    }
}



