package tn.rifq_android.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import kotlinx.coroutines.flow.firstOrNull
import tn.rifq_android.data.storage.ThemePreference
import tn.rifq_android.ui.components.BottomNavBar
import tn.rifq_android.ui.screens.home.HomeScreen
import tn.rifq_android.ui.screens.profile.ProfileScreen
import tn.rifq_android.ui.screens.discover.DiscoverScreen
import tn.rifq_android.ui.screens.calendar.CalendarScreen
import tn.rifq_android.ui.screens.join.JoinScreen
import tn.rifq_android.ui.screens.chat.ChatAIScreen
import tn.rifq_android.ui.screens.petsitter.PetSitterScreen
import tn.rifq_android.ui.screens.petsitter.AvailableSittersScreen
import tn.rifq_android.ui.screens.vet.FindVetScreen
import tn.rifq_android.ui.screens.medical.MedicalHistoryScreen
import tn.rifq_android.ui.screens.medical.MedicalScreen
import tn.rifq_android.ui.screens.profile.EditProfileScreen
import tn.rifq_android.ui.screens.help.HelpScreen
import tn.rifq_android.ui.screens.map.MapScreen
import tn.rifq_android.ui.screens.chat.ConversationsListScreen
import tn.rifq_android.ui.screens.chat.ChatViewScreen
import tn.rifq_android.ui.screens.auth.VerifyEmailScreen
import tn.rifq_android.ui.screens.pet.EditPetScreen
import tn.rifq_android.viewmodel.auth.AuthViewModel
import tn.rifq_android.viewmodel.auth.AuthViewModelFactory
import tn.rifq_android.viewmodel.booking.BookingViewModelFactory
import tn.rifq_android.viewmodel.profile.ProfileViewModel
import tn.rifq_android.viewmodel.profile.ProfileViewModelFactory
import tn.rifq_android.viewmodel.profile.ProfileUiState
import tn.rifq_android.util.ProfileCompletionUtil

