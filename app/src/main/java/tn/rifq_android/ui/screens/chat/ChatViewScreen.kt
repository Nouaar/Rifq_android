package tn.rifq_android.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.rifq_android.data.model.chat.Message
import tn.rifq_android.ui.components.AudioMessageBubble
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.util.AudioRecorder
import tn.rifq_android.viewmodel.chat.ChatViewModel
import java.io.File

/**
 * ChatViewScreen matching iOS ChatView
 * Individual conversation screen with message list and input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatViewScreen(
    navController: NavHostController,
    themePreference: tn.rifq_android.data.storage.ThemePreference,
    recipientId: String,
    recipientName: String,
    currentUserId: String,
    conversationId: String? = null,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    // Audio recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    val audioRecorder = remember { AudioRecorder(context) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var hasAudioPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var messageText by remember { mutableStateOf("") }
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var actualConversationId by remember { mutableStateOf(conversationId) }
    
    val listState = rememberLazyListState()
    
    // Function definitions
    fun startRecording() {
        audioFile = audioRecorder.startRecording()
        if (audioFile != null) {
            isRecording = true
        }
    }
    
    fun stopRecording() {
        val file = audioRecorder.stopRecording()
        isRecording = false
        recordingTime = 0
        if (file != null && file.exists()) {
            // Upload audio message
            coroutineScope.launch {
                viewModel.uploadAudioMessage(recipientId, actualConversationId, file)
            }
        }
        audioFile = null
    }
    
    fun cancelRecording() {
        audioRecorder.cancelRecording()
        isRecording = false
        recordingTime = 0
        audioFile = null
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted && !isRecording) {
            startRecording()
        }
    }
    
    // Recording timer
    LaunchedEffect(isRecording) {
        recordingTime = 0
        while (isRecording) {
            delay(1000)
            recordingTime++
        }
    }
    
    // Log for debugging
    LaunchedEffect(error) {
        error?.let {
            android.util.Log.e("ChatViewScreen", "Error: $it")
        }
    }
    
    LaunchedEffect(messages.size) {
        android.util.Log.d("ChatViewScreen", "Messages count: ${messages.size}")
    }

    
    // iOS: onAppear - get or create conversation, then ALWAYS load messages
    LaunchedEffect(conversationId, recipientId) {
        if (conversationId != null) {
            // We have a conversation ID (from conversations list) - load messages directly
            actualConversationId = conversationId
            viewModel.loadMessages(conversationId)
        } else if (actualConversationId == null && recipientId.isNotEmpty()) {
            // Get or create conversation first (like iOS) - new conversation from Discover
            val conversation = viewModel.getOrCreateConversation(recipientId)
            if (conversation != null) {
                actualConversationId = conversation.normalizedId
                // iOS: ALWAYS load messages after getting conversation
                viewModel.loadMessages(conversation.normalizedId)
            }
        } else if (actualConversationId != null) {
            // Existing conversation - just load messages
            viewModel.loadMessages(actualConversationId!!)
        }
    }
    
    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = recipientName,
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
            // Messages List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading && messages.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VetCanyon
                        )
                    }
                    messages.isEmpty() -> {
                        Text(
                            text = "No messages yet. Start the conversation!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { message ->
                                // Check for audio message
                                if (message.audioURL != null && message.audioURL.isNotBlank()) {
                                    AudioMessageBubble(
                                        audioURL = message.audioURL,
                                        isFromCurrentUser = message.normalizedSenderId == currentUserId
                                    )
                                } else if (message.content.contains("🎤 Audio message")) {
                                    // Placeholder for audio message without URL
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = if (message.normalizedSenderId == currentUserId) 
                                            Arrangement.End else Arrangement.Start
                                    ) {
                                        Text(
                                            text = "🎤 Audio message",
                                            fontSize = 14.sp,
                                            color = if (message.normalizedSenderId == currentUserId) 
                                                Color.White else TextPrimary,
                                            modifier = Modifier
                                                .background(
                                                    color = if (message.normalizedSenderId == currentUserId) 
                                                        OrangeAccent else CardBackground,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                } else {
                                    MessageBubble(
                                        message = message,
                                        isFromCurrentUser = message.normalizedSenderId == currentUserId,
                                        onEdit = {
                                            editingMessageId = message.normalizedId
                                            messageText = message.content
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            coroutineScope.launch {
                                                viewModel.deleteMessage(message.normalizedId)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Message Input
            MessageInput(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank() && recipientId.isNotEmpty()) {
                        val textToSend = messageText.trim()
                        messageText = "" // Clear immediately for better UX (like iOS)
                        
                        coroutineScope.launch {
                            // Send message with recipientId and optional conversationId
                            // Backend creates conversation if it doesn't exist
                            val success = viewModel.sendMessage(recipientId, textToSend, actualConversationId)
                            if (!success) {
                                // Restore message if send failed (like iOS)
                                messageText = textToSend
                            } else if (actualConversationId == null) {
                                // Update conversation ID after first message
                                val conversations = viewModel.conversations.value
                                val newConv = conversations.firstOrNull { conv ->
                                    conv.participants?.any { it.normalizedId == recipientId } == true
                                }
                                if (newConv != null) {
                                    actualConversationId = newConv.normalizedId
                                }
                            }
                        }
                    }
                },
                enabled = recipientId.isNotEmpty(), // Always enable if we have recipient
                isRecording = isRecording,
                recordingTime = recordingTime,
                onStartRecording = {
                    if (hasAudioPermission) {
                        startRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecording = { stopRecording() },
                onCancelRecording = { cancelRecording() }
            )
        }
    }
    
    // Edit Message Dialog
    if (showEditDialog && editingMessageId != null) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingMessageId = null
                messageText = ""
            },
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Edit your message...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VetCanyon,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editingMessageId?.let { id ->
                            coroutineScope.launch {
                                viewModel.editMessage(id, messageText.trim())
                                showEditDialog = false
                                editingMessageId = null
                                messageText = ""
                            }
                        }
                    }
                ) {
                    Text("Save", color = VetCanyon)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingMessageId = null
                        messageText = ""
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Format timestamp
    val timestamp = try {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(message.createdAt)
        if (date != null) {
            val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            timeFormat.format(date)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromCurrentUser) {
            // Other person's messages on the left
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Message bubble
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackground,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .border(1.dp, VetStroke, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (message.isDeleted == true) {
                            Text(
                                text = "This message has been deleted",
                                fontSize = 14.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                text = message.content,
                                fontSize = 15.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                
                // Timestamp and edited indicator
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (timestamp.isNotEmpty()) {
                        Text(
                            text = timestamp,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (message.isEdited || message.updatedAt != null) {
                        Text(
                            text = "(edited)",
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(60.dp))
        } else {
            // My messages on the right
            Spacer(modifier = Modifier.width(60.dp))
            
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Message bubble
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = VetCanyon,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clickable { showMenu = true }
                ) {
                    Box {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            if (message.isDeleted == true) {
                                Text(
                                    text = "This message has been deleted",
                                    fontSize = 14.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    text = message.content,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        // Menu for current user's messages
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (message.isDeleted != true) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = false
                                        onEdit()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color.Red) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Timestamp and edited indicator
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (timestamp.isNotEmpty()) {
                        Text(
                            text = timestamp,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    if (message.isEdited || message.updatedAt != null) {
                        Text(
                            text = "(edited)",
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    isRecording: Boolean = false,
    recordingTime: Int = 0,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onCancelRecording: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        if (isRecording) {
            // Recording View (like iOS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Waveform animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    repeat(30) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "wave")
                        val height by infiniteTransition.animateFloat(
                            initialValue = 8f,
                            targetValue = 30f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = LinearEasing, delayMillis = index * 50),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "waveHeight"
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Red)
                        )
                    }
                }
                
                // Recording time
                Text(
                    text = formatRecordingTime(recordingTime),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                // Cancel button
                IconButton(
                    onClick = onCancelRecording,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CardBackground, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Stop/Send button
                IconButton(
                    onClick = onStopRecording,
                    modifier = Modifier
                        .size(36.dp)
                        .background(OrangeAccent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Text Input View (like iOS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Microphone button
                IconButton(
                    onClick = { if (text.isEmpty()) onStartRecording() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (text.isEmpty()) "Record" else "Add",
                        tint = OrangeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VetInputBackground,
                        unfocusedContainerColor = VetInputBackground,
                        focusedBorderColor = VetCanyon,
                        unfocusedBorderColor = VetStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                    enabled = enabled
                )
                
                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (enabled && text.isNotBlank()) VetCanyon else VetCanyon.copy(alpha = 0.4f),
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
}

private fun formatRecordingTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}
