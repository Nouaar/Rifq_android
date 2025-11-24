package tn.rifq_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import tn.rifq_android.ui.theme.CardBackground
import tn.rifq_android.ui.theme.HeaderBackground
import tn.rifq_android.ui.theme.TextPrimary
import tn.rifq_android.ui.theme.TextSecondary
import tn.rifq_android.ui.theme.VetStroke

@Composable
fun TopNavBar(
    title: String,
    navController: NavHostController? = null,
    showBackButton: Boolean = true,
    backIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    onBackClick: (() -> Unit)? = null,
    onMessagesClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    messageCount: Int = 0,
    notificationCount: Int = 0,
    actions: @Composable RowScope.() -> Unit = {},
    fontSize: TextUnit = 22.sp
) {
    Surface(
        color = HeaderBackground,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showBackButton) {
                    TopNavIconButton(
                        icon = backIcon,
                        contentDescription = "Back",
                        onClick = {
                            if (onBackClick != null) {
                                onBackClick()
                            } else {
                                navController?.popBackStack()
                            }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()

                    onMessagesClick?.let {
                        TopNavBadgeButton(
                            icon = Icons.Filled.Email,
                            contentDescription = "Messages",
                            badgeCount = messageCount,
                            onClick = it
                        )
                    }

                    onNotificationsClick?.let {
                        TopNavBadgeButton(
                            icon = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            badgeCount = notificationCount,
                            onClick = it
                        )
                    }

                    onSettingsClick?.let {
                        TopNavIconButton(
                            icon = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            onClick = it
                        )
                    }
                }
            }

            Divider(
                thickness = 1.dp,
                color = VetStroke.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun TopNavIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .border(1.dp, VetStroke.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary
        )
    }
}

@Composable
private fun TopNavBadgeButton(
    icon: ImageVector,
    contentDescription: String?,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        TopNavIconButton(icon = icon, contentDescription = contentDescription, onClick = onClick)

        if (badgeCount > 0) {
            Text(
                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Red, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