@Composable
fun MainScreen(
    themePreference: ThemePreference,
    notificationNavData: tn.rifq_android.NotificationNavData? = null,
    onNotificationHandled: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToChangeEmail: () -> Unit = {},
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Create AuthViewModel for verification screen
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )
    
    // Profile ViewModel for completion check
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val profileUiState by profileViewModel.uiState.collectAsState()
    
    // Profile completion state
    var showProfileCompletionAlert by remember { mutableStateOf(false) }
    var shouldPresentEditProfile by remember { mutableStateOf(false) }
    
    // Check profile completion
    LaunchedEffect(profileUiState) {
        if (profileUiState is ProfileUiState.Success) {
            val user = (profileUiState as ProfileUiState.Success).user
            if (ProfileCompletionUtil.requiresProfileCompletion(user)) {
                showProfileCompletionAlert = true
            }
        }
    }
    
    // Handle profile completion alert
    if (showProfileCompletionAlert && profileUiState is ProfileUiState.Success) {
        val user = (profileUiState as ProfileUiState.Success).user
        AlertDialog(
            onDismissRequest = { showProfileCompletionAlert = false },
            title = { Text("Complete your profile to start") },
            text = { Text(ProfileCompletionUtil.getMissingFieldsMessage(user)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showProfileCompletionAlert = false
                        // Navigate directly to edit profile screen (iOS Reference: MainTabView.swift line 77)
                        navController.navigate("edit_profile")
                    }
                ) {
                    Text("Complete Now")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProfileCompletionAlert = false }
                ) {
                    Text("Later")
                }
            }
        )
    }
    
    // Auto-navigate to edit profile if needed
    LaunchedEffect(shouldPresentEditProfile) {
        if (shouldPresentEditProfile) {
            kotlinx.coroutines.delay(100)
            navController.navigate("edit_profile")
            shouldPresentEditProfile = false
        }
    }
    
    // Initialize SubscriptionManager (iOS Reference: MainTabView.swift line 11)
    LaunchedEffect(Unit) {
        val tokenManager = tn.rifq_android.data.storage.TokenManager(context)
        val repository = tn.rifq_android.data.repository.SubscriptionRepository(
            tn.rifq_android.data.api.RetrofitInstance.subscriptionApi
        )
        tn.rifq_android.util.SubscriptionManager.initialize(repository, tokenManager)
        // Check subscription status on start (iOS Reference: MainTabView.swift line 127)
        tn.rifq_android.util.SubscriptionManager.checkSubscriptionStatus()
    }
    
    // Show expiration alert if needed (iOS Reference: SubscriptionManager.swift line 14)
    val showExpirationAlert by tn.rifq_android.util.SubscriptionManager.showExpirationAlert.collectAsState()
    val expirationMessage by tn.rifq_android.util.SubscriptionManager.expirationMessage.collectAsState()
    
    // Handle notification navigation
    LaunchedEffect(notificationNavData) {
        notificationNavData?.let { data ->
            when (data.destination) {
                "chat" -> {
                    // Navigate to conversations or specific conversation
                    if (data.conversationId != null) {
                        navController.navigate("conversation/${data.conversationId}") {
                            popUpTo("home")
                        }
                    } else {
                        navController.navigate("conversations") {
                            popUpTo("home")
                        }
                    }
                }
                "booking_detail" -> {
                    // Navigate to booking detail
                    data.bookingId?.let { bookingId ->
                        navController.navigate("booking_detail/$bookingId") {
                            popUpTo("home")
                        }
                    }
                }
                "notifications" -> {
                    navController.navigate("notifications") {
                        popUpTo("home")
                    }
                }
            }
            onNotificationHandled()
        }
    }
    
    // Expiration alert dialog (iOS Reference: SubscriptionManager.swift line 64)
    if (showExpirationAlert && expirationMessage != null) {
        AlertDialog(
            onDismissRequest = {
                tn.rifq_android.util.SubscriptionManager.dismissExpirationAlert()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        "Warning",
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subscription Alert")
                }
            },
            text = { Text(expirationMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        tn.rifq_android.util.SubscriptionManager.dismissExpirationAlert()
                        // Navigate to subscription management if needed
                        navController.navigate("subscription_management")
                    }
                ) {
                    Text("View Subscription")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tn.rifq_android.util.SubscriptionManager.dismissExpirationAlert()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Tab bar visibility - hide on detail screens (iOS Reference: MainTabView.swift line 36)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = remember(currentRoute) {
        currentRoute in listOf("home", "discover", "chat_ai", "mypets", "profile")
    }
    
    // Tab bar animation is implemented above
    // Tab content transitions are handled by NavHost (fade by default)
    // iOS Reference: MainTabView.swift lines 34-35, 51-52
    
    Scaffold(
        bottomBar = {
            // Tab bar show/hide animation (iOS Reference: MainTabView.swift lines 51-52)
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(200)
                ) + fadeOut(animationSpec = tween(200))
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home", // Match iOS default tab
            modifier = Modifier.padding(innerPadding)
        ) {
            // iOS tab: Home
            composable("home") {
                HomeScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // iOS tab: Discover
            composable("discover") {
                DiscoverScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // iOS tab: AI Chat
            composable("chat_ai") {
                ChatAIScreen(
                    navController = navController
                )
            }

            // iOS tab: My Pets
            composable("mypets") {
                tn.rifq_android.ui.screens.mypets.MyPetsScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // iOS tab: Profile
                    composable("profile") {
                        ProfileScreen(
                            navController = navController,
                            onNavigateToChangePassword = onNavigateToChangePassword,
                            onNavigateToChangeEmail = onNavigateToChangeEmail,
                            onNavigateToJoin = { navController.navigate("join_team") },
                            onNavigateToSubscription = { navController.navigate("subscription_management") },
                            onLogout = onLogout
                        )
                    }

            // Additional screens
            composable("join") {
                tn.rifq_android.ui.screens.join.JoinTeamScreen(
                    navController = navController
                )
            }
            
            composable("join_team") {
                tn.rifq_android.ui.screens.join.JoinTeamScreen(
                    navController = navController
                )
            }

            composable("join_vet") {
                tn.rifq_android.ui.screens.join.JoinPetSitterScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("join_sitter") {
                tn.rifq_android.ui.screens.join.JoinPetSitterScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("add_pet") {
                tn.rifq_android.ui.screens.pet.AddPetFlowScreen(navController = navController)
            }

            composable("calendar") {
                CalendarScreen(navController = navController)
            }
            
            // Add Calendar Event - Manual reminder creation (iOS Reference: AddCalendarEventView.swift)
            composable("add_calendar_event") {
                tn.rifq_android.ui.screens.calendar.AddCalendarEventScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }
            
            // Find Hub - Unified navigation to care services (iOS Reference: FindHubView.swift)
            composable("find_hub") {
                tn.rifq_android.ui.screens.findhub.FindHubScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("petsitter") {
                PetSitterScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            // Available Sitters Screen (iOS Reference: AvailableSittersView.swift)
            composable("available_sitters") {
                AvailableSittersScreen(
                    navController = navController
                )
            }

            composable("clinic") {
                FindVetScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("pet_detail/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                tn.rifq_android.ui.screens.petdetail.PetProfileScreen(navController = navController, petId = petId)
            }

            composable("medical_history/{petId}") { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId")
                MedicalHistoryScreen(navController = navController, petId = petId)
            }

            composable("medical") {
                MedicalScreen(navController = navController)
            }

            composable("edit_profile") {
                EditProfileScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable(
                route = "edit_pet/{petId}",
                arguments = listOf(navArgument("petId") { defaultValue = "" })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getString("petId").orEmpty()
                if (petId.isNotBlank()) {
                    EditPetScreen(
                        navController = navController,
                        petId = petId
                    )
                } else {
                    Text("Invalid pet ID")
                }
            }

            composable("help") {
                HelpScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("map") {
                MapScreen(
                    navController = navController,
                    themePreference = themePreference
                )
            }

            composable("conversations") {
                ConversationsListScreen(
                    navController = navController,
                    themePreference = themePreference,
                    currentUserId = "current_user_id" // TODO: Get from session
                )
            }
            
            // Direct conversation navigation (from notifications)
            composable("conversation/{conversationId}") { backStackEntry ->
                val context = LocalContext.current
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                val chatViewModel: tn.rifq_android.viewmodel.chat.ChatViewModel = viewModel()
                val conversations by chatViewModel.conversations.collectAsState()
                
                // Get current user ID from token
                val tokenManager = remember { tn.rifq_android.data.storage.TokenManager(context) }
                val currentUserId = remember {
                    var userId = ""
                    kotlinx.coroutines.runBlocking {
                        val token = tokenManager.getAccessToken().firstOrNull()
                        userId = if (token != null) {
                            tn.rifq_android.util.JwtDecoder.getUserIdFromToken(token) ?: ""
                        } else {
                            ""
                        }
                    }
                    userId
                }
                
                // Find conversation to get recipient details
                LaunchedEffect(conversationId) {
                    if (conversations.isEmpty()) {
                        chatViewModel.loadConversations()
                    }
                }
                
                val conversation = conversations.find { it.normalizedId == conversationId }
                val recipient = conversation?.participants?.firstOrNull { it.normalizedId != currentUserId }
                
                ChatViewScreen(
                    navController = navController,
                    themePreference = themePreference,
                    recipientId = recipient?.normalizedId ?: "",
                    recipientName = recipient?.name ?: "Chat",
                    currentUserId = currentUserId,
                    conversationId = conversationId
                )
            }

            composable("chat/{recipientId}/{recipientName}") { backStackEntry ->
                val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
                val recipientName = backStackEntry.arguments?.getString("recipientName") ?: "User"
                val context = LocalContext.current
                val tokenManager = remember { tn.rifq_android.data.storage.TokenManager(context) }
                val currentUserId = remember {
                    var userId = ""
                    kotlinx.coroutines.runBlocking {
                        val token = tokenManager.getAccessToken().firstOrNull()
                        userId = if (token != null) {
                            tn.rifq_android.util.JwtDecoder.getUserIdFromToken(token) ?: ""
                        } else {
                            ""
                        }
                    }
                    userId
                }
                
                // iOS: Just pass recipientId/Name, ChatViewScreen handles getOrCreateConversation
                ChatViewScreen(
                    navController = navController,
                    themePreference = themePreference,
                    recipientId = recipientId,
                    recipientName = recipientName,
                    currentUserId = currentUserId,
                    conversationId = null // Will be created automatically if needed
                )
            }

            composable("vet_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                tn.rifq_android.ui.screens.vet.VetProfileScreen(
                    navController = navController,
                    vetId = userId
                )
            }

            composable("sitter_profile/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                tn.rifq_android.ui.screens.petsitter.PetSitterProfileScreen(
                    navController = navController,
                    sitterId = userId
                )
            }
            
            // Booking screens (match iOS)
            composable("booking_list") {
                val bookingVm: tn.rifq_android.viewmodel.booking.BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                tn.rifq_android.ui.screens.booking.BookingListScreen(
                    viewModel = bookingVm,
                    onBookingSelected = { booking -> navController.navigate("booking_detail/${booking.id}") }
                )
            }

            composable("booking_create") {
                val bookingVm: tn.rifq_android.viewmodel.booking.BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                tn.rifq_android.ui.screens.booking.BookingCreateScreen(
                    viewModel = bookingVm,
                    onBookingCreated = { navController.navigate("booking_list") }
                )
            }
            
            composable("booking_create/{providerId}/{providerName}/{providerType}") { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                val providerName = backStackEntry.arguments?.getString("providerName") ?: ""
                val providerType = backStackEntry.arguments?.getString("providerType") ?: "vet"
                val bookingVm: tn.rifq_android.viewmodel.booking.BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                tn.rifq_android.ui.screens.booking.BookingCreateScreen(
                    viewModel = bookingVm,
                    providerId = providerId,
                    providerName = providerName,
                    providerType = providerType,
                    onBookingCreated = {
                        navController.navigate("booking_list") {
                            popUpTo("booking_list") { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("booking_detail/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingVm: tn.rifq_android.viewmodel.booking.BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                LaunchedEffect(bookingId) { bookingVm.fetchBookingById(bookingId) }
                tn.rifq_android.ui.screens.booking.BookingDetailScreen(
                    booking = bookingVm.selectedBooking.collectAsState().value,
                    onBack = { navController.popBackStack() },
                    viewModel = bookingVm
                )
            }

            composable("booking_update/{bookingId}") { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                val bookingVm: tn.rifq_android.viewmodel.booking.BookingViewModel = viewModel(factory = BookingViewModelFactory(context))
                tn.rifq_android.ui.screens.booking.BookingUpdateScreen(
                    viewModel = bookingVm,
                    bookingId = bookingId,
                    onBookingUpdated = { navController.navigate("booking_detail/$bookingId") }
                )
            }
            
            // Notifications screen (iOS Reference: NotificationsView.swift)
            composable("notifications") {
                tn.rifq_android.ui.screens.notification.NotificationsScreen(
                    navController = navController,
                )
            }
            
            // Subscription Management (iOS Reference: STRIPE_SUBSCRIPTION_IMPLEMENTATION.md)
            composable("subscription_management") {
                tn.rifq_android.ui.screens.subscription.SubscriptionManagementScreen(
                    navController = navController
                )
            }
            
            composable("join_vet_sitter") {
                tn.rifq_android.ui.screens.subscription.JoinWithSubscriptionScreen(
                    navController = navController
                )
            }
            
            composable("email_verification") {
                tn.rifq_android.ui.screens.subscription.EmailVerificationScreen(
                    navController = navController
                )
            }
            
            // Email verification (from conversion flow)
            composable(
                route = "verify?email={email}",
                arguments = listOf(navArgument("email") { 
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerifyEmailScreen(
                    viewModel = authViewModel,
                    email = email,
                    onVerified = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

