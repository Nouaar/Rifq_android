package tn.rifq_android.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.ai.AIChatViewModel
import tn.rifq_android.viewmodel.ai.AIChatViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI Chat Assistant Screen
 * iOS Reference: ChatAIView.swift
 * Conversational AI for pet health advice
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    navController: NavHostController,
    themePreference: tn.rifq_android.data.storage.ThemePreference,
    viewModel: AIChatViewModel = viewModel(factory = AIChatViewModelFactory(LocalContext.current))
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Quick question suggestions
    val quickQuestions = listOf(
        "What vaccines does my dog need?",
        "Why is my cat vomiting?",
        "How often should I bathe my pet?",
        "What's the best food for puppies?",
        "Signs of pet dehydration?"
    )
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100) // Small delay for UI update
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "🐶 AI Assistant",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController,
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(OrangeAccent.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI Vet Assistant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Powered by Google Gemini",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        "Verified",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Questions (only at start)
                    if (messages.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Quick Questions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                quickQuestions.forEach { question ->
                                    SuggestionChip(
                                        onClick = {
                                            userInput = question
                                            coroutineScope.launch {
                                                viewModel.sendMessage(question)
                                                userInput = ""
                                            }
                                        },
                                        label = { Text(question, fontSize = 13.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = OrangeAccent.copy(alpha = 0.1f),
                                            labelColor = TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // Chat Messages
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isFromUser = message.isFromUser
                        )
                    }
                    
                    // Typing indicator
                    if (isLoading) {
                        item {
                            TypingIndicatorAI()
                        }
                    }
                    
                    // Error message
                    if (error != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Red.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        "Error",
                                        tint = Color.Red
                                    )
                                    Text(
                                        text = error ?: "Unknown error",
                                        fontSize = 13.sp,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Input Area
            MessageInputAI(
                text = userInput,
                onTextChange = { userInput = it },
                onSend = {
                    if (userInput.isNotBlank()) {
                        coroutineScope.launch {
                            viewModel.sendMessage(userInput.trim())
                            userInput = ""
                        }
                    }
                },
                enabled = !isLoading
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: AIChatMessage,
    isFromUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromUser) Spacer(modifier = Modifier.width(40.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isFromUser) VetCanyon else CardBackground,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Icon and role
                if (!isFromUser) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text("🤖", fontSize = 16.sp)
                        Text(
                            text = "AI Assistant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangeAccent
                        )
                    }
                }
                
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isFromUser) Color.White else TextPrimary,
                    lineHeight = 20.sp
                )
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = if (isFromUser) Color.White.copy(alpha = 0.7f) else TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        if (isFromUser) Spacer(modifier = Modifier.width(40.dp))
    }
}

@Composable
private fun MessageInputAI(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about pet health...", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VetInputBackground,
                    unfocusedContainerColor = VetInputBackground,
                    focusedBorderColor = OrangeAccent,
                    unfocusedBorderColor = VetStroke
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                enabled = enabled
            )
            
            IconButton(
                onClick = onSend,
                enabled = enabled && text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (enabled && text.isNotBlank()) OrangeAccent else OrangeAccent.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun TypingIndicatorAI() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Spacer(modifier = Modifier.width(40.dp))
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBackground,
            modifier = Modifier.widthIn(max = 100.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🤖", fontSize = 14.sp)
                repeat(3) { index ->
                    var alpha by remember { mutableStateOf(0.3f) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(index * 200L)
                            alpha = 1f
                            delay(600)
                            alpha = 0.3f
                            delay(200)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(OrangeAccent.copy(alpha = alpha), CircleShape)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> SimpleDateFormat("h:mm a", Locale.US).format(Date(timestamp))
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(timestamp))
    }
}

// AI Chat Message Data Class
data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)

