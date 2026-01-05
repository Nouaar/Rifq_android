package tn.rifq_android.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.DisposableEffect
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
import tn.rifq_android.data.model.pet.Pet
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.util.CalendarEvent
import tn.rifq_android.util.CalendarManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pet-specific Calendar Screen
 * Shows calendar view and Quick Add buttons for: Reminder, Appointment, Vaccination, Medication
 * iOS Reference: PetCalendarView.swift
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCalendarScreen(
    navController: NavHostController,
    petId: String?
) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    val profileViewModel: tn.rifq_android.viewmodel.profile.ProfileViewModel = viewModel(
        factory = tn.rifq_android.viewmodel.profile.ProfileViewModelFactory(context)
    )
    
    var pet by remember { mutableStateOf<Pet?>(null) }
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(false) }
    
    // Load pet data from profile
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    
    val profileState by profileViewModel.uiState.collectAsState()
    LaunchedEffect(petId, profileState) {
        if (petId != null && profileState is tn.rifq_android.viewmodel.profile.ProfileUiState.Success) {
            val pets = (profileState as tn.rifq_android.viewmodel.profile.ProfileUiState.Success).pets
            pet = pets.find { it.id == petId }
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
        if (hasPermission && petId != null) {
            loadPetCalendarEvents(calendarManager, petId) { events = it; isLoading = false }
        }
    }
    
    // Reload trigger - increments when we want to force a reload
    // This will trigger a reload when returning from add event screen
    var reloadTrigger by remember { mutableStateOf(0) }
    
    // Check permissions on launch and reload events when petId changes or reload is triggered
    LaunchedEffect(petId, reloadTrigger) {
        hasPermission = calendarManager.hasCalendarPermission()
        if (hasPermission && petId != null) {
            isLoading = true
            loadPetCalendarEvents(calendarManager, petId) { events = it; isLoading = false }
        }
    }
    
    // Reload events when returning from add event screen
    // Check savedStateHandle for a flag that indicates an event was added
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle?.get<Boolean>("eventAdded")) {
        val eventAdded = savedStateHandle?.get<Boolean>("eventAdded")
        if (eventAdded == true && petId != null) {
            // Clear the flag and reload events
            savedStateHandle?.set("eventAdded", false)
            reloadTrigger++
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "${pet?.name ?: "Pet"}'s Calendar",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = PageBackground,
        floatingActionButton = {
            if (hasPermission) {
                FloatingActionButton(
                    onClick = { 
                        navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                        navController.navigate("add_calendar_event")
                    },
                    containerColor = VetCanyon,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add Event")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Calendar Widget Section
            item {
                CalendarWidgetSection(events = events)
            }
            
            // Quick Add Section
            item {
                QuickAddSection(
                    petId = petId,
                    navController = navController
                )
            }
            
            // Upcoming Events Section (iOS Reference: eventsListSection lines 222-270)
            item {
                Text(
                    text = "UPCOMING EVENTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                        EmptyEventsCard()
                    }
                }
                else -> {
                    items(events) { event ->
                        EventCardEnhanced(event) {
                            // Navigate to event details or edit
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calendar Month View - Full calendar grid with event indicators
 * iOS Reference: CalendarMonthView (CalendarView.swift lines 319-448)
 */
@Composable
internal fun CalendarWidgetSection(
    events: List<CalendarEvent> = emptyList()
) {
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().time) }
    
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Navigation (iOS Reference: lines 366-384)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft, 
                        "Previous Month",
                        tint = VetCanyon
                    )
                }
                
                Text(
                    text = monthFormatter.format(
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                        }.time
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                IconButton(onClick = { 
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) {
                    Icon(
                        Icons.Default.KeyboardArrowRight, 
                        "Next Month",
                        tint = VetCanyon
                    )
                }
            }
            
            // Weekday Headers (iOS Reference: lines 386-396)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Calendar Grid (iOS Reference: lines 398-412)
            val days = getCalendarDays(currentYear, currentMonth)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 6 weeks of days
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (day in 0 until 7) {
                            val index = week * 7 + day
                            if (index < days.size) {
                                val date = days[index]
                                CalendarDayView(
                                    date = date,
                                    isSelected = isSameDay(date, selectedDate),
                                    isToday = isSameDay(date, Calendar.getInstance().time),
                                    isCurrentMonth = date.get(Calendar.MONTH) == currentMonth,
                                    hasEvent = hasEvent(date, events),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedDate = date.time
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calendar Day View - Individual day cell
 * iOS Reference: CalendarDayView (CalendarView.swift lines 452-491)
 */
@Composable
private fun CalendarDayView(
    date: Calendar,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    hasEvent: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) VetCanyon else Color.Transparent)
            .clickable { onTap() }
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(2.dp, VetCanyon, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = date.get(Calendar.DAY_OF_MONTH).toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isSelected -> Color.White
                    !isCurrentMonth -> TextSecondary.copy(alpha = 0.3f)
                    isToday -> VetCanyon
                    else -> TextPrimary
                }
            )
            
            if (hasEvent) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else VetCanyon)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Get all calendar days for a month (42 days = 6 weeks)
 * iOS Reference: calendarDays computed property (lines 414-444)
 */
private fun getCalendarDays(year: Int, month: Int): List<Calendar> {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    
    // Get the first day of the month
    val firstDayOfMonth = calendar.clone() as Calendar
    
    // Get day of week for first day (0 = Sunday)
    val firstDayWeekday = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
    
    // Calculate the first day to show (might be from previous month)
    val firstDayToShow = (firstDayOfMonth.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, -firstDayWeekday)
    }
    
    // Generate exactly 42 days (6 weeks x 7 days)
    val days = mutableListOf<Calendar>()
    for (i in 0 until 42) {
        val day = (firstDayToShow.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, i)
        }
        days.add(day)
    }
    
    return days
}

private fun isSameDay(cal1: Calendar, cal2: Date): Boolean {
    val c1 = cal1.clone() as Calendar
    val c2 = Calendar.getInstance().apply { time = cal2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = date1 }
    val c2 = Calendar.getInstance().apply { time = date2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun hasEvent(date: Calendar, events: List<CalendarEvent>): Boolean {
    return events.any { event ->
        val eventCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
        date.get(Calendar.YEAR) == eventCal.get(Calendar.YEAR) &&
        date.get(Calendar.DAY_OF_YEAR) == eventCal.get(Calendar.DAY_OF_YEAR)
    }
}

/**
 * Quick Add Section - Action buttons for adding events
 * iOS Reference: quickAddSection (CalendarView.swift lines 179-220)
 */
@Composable
private fun QuickAddSection(
    petId: String?,
    navController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "QUICK ADD",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        
        // Grid layout - 2 columns (iOS Reference: LazyVGrid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAddButton(
                title = "Medication",
                icon = Icons.Default.Info, // Closest to pills.fill
                color = Color(0xFFFF9500),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "MEDICATION")
                navController.navigate("add_calendar_event")
            }
            
            QuickAddButton(
                title = "Vaccination",
                icon = Icons.Default.Add, // Closest to syringe.fill
                color = Color(0xFF34C759),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "VACCINATION")
                navController.navigate("add_calendar_event")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAddButton(
                title = "Appointment",
                icon = Icons.Default.DateRange,
                color = Color(0xFF007AFF),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "APPOINTMENT")
                navController.navigate("add_calendar_event")
            }
            
            QuickAddButton(
                title = "Reminder",
                icon = Icons.Default.Notifications,
                color = Color(0xFFAF52DE),
                modifier = Modifier.weight(1f)
            ) {
                navController.currentBackStackEntry?.savedStateHandle?.set("petId", petId ?: "")
                navController.currentBackStackEntry?.savedStateHandle?.set("type", "REMINDER")
                navController.navigate("add_calendar_event")
            }
        }
    }
}

/**
 * Quick Add Button - Individual action card
 * iOS Reference: CalendarActionCard (CalendarView.swift lines 275-317)
 */
@Composable
private fun QuickAddButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

/**
 * Permission Request Card - Calendar access prompt
 * iOS Reference: authorizationView (CalendarView.swift lines 112-156)
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
        Spacer(modifier = Modifier.weight(1f))
        
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
            text = "To track your pet's appointments, medications, and vaccinations, please allow calendar access.",
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
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Empty Events Card - Shown when no events exist
 * iOS Reference: Empty state in eventsListSection (CalendarView.swift lines 246-256)
 */
@Composable
private fun EmptyEventsCard() {
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
 * Event Card - Displays individual calendar event
 * iOS Reference: EventRowView (CalendarView.swift lines 493-563)
 */
@Composable
private fun EventCardEnhanced(event: CalendarEvent, onClick: () -> Unit) {
    val eventColor = getEventColor(event)
    val eventIcon = getEventIcon(event)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Type Icon (iOS Reference: lines 500-505)
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
            
            // Event Details (iOS Reference: lines 507-527)
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
                Text(
                    text = formatEventDate(event.startTime),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            
            // Chevron (iOS Reference: lines 531-534)
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private fun getEventColor(event: CalendarEvent): Color {
    // Determine color based on event description/type
    return when {
        event.description.contains("Medication", ignoreCase = true) -> Color(0xFFFF9500)
        event.description.contains("Vaccination", ignoreCase = true) -> Color(0xFF34C759)
        event.description.contains("Appointment", ignoreCase = true) -> Color(0xFF007AFF)
        event.description.contains("Reminder", ignoreCase = true) -> Color(0xFFAF52DE)
        else -> VetCanyon
    }
}

private fun getEventIcon(event: CalendarEvent): androidx.compose.ui.graphics.vector.ImageVector {
    // Determine icon based on event description/type
    return when {
        event.description.contains("Medication", ignoreCase = true) -> Icons.Default.Info
        event.description.contains("Vaccination", ignoreCase = true) -> Icons.Default.Add
        event.description.contains("Appointment", ignoreCase = true) -> Icons.Default.DateRange
        event.description.contains("Reminder", ignoreCase = true) -> Icons.Default.Notifications
        else -> Icons.Default.DateRange
    }
}

private fun formatEventDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.US)
    return formatter.format(Date(timestamp))
}

private fun loadPetCalendarEvents(
    calendarManager: CalendarManager,
    petId: String,
    onEventsLoaded: (List<CalendarEvent>) -> Unit
) {
    try {
        val events = calendarManager.loadEventsForPet(petId)
        onEventsLoaded(events)
    } catch (e: Exception) {
        android.util.Log.e("PetCalendarScreen", "Failed to load events: ${e.message}")
        onEventsLoaded(emptyList())
    }
}
