package tn.rifq_android.ui.screens.vet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import tn.rifq_android.ui.theme.*
import kotlin.collections.get


// Data classes
data class VetService(
    val label: String,
    val price: String
)

data class VetProfileData(
    val id: String,
    val name: String,
    val role: String,
    val emoji: String,
    val rating: Double,
    val reviews: Int,
    val is24_7: Boolean,
    val about: String,
    val services: List<VetService>,
    val hours: List<String>,
    val avatarBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetProfileScreen(
    navController: NavHostController,
    vetId: String? = null
) {
    // Mock data repository - In production, fetch from ViewModel/Repository based on vetId
    val vetsData = mapOf(
        "1" to VetProfileData(
            id = "1",
            name = "Dr. Ahmed Ben Ali",
            role = "Veterinary Specialist",
            emoji = "👨‍⚕️",
            rating = 4.8,
            reviews = 128,
            is24_7 = true,
            about = "Veterinarian with 10+ years of experience in surgery and dermatology. Specialized in treating complex cases.",
            services = listOf(
                VetService("Cabinet", "35€"),
                VetService("Home Visit", "60€"),
                VetService("Video Call", "25€")
            ),
            hours = listOf(
                "Mon–Sat: 9:00 AM – 6:00 PM",
                "Sun: 10:00 AM – 4:00 PM",
                "24/7 Emergency Available"
            ),
            avatarBg = Color(0xFFE8C4B4)
        ),
        "2" to VetProfileData(
            id = "2",
            name = "Dr. Soumaya El Aloui",
            role = "General & Emergency Veterinarian",
            emoji = "👩‍⚕️",
            rating = 4.9,
            reviews = 247,
            is24_7 = false,
            about = "Experienced veterinarian specializing in general care and emergency cases. Passionate about animal welfare.",
            services = listOf(
                VetService("Cabinet", "40€"),
                VetService("Home Visit", "65€"),
                VetService("Emergency", "80€")
            ),
            hours = listOf(
                "Mon–Fri: 8:00 AM – 7:00 PM",
                "Sat: 9:00 AM – 5:00 PM",
                "Sun: Closed"
            ),
            avatarBg = Color(0xFFD4A88A)
        ),
        "3" to VetProfileData(
            id = "3",
            name = "Dr. Karim Miled",
            role = "Orthopedics Specialist",
            emoji = "👨‍⚕️",
            rating = 4.6,
            reviews = 89,
            is24_7 = false,
            about = "Specialized in orthopedics and rehabilitation. Helping pets recover and live their best lives.",
            services = listOf(
                VetService("Cabinet", "45€"),
                VetService("Home Visit", "70€"),
                VetService("Rehabilitation", "55€")
            ),
            hours = listOf(
                "Mon–Fri: 10:00 AM – 6:00 PM",
                "Sat: 10:00 AM – 2:00 PM",
                "Sun: Closed"
            ),
            avatarBg = Color(0xFFA8D55F)
        )
    )

    // Get vet data or use default
    val vet = vetsData[vetId] ?: vetsData["1"]!!

    Scaffold(
        topBar = { VetProfileTopBar(navController, vet.name) },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 40.dp)
        ) {
            // Header Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(vet.avatarBg.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vet.emoji,
                            fontSize = 40.sp
                        )
                    }

                    // Name
                    Text(
                        text = vet.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Role
                    Text(
                        text = vet.role,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    // Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Rating Badge
                        BadgeChip(
                            icon = "⭐",
                            text = String.format("%.1f", vet.rating),
                            backgroundColor = CardBackground,
                            textColor = TextPrimary,
                            borderColor = GreyBorder
                        )

                        // Reviews Badge
                        BadgeChip(
                            icon = "📝",
                            text = "${vet.reviews} reviews",
                            backgroundColor = CardBackground,
                            textColor = TextPrimary,
                            borderColor = GreyBorder
                        )

                        // 24/7 Badge
                        if (vet.is24_7) {
                            BadgeChip(
                                icon = "⚡",
                                text = "24/7",
                                backgroundColor = OrangeAccent.copy(alpha = 0.18f),
                                textColor = OrangeAccent,
                                borderColor = OrangeAccent
                            )
                        }
                    }
                }
            }

            // About Section
            item {
                SectionCard(title = "About") {
                    Text(
                        text = vet.about,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }

            // Services & Pricing Section
            item {
                SectionCard(title = "Services & Pricing") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        vet.services.forEachIndexed { index, service ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = service.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = service.price,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OrangeAccent
                                )
                            }

                            if (index < vet.services.size - 1) {
                                Divider(color = GreyBorder)
                            }
                        }
                    }
                }
            }

            // Hours Section
            item {
                SectionCard(title = "Hours") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        vet.hours.forEach { hour ->
                            Text(
                                text = hour,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Book Appointment Button
            item {
                Button(
                    onClick = { /* Handle booking */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent
                    )
                ) {
                    Text(
                        text = "BOOK APPOINTMENT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VetProfileTopBar(navController: NavHostController, vetName: String) {
    TopAppBar(
        title = {
            Text(
                vetName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = { /* notifications */ }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                        .border(1.dp, GreyBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { /* settings */ }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBackground)
                        .border(1.dp, GreyBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBackground
        )
    )
}

@Composable
private fun BadgeChip(
    icon: String,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 11.sp
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GreyBorder, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VetProfileScreenPreview() {
    AppTheme {
        VetProfileScreen(
            navController = rememberNavController(),
            vetId = "1"
        )
    }
}