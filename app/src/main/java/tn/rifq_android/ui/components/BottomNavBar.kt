package tn.rifq_android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import tn.rifq_android.ui.theme.*

/**
 * Bottom Navigation Bar matching iOS VetTabBar design
 * iOS Reference: VetTabBar.swift
 */
@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Map routes to tabs
    val selectedTab = when (currentRoute) {
        "home" -> VetTab.HOME
        "discover" -> VetTab.DISCOVER
        "chat_ai" -> VetTab.AI
        "mypets" -> VetTab.MY_PETS
        "profile" -> VetTab.PROFILE
        else -> VetTab.HOME
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Background with rounded rectangle, shadow, and border
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .border(
                    width = 1.dp,
                    color = VetStroke.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            color = CardBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardTabButton(
                        tab = VetTab.HOME,
                        isSelected = selectedTab == VetTab.HOME,
                        onClick = {
                            if (selectedTab != VetTab.HOME) {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StandardTabButton(
                        tab = VetTab.DISCOVER,
                        isSelected = selectedTab == VetTab.DISCOVER,
                        onClick = {
                            if (selectedTab != VetTab.DISCOVER) {
                                navController.navigate("discover") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                CenterAIButton(
                    isSelected = selectedTab == VetTab.AI,
                    onClick = {
                        if (selectedTab != VetTab.AI) {
                            navController.navigate("chat_ai") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StandardTabButton(
                        tab = VetTab.MY_PETS,
                        isSelected = selectedTab == VetTab.MY_PETS,
                        onClick = {
                            if (selectedTab != VetTab.MY_PETS) {
                                navController.navigate("mypets") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StandardTabButton(
                        tab = VetTab.PROFILE,
                        isSelected = selectedTab == VetTab.PROFILE,
                        onClick = {
                            if (selectedTab != VetTab.PROFILE) {
                                navController.navigate("profile") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

enum class VetTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    DISCOVER("Discover", Icons.Default.LocationOn),
    AI("AI", Icons.Default.Star), // Using Star as alternative to AutoAwesome
    MY_PETS("My Pets", Icons.Default.Favorite), // Using Favorite as alternative to Pets
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
private fun StandardTabButton(
    tab: VetTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circleSize by animateDpAsState(
        targetValue = if (isSelected) 34.dp else 30.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tabIconSize"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) VetCanyon.copy(alpha = 0.14f) else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) VetCanyon else CardBackground
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else VetStroke.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) Color.White else TextSecondary
                )
            }

            Text(
                text = tab.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) VetCanyon else TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CenterAIButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size by animateFloatAsState(
        targetValue = if (isSelected) 74f else 68f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "aiButtonSize"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "aiButtonScale"
    )

    Box(
        modifier = Modifier
            .offset(y = (-24).dp)
            .size(size.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = VetCanyon.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        VetCanyon,
                        VetCanyon.copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI Chat",
                modifier = Modifier.size(26.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "AI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = if (isSelected) 1.0f else 0.85f)
            )
        }
    }
}
