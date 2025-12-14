package tn.rifq_android.ui.screens.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.rifq_android.data.model.subscription.SubscriptionStatus
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.subscription.SubscriptionUiState
import tn.rifq_android.viewmodel.subscription.SubscriptionViewModel
import tn.rifq_android.viewmodel.subscription.SubscriptionViewModelFactory

/**
 * Manage Subscription Screen
 * 
 * Allows users to:
 * - Choose role (Veterinarian or Pet Sitter) if not already chosen
 * - Edit their professional profile
 * - Cancel subscription and downgrade to Owner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubscriptionScreen(
    navController: NavHostController,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModelFactory())
) {
    val subscription by viewModel.subscription.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    
    // Load subscription on screen open
    LaunchedEffect(Unit) {
        viewModel.getSubscription()
    }
    
    // Handle cancellation success
    LaunchedEffect(uiState) {
        if (uiState is SubscriptionUiState.Success) {
            val message = (uiState as SubscriptionUiState.Success).message
            if (message?.contains("canceled") == true || message?.contains("downgraded") == true) {
                // Navigate back to profile after cancellation
                navController.popBackStack()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopNavBar(
                title = "Manage Subscription",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                is SubscriptionUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                }
                is SubscriptionUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    subscription?.let { sub ->
                        // Subscription Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Premium",
                                    modifier = Modifier.size(48.dp),
                                    tint = OrangeAccent
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Premium Subscription",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (sub.status) {
                                        "active" -> "Active"
                                        "expires_soon" -> "Expires Soon"
                                        "canceled" -> "Canceled"
                                        "expired" -> "Expired"
                                        else -> "Unknown"
                                    },
                                    fontSize = 16.sp,
                                    color = when (sub.status) {
                                        "active" -> Color(0xFF4CAF50)
                                        "expires_soon" -> OrangeAccent
                                        else -> Color.Gray
                                    }
                                )
                            }
                        }
                        
                        // Role Selection or Profile Management
                        Text(
                            text = "Professional Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Text(
                            text = "Choose your professional role to get started or edit your existing profile.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        
                        // Veterinarian Option
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                navController.navigate("join_vet")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Veterinarian",
                                    modifier = Modifier.size(40.dp),
                                    tint = OrangeAccent
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Veterinarian",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Provide medical care for pets",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Go",
                                    tint = TextSecondary
                                )
                            }
                        }
                        
                        // Pet Sitter Option
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                navController.navigate("join_sitter")
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Pet Sitter",
                                    modifier = Modifier.size(40.dp),
                                    tint = OrangeAccent
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pet Sitter",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Care for pets at home or while owners are away",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Go",
                                    tint = TextSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Cancel Subscription Button
                        if (sub.status == "active" || sub.status == "expires_soon") {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Red
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cancel Subscription", fontSize = 16.sp)
                            }
                        }
                    } ?: run {
                        // No subscription found
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No active subscription found",
                                    fontSize = 16.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Cancel Subscription?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Are you sure you want to cancel your subscription?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚠️ This will:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Remove your verified badge")
                    Text("• Downgrade your account to Owner")
                    Text("• Remove your professional profile")
                    Text("• Remove you from discover listings")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelSubscription()
                        showCancelDialog = false
                    }
                ) {
                    Text("Yes, Cancel", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Subscription")
                }
            }
        )
    }
}
