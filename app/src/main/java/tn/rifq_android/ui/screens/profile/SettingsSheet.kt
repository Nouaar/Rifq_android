package tn.rifq_android.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var locationEnabled by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider()

            SettingsRowWithSwitch(
                title = "Notifications",
                subtitle = "Receive push notifications",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            SettingsRowWithSwitch(
                title = "Dark Mode",
                subtitle = "Use dark theme",
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )

            SettingsRowWithSwitch(
                title = "Location Services",
                subtitle = "Allow location access",
                checked = locationEnabled,
                onCheckedChange = { locationEnabled = it }
            )

            HorizontalDivider()

            SettingsRow(
                title = "Change Password",
                onClick = {  }
            )

            SettingsRow(
                title = "Privacy Policy",
                onClick = {  }
            )

            SettingsRow(
                title = "Terms of Service",
                onClick = {  }
            )

            SettingsRow(
                title = "About",
                subtitle = "Version 1.0.0",
                onClick = {  }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Close")
            }
        }
    }
}
