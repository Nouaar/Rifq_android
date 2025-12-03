package tn.rifq_android.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.subscription.SubscriptionUiState
import tn.rifq_android.viewmodel.subscription.SubscriptionViewModel
import tn.rifq_android.viewmodel.subscription.SubscriptionViewModelFactory

/**
 * Join Vet/Sitter with Subscription Screen
 * 
 * Flow:
 * 1. User selects role (vet or sitter)
 * 2. Reviews subscription details ($30/month)
 * 3. Creates subscription
 * 4. Backend sends verification email
 * 5. User verifies email (navigates to email verification screen)
 * 6. Subscription status changes to "active"
 * 7. User role upgraded, added to discover list/map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinWithSubscriptionScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModelFactory())
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val stripeHandler = remember { tn.rifq_android.util.StripePaymentHandler(context) }
    
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPaymentProcessing by remember { mutableStateOf(false) }
    var showPaymentScreen by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle payment required
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SubscriptionUiState.PaymentRequired -> {
                showPaymentProcessing = true
                stripeHandler.handlePayment(
                    clientSecret = state.clientSecret,
                    onSuccess = {
                        showPaymentProcessing = false
                        // Payment successful, navigate to form screen to fill details
                        if (selectedRole != null) {
                            val route = if (selectedRole == "vet") "join_vet" else "join_sitter"
                            navController.navigate(route) {
                                popUpTo("join_vet_sitter") { inclusive = true }
                            }
                        }
                        viewModel.resetUiState()
                    },
                    onError = { error ->
                        showPaymentProcessing = false
                        viewModel.setError(error)
                    }
                )
            }
            is SubscriptionUiState.Success -> {
                // Direct success (no payment required), navigate to form screen
                if (selectedRole != null) {
                    val route = if (selectedRole == "vet") "join_vet" else "join_sitter"
                    navController.navigate(route) {
                        popUpTo("join_vet_sitter") { inclusive = true }
                    }
                }
                viewModel.resetUiState()
            }
            else -> {}
        }
    }
    
    // Payment processing dialog
    if (showPaymentProcessing) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Processing Payment") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait while we process your payment...")
                }
            },
            confirmButton = { }
        )
    }
    
    // Confirm dialog
    if (showConfirmDialog && selectedRole != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Subscription") },
            text = {
                Column {
                    Text("You're about to subscribe as a ${selectedRole!!.replaceFirstChar { it.uppercase() }}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Price: $${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("After payment, you'll receive a verification code via email to activate your subscription.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.createSubscription(selectedRole!!)
                        showConfirmDialog = false
                    }
                ) {
                    Text("Subscribe", color = OrangeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Become a Professional",
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
                // Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            "Professional",
                            modifier = Modifier.size(48.dp),
                            tint = OrangeAccent
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Join as a Professional",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Get discovered by pet owners and start receiving booking requests",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Text(
                    text = "Choose Your Role",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Vet Option
                RoleCard(
                    title = "Veterinarian",
                    description = "Provide medical care and consultations for pets",
                    icon = Icons.Default.Star,
                    color = Color(0xFF2196F3),
                    isSelected = selectedRole == "vet",
                    onClick = { selectedRole = "vet" }
                )
                
                // Sitter Option
                RoleCard(
                    title = "Pet Sitter",
                    description = "Offer pet sitting, walking, and care services",
                    icon = Icons.Default.Favorite,
                    color = Color(0xFF4CAF50),
                    isSelected = selectedRole == "sitter",
                    onClick = { selectedRole = "sitter" }
                )
                
                // Subscription Benefits
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Subscription Benefits",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        
                        BenefitRow("✅", "Appear in discover list and map")
                        BenefitRow("✅", "Receive booking requests from pet owners")
                        BenefitRow("✅", "Manage your schedule and appointments")
                        BenefitRow("✅", "Build your professional profile")
                        BenefitRow("✅", "Connect with pet owners via chat")
                        
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Subscription",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "$${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeAccent
                            )
                        }
                    }
                }
                
                // Important Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            "Info",
                            tint = Color(0xFF2196F3)
                        )
                        Column {
                            Text(
                                text = "Email Verification Required",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "After subscribing, you'll receive a verification code via email to activate your subscription.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                // Error message
                if (uiState is SubscriptionUiState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = (uiState as SubscriptionUiState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Subscribe Button
                Button(
                    onClick = {
                        if (selectedRole != null) {
                            showPaymentScreen = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    enabled = selectedRole != null && uiState !is SubscriptionUiState.Loading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is SubscriptionUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, "Subscribe")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Subscribe Now",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
    }
    
    // Payment Screen as Popup
    if (showPaymentScreen) {
        PaymentScreen(
            amount = "$${SubscriptionViewModel.SUBSCRIPTION_PRICE}/month",
            onPaymentComplete = {
                showPaymentScreen = false
                // After payment screen, proceed with subscription creation
                if (selectedRole != null) {
                    viewModel.createSubscription(selectedRole!!)
                }
            },
            onDismiss = {
                showPaymentScreen = false
            }
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = color
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    "Selected",
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(bullet: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = bullet,
            fontSize = 14.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}

