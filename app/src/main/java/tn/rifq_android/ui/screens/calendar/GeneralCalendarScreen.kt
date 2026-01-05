package tn.rifq_android.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.util.CalendarEvent
import tn.rifq_android.util.CalendarManager
import tn.rifq_android.viewmodel.profile.ProfileUiState
import tn.rifq_android.viewmodel.profile.ProfileViewModel
import tn.rifq_android.viewmodel.profile.ProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * General Calendar Screen - Shows all pets' events in one calendar
 * iOS Reference: GeneralCalendarView (MyPetsView.swift lines 389-580)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralCalendarScreen(navController: NavHostController) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    
    // Load profile to get pets
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    
    val profileState by profileViewModel.uiState.collectAsState()
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission && profileState is ProfileUiState.Success) {
            loadAllPetsEvents(calendarManager, profileState, events = { events = it; isLoading = false })
        }
    }
    
    // Check permissions and load events
    LaunchedEffect(profileState) {
        hasPermission = calendarManager.hasCalendarPermission()
        if (hasPermission && profileState is ProfileUiState.Success) {
            isLoading = true
            loadAllPetsEvents(calendarManager, profileState, events = { events = it; isLoading = false })
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "All Pets Calendar",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Calendar Month View (iOS Reference: lines 404-411)
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    CalendarWidgetSection(
                        events = events
                    )
                }
            }
            
            // Events List Section (iOS Reference: eventsListSection lines 435-504)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "UPCOMING EVENTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            when {
                !hasPermission -> {
                    item {
                        PermissionRequestCard(
                            onRequestPermission = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CALENDAR,
                                        Manifest.permission.WRITE_CALENDAR
                                    )
                                )
                            }
                        )
                    }
                }
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VetCanyon)
                        }
                    }
                }
                events.isEmpty() -> {
                    item {
                        EmptyEventsState()
                    }
                }
                else -> {
                    val upcomingEvents = events.filter { it.startTime >= System.currentTimeMillis() }
                        .sortedBy { it.startTime }
                    
                    if (upcomingEvents.isEmpty()) {
                        item {
                            AllCaughtUpState()
                        }
                    } else {
                        items(upcomingEvents.take(20)) { event ->
                            GeneralEventRow(
                                event = event,
                                pets = if (profileState is ProfileUiState.Success) {
                                    (profileState as ProfileUiState.Success).pets
                                } else {
                                    emptyList()
                                },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                // Navigate to event detail or edit
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * General Event Row - Shows event with pet name
 * iOS Reference: GeneralEventRowWithPet (MyPetsView.swift lines 506-580)
 */
@Composable
private fun GeneralEventRow(
    event: CalendarEvent,
    pets: List<tn.rifq_android.data.model.pet.Pet>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val eventColor = getEventColor(event)
    val eventIcon = getEventIcon(event)
    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.US) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Type Icon (iOS Reference: lines 517-522)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(eventColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = eventIcon,
                    contentDescription = null,
                    tint = eventColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Event Details (iOS Reference: lines 524-550)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                
                // Pet name and date on same line (iOS Reference: lines 528-538)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Try to find pet name from event description or booking info
                    val petName = event.description.substringBefore(":")
                        .takeIf { it.length < 30 }
                    
                    if (!petName.isNullOrBlank() && petName != event.description) {
                        Text(
                            text = petName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = VetCanyon
                        )
                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    
                    Text(
                        text = dateFormatter.format(Date(event.startTime)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
            
            // Chevron (iOS Reference: similar to other event rows)
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Empty Events State - No events scheduled
 * iOS Reference: lines 447-455
 */
@Composable
private fun EmptyEventsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "No events",
            modifier = Modifier.size(40.dp),
            tint = TextSecondary
        )
        Text(
            text = "No events scheduled",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

/**
 * All Caught Up State - No upcoming events
 * iOS Reference: lines 457-466
 */
@Composable
private fun AllCaughtUpState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "All caught up",
            modifier = Modifier.size(40.dp),
            tint = Color(0xFF34C759) // Green
        )
        Text(
            text = "All caught up!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

/**
 * Permission Request Card
 * iOS Reference: Similar to authorization prompts in other screens
 */
@Composable
private fun PermissionRequestCard(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = VetCanyon
        )
        
        Text(
            text = "Calendar Access Required",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Text(
            text = "To see all pet events in one place, please allow calendar access.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = VetCanyon),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .widthIn(max = 200.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                "Grant Access",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

private fun loadAllPetsEvents(
    calendarManager: CalendarManager,
    profileState: ProfileUiState,
    events: (List<CalendarEvent>) -> Unit
) {
    if (profileState !is ProfileUiState.Success) {
        events(emptyList())
        return
    }
    
    try {
        val allEvents = mutableListOf<CalendarEvent>()
        profileState.pets.forEach { pet ->
            pet.id?.let { petId ->
                val petEvents = calendarManager.loadEventsForPet(petId)
                allEvents.addAll(petEvents)
            }
        }
        events(allEvents.sortedBy { it.startTime })
    } catch (e: Exception) {
        Log.e("GeneralCalendarScreen", "Failed to load events: ${e.message}")
        events(emptyList())
    }
}

private fun getEventColor(event: CalendarEvent): Color {
    return when {
        event.description.contains("Medication", ignoreCase = true) -> Color(0xFFFF9500)
        event.description.contains("Vaccination", ignoreCase = true) -> Color(0xFF34C759)
        event.description.contains("Appointment", ignoreCase = true) -> Color(0xFF007AFF)
        event.description.contains("Reminder", ignoreCase = true) -> Color(0xFFAF52DE)
        else -> VetCanyon
    }
}

private fun getEventIcon(event: CalendarEvent): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        event.description.contains("Medication", ignoreCase = true) -> Icons.Default.Info
        event.description.contains("Vaccination", ignoreCase = true) -> Icons.Default.Add
        event.description.contains("Appointment", ignoreCase = true) -> Icons.Default.DateRange
        event.description.contains("Reminder", ignoreCase = true) -> Icons.Default.Notifications
        else -> Icons.Default.DateRange
    }
}
