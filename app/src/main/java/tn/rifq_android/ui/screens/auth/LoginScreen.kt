package tn.rifq_android.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.AuthUiState
import tn.rifq_android.viewmodel.AuthViewModel
import tn.rifq_android.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val message = (uiState as AuthUiState.Success).message
            if (message.contains("Login", ignoreCase = true) ||
                message.contains("successful", ignoreCase = true)) {
                onNavigateToHome()
                viewModel.resetState()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBackground)
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(LoginBackground)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "Rifq",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )

            Text(
                text = "Pet Healthcare Platform",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            Button(
                onClick = {
                    viewModel.login(email.text.trim(), password.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDA866C)),
                shape = RoundedCornerShape(50.dp),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "SIGN IN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            when (val state = uiState) {
                is AuthUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                else -> {}
            }

            // Links - Sign Up | Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onNavigateToRegister) {
                    Text("Sign Up", color = OrangePrimary)
                }
                Text(text = "|", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                TextButton(onClick = { /* TODO: Navigate to Forgot Password */ }) {
                    Text("Forgot Password?", color = OrangePrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            HorizontalDivider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Google Sign In Button
            OutlinedButton(
                onClick = { /* TODO: Google Sign-In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = uiState !is AuthUiState.Loading).copy(
                    brush = SolidColor(OrangePrimary)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangePrimary
                ),
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text(
                    text = "SIGN IN WITH GOOGLE",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}