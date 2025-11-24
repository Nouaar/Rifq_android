package tn.rifq_android.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import tn.rifq_android.viewmodel.ai.PetAIViewModel
import tn.rifq_android.viewmodel.profile.ProfileViewModel
import tn.rifq_android.viewmodel.profile.ProfileViewModelFactory
import tn.rifq_android.viewmodel.profile.ProfileUiState
import tn.rifq_android.data.storage.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    themePreference: tn.rifq_android.data.storage.ThemePreference
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // AI ViewModel
    val tokenManager = remember { TokenManager(context) }
    val aiViewModel = remember { PetAIViewModel(tokenManager) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            LoadingScreen()
        }
        is ProfileUiState.Success -> {
            // Load AI content for all pets
            LaunchedEffect(state.pets) {
                if (state.pets.isNotEmpty()) {
                    val petIds = state.pets.mapNotNull { it.id }
                    if (petIds.isNotEmpty()) {
                        aiViewModel.generateContentForPets(petIds, silent = true)
                    }
                }
            }
            
            HomeScreenContent(
                navController = navController,
                pets = state.pets,
                userName = state.user.name,
                themePreference = themePreference,
                aiViewModel = aiViewModel
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
    userName: String,
    themePreference: tn.rifq_android.data.storage.ThemePreference,
    aiViewModel: PetAIViewModel
) {
    // Notification badge manager
    val badgeViewModel: tn.rifq_android.util.NotificationBadgeViewModel = viewModel()
    val notificationCount by badgeViewModel.notificationCount.collectAsState()
    val messageCount by badgeViewModel.messageCount.collectAsState()
    
    // Refresh badge counts periodically
    LaunchedEffect(Unit) {
        badgeViewModel.refresh()
        // Refresh every 30 seconds
        while (true) {
            kotlinx.coroutines.delay(30000)
            badgeViewModel.refresh()
        }
    }
    
    // Fade in animation when screen loads
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val petTips by aiViewModel.petTips.collectAsState()
    val petStatuses by aiViewModel.petStatuses.collectAsState()
    val petReminders by aiViewModel.petReminders.collectAsState()
    val isAILoading by aiViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = { 
            TopNavBar(
                title = "Home",
                navController = navController,
                showBackButton = false,
                onMessagesClick = { navController.navigate("conversations") },
                onNotificationsClick = { navController.navigate("notifications") },
                messageCount = messageCount,
                notificationCount = notificationCount
            ) 
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .graphicsLayer { this.alpha = alpha },
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Daily Tips section
            item {
                DailyTipsSection(
                    navController = navController,
                    petTips = petTips,
                    pets = pets,
                    isLoading = isAILoading
                )
            }

            // Pet Health Snapshot
            item {
                PetHealthSnapshotSection(
                    pets = pets,
                    navController = navController,
                    petStatuses = petStatuses
                )
            }

            // Upcoming Reminders
            item {
                UpcomingRemindersSection(
                    petReminders = petReminders,
                    pets = pets,
                    isLoading = isAILoading
                )
            }

            // Find Care Hub (iOS Reference: FindHubView.swift)
            item {
                FindCareButton(navController = navController)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// MARK: - Sections

@Composable
private fun DailyTipsSection(
    navController: NavHostController,
    petTips: Map<String, List<tn.rifq_android.data.model.ai.PetTip>>,
    pets: List<tn.rifq_android.data.model.pet.Pet>,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Tips",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            OutlinedButton(
                onClick = { navController.navigate("add_pet") },
                shape = RoundedCornerShape(100.dp),
                border = BorderStroke(1.dp, VetCanyon),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VetCanyon,
                    containerColor = CardBackground
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = VetCanyon
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add a pet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Collect all tips from all pets
        val allTips = petTips.values.flatten()

        when {
            isLoading && allTips.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VetCanyon, modifier = Modifier.size(32.dp))
                }
            }
            allTips.isNotEmpty() -> {
                // Tips carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(allTips.size) { index ->
                        DailyTipCard(tip = allTips[index])
                    }
                }
            }
            else -> {
                // Show mock tips as fallback
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(dailyTipsMock.size) { index ->
                        DailyTipCard(tip = dailyTipsMock[index])
                    }
                }
            }
        }
    }
}

@Composable
private fun PetHealthSnapshotSection(
    pets: List<tn.rifq_android.data.model.pet.Pet>,
    navController: NavHostController,
    petStatuses: Map<String, tn.rifq_android.data.model.ai.PetStatus>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Pet Health Snapshot",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (pets.isEmpty()) {
            Text(
                text = "No pets yet",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pets.forEach { pet ->
                    PetStatusRow(
                        pet = pet,
                        status = petStatuses[pet.id],
                        onClick = { navController.navigate("pet_detail/${pet.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingRemindersSection(
    petReminders: Map<String, List<tn.rifq_android.data.model.ai.PetReminder>>,
    pets: List<tn.rifq_android.data.model.pet.Pet>,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Upcoming Reminders",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Collect all reminders from all pets
        val allReminders = petReminders.values.flatten()

        when {
            isLoading && allReminders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VetCanyon, modifier = Modifier.size(32.dp))
                }
            }
            allReminders.isNotEmpty() -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allReminders.take(5).forEach { reminder ->
                        AIReminderRow(reminder = reminder)
                    }
                }
            }
            else -> {
                // Show mock reminders as fallback
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    remindersMock.forEach { reminder ->
                        ReminderRow(reminder = reminder)
                    }
                }
            }
        }
    }
}

