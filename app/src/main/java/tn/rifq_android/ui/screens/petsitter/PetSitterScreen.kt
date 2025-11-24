package tn.rifq_android.ui.screens.petsitter

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.rifq_android.data.model.sitter.SitterCard
import tn.rifq_android.data.model.sitter.ServiceType
import tn.rifq_android.ui.components.LabeledDateField
import tn.rifq_android.ui.components.RadioRow
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.sitter.SitterListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSitterScreen(
    navController: NavHostController,
    themePreference: tn.rifq_android.data.storage.ThemePreference,
    viewModel: SitterListViewModel = viewModel()
) {
    var fromDate by remember { mutableStateOf(Date()) }
    var toDate by remember { mutableStateOf(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) }
    var selectedService by remember { mutableStateOf(ServiceType.AT_HOME) }

    val sitters by viewModel.sitters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()


    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "contentFade"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopNavBar(
                title = "Pet Sitter",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        },
        containerColor = PageBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
                .graphicsLayer { this.alpha = alpha },
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {
                Text(
                    text = "Find Pet Sitter",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }


            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LabeledDateField(
                        title = "FROM DATE",
                        date = fromDate,
                        onDateChange = { fromDate = it }
                    )

                    LabeledDateField(
                        title = "TO DATE",
                        date = toDate,
                        onDateChange = { toDate = it }
                    )
                }
            }


            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SERVICE TYPE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RadioRow(
                            title = "At Home",
                            price = "€25/day",
                            isSelected = selectedService == ServiceType.AT_HOME,
                            onClick = { selectedService = ServiceType.AT_HOME }
                        )
                        RadioRow(
                            title = "Visit Only",
                            price = "€15",
                            isSelected = selectedService == ServiceType.VISIT_ONLY,
                            onClick = { selectedService = ServiceType.VISIT_ONLY }
                        )
                        RadioRow(
                            title = "Walking",
                            price = "€20",
                            isSelected = selectedService == ServiceType.WALKING,
                            onClick = { selectedService = ServiceType.WALKING }
                        )
                    }
                }
            }


            item {
                Button(
                    onClick = { /* TODO: Implement action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Action")
                }
            }
        }
    }
}
