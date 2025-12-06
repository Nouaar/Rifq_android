package tn.rifq_android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import tn.rifq_android.ui.theme.*


// Global state for drawer
object DrawerState {
    val isExpanded = mutableStateOf(false)
}

@Composable
fun TopNavBar(
    title: String,
    navController: NavHostController? = null,
    showBackButton: Boolean = true,
    showMenuButton: Boolean = false,
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
    val isMenuExpanded by DrawerState.isExpanded
    
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
                when {
                    showBackButton -> {
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
                    }
                    showMenuButton -> {
                        TopNavIconButton(
                            icon = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            onClick = { DrawerState.isExpanded.value = !DrawerState.isExpanded.value }
                        )
                    }
                    else -> {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
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

            HorizontalDivider(
                thickness = 1.dp,
                color = VetStroke.copy(alpha = 0.5f)
            )
        }
    }
}

// Sidebar Drawer Overlay Component
@Composable
fun NavigationDrawerOverlay(
    navController: NavHostController?,
    userName: String? = null,
    userImage: String? = null
) {
    val isMenuExpanded by DrawerState.isExpanded
    
    // Scrim (dark overlay) when drawer is open
    AnimatedVisibility(
        visible = isMenuExpanded,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    onClick = { DrawerState.isExpanded.value = false },
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
                .zIndex(999f)
        )
    }
    
    // Sidebar drawer
    AnimatedVisibility(
        visible = isMenuExpanded,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(),
        modifier = Modifier.zIndex(1000f)
    ) {
        NavigationDrawerMenu(
            navController = navController,
            onItemClick = { DrawerState.isExpanded.value = false },
            userName = userName,
            userImage = userImage
        )
    }
}

@Composable
fun NavigationDrawerMenu(
    navController: NavHostController?,
    onItemClick: () -> Unit,
    userName: String? = null,
    userImage: String? = null
) {
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    data class MenuItem(val label: String, val icon: ImageVector, val route: String)

    val menuItems = listOf(
        MenuItem("Home", Icons.Filled.Home, "home"),
        MenuItem("Discover", Icons.Filled.Search, "discover"),
        MenuItem("My Pets", Icons.Filled.Favorite, "myPets"),
        MenuItem("Conversations", Icons.Filled.Email, "conversations"),
        MenuItem("My Bookings", Icons.Filled.DateRange, "booking_list"),
        MenuItem("Calendar", Icons.Filled.Edit, "calendar"),
        MenuItem("Community", Icons.Filled.Menu, "community")
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
        color = CardBackground,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Menu",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    thickness = 1.dp,
                    color = VetStroke.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Menu items
            menuItems.forEach { item ->
                val isSelected = currentRoute == item.route

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate(item.route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            onItemClick()
                        }
                        .background(if (isSelected) HeaderBackground else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) TextPrimary else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // User Profile Section at Bottom
            if (userName != null) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = VetStroke.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController?.navigate("profile") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            onItemClick()
                        }
                        .background(if (currentRoute == "profile") HeaderBackground else Color.Transparent)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (userImage != null && userImage.isNotBlank()) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(userImage),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "View Profile",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { rotationZ = 180f }
                    )
                }
            }
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
        Icon(
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