// MARK: - Cards

data class DailyTip(
    val emoji: String,
    val title: String,
    val detail: String
)

@Composable
private fun DailyTipCard(tip: Any) {
    val (emoji, title, detail) = when (tip) {
        is tn.rifq_android.data.model.ai.PetTip -> Triple(tip.emoji, tip.title, tip.detail)
        is DailyTip -> Triple(tip.emoji, tip.title, tip.detail)
        else -> Triple("🐾", "Tip", "")
    }
    
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 36.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = detail,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PetStatusRow(
    pet: tn.rifq_android.data.model.pet.Pet,
    status: tn.rifq_android.data.model.ai.PetStatus?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pet avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                if (pet.photo != null) {
                    Image(
                        painter = rememberAsyncImagePainter(pet.photo),
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = PetUtils.getPetEmoji(pet.species),
                        fontSize = 28.sp
                    )
                }
            }

            // Pet info and status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pet.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // Status pills
                    if (status != null && status.pills.isNotEmpty()) {
                        status.pills.forEach { pill ->
                            StatusPillChip(pill = pill)
                        }
                    }
                }

                // Status summary or breed
                Text(
                    text = status?.summary ?: (pet.breed ?: pet.species.replaceFirstChar { it.uppercase() }),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatusPillChip(pill: tn.rifq_android.data.model.ai.StatusPill) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(pill.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = pill.text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = pill.textColor
        )
    }
}

// AI Reminder Row
@Composable
private fun AIReminderRow(reminder: tn.rifq_android.data.model.ai.PetReminder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(reminder.tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminder.icon,
                    fontSize = 18.sp
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = reminder.detail,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
                Text(
                    text = reminder.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = reminder.tint
                )
            }
        }
    }
}

// Legacy Reminder Row for mock data
data class ReminderData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val detail: String,
    val date: String,
    val tint: Color
)

@Composable
private fun ReminderRow(reminder: ReminderData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(reminder.tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reminder.icon,
                    contentDescription = null,
                    tint = reminder.tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = reminder.detail,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = reminder.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = reminder.tint
                )
            }
        }
    }
}

// MARK: - Mock Data

private val dailyTipsMock = listOf(
    DailyTip(
        emoji = "🥕",
        title = "Fresh Nutrition",
        detail = "Rotate crunchy vegetables with high-protein treats to keep meals balanced."
    ),
    DailyTip(
        emoji = "🚶‍♀️",
        title = "Stay Active",
        detail = "Short walks twice a day help maintain healthy joints and reduce anxiety."
    ),
    DailyTip(
        emoji = "🪥",
        title = "Dental Care",
        detail = "Brush teeth 2-3 times per week to prevent plaque build-up and gum issues."
    )
)

private val remindersMock = listOf(
    ReminderData(
        icon = Icons.Default.Notifications,
        title = "Luna • Vaccination Booster",
        detail = "Feline FVCRP booster due soon.",
        date = "Dec 24 • 9:00 AM",
        tint = VetCanyon
    ),
    ReminderData(
        icon = Icons.Default.Warning,
        title = "Max • Heartworm Prevention",
        detail = "Monthly chewable due this weekend.",
        date = "Dec 26 • 10:00 AM",
        tint = Color(0xFF3B82F6)
    ),
    ReminderData(
        icon = Icons.Default.Favorite,
        title = "Grooming Session",
        detail = "Schedule grooming and nail trim.",
        date = "Dec 29 • 2:00 PM",
        tint = Color(0xFF9333EA)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPetsTopBar() {
    // Using the new TopNavBar component for consistency
    TopNavBar(
        title = "Home",
        showBackButton = false
    )
}

@Composable
private fun SmallPetCard(
    pet: tn.rifq_android.data.model.pet.Pet, 
    onClick: () -> Unit,
    itemIndex: Int = 0
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "petCardScale"
    )
    
    // Staggered fade-in animation
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "petCardFade_$itemIndex"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 20f,
        animationSpec = tween(durationMillis = 400),
        label = "petCardOffset_$itemIndex"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(itemIndex * 50L)
        isVisible = true
    }

    Card(
        modifier = Modifier
            .width(130.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationY = offsetY
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    .background(PetUtils.getPetColor(pet.species).copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                if (!pet.photo.isNullOrBlank()) {
                    // Display photo from Cloudinary
                    Image(
                        painter = rememberAsyncImagePainter(pet.photo),
                        contentDescription = "${pet.name}'s photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display emoji fallback
                    Text(
                        text = PetUtils.getPetEmoji(pet.species),
                        fontSize = 40.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = pet.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                icon = "📖",
                label = "Bookings",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("booking_list") }
            QuickActionCard(
                icon = "➕",
                label = "New Booking",
                modifier = Modifier.weight(1f)
            ) { navController.navigate("booking_create") }
        }
    }
}

@Composable
private fun QuickActionCard(icon: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionCardScale"
    )

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.35f))
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
private fun FindCareButton(navController: NavHostController) {
    Card(
        onClick = { navController.navigate("find_hub") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = VetCanyon.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Find Care",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Discover trusted vets and pet sitters near you",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(VetCanyon, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Find Care",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

