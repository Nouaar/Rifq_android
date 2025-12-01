package tn.rifq_android.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
// Pull to refresh not available in this Compose version - using manual refresh button instead
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import tn.rifq_android.data.model.map.MapLocation
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.map.MapViewModel

/**
 * Embedded Map View for Discover tab
 * iOS Reference: DiscoverView.swift lines 270-344
 */
@Composable
fun EmbeddedMapView(
    navController: NavHostController,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val vetLocations by viewModel.vetLocations.collectAsState()
    val sitterLocations by viewModel.sitterLocations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedLocation by remember { mutableStateOf<MapLocation?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    
    val allLocations = remember(vetLocations, sitterLocations) {
        vetLocations.map { MapLocation.Vet(it) } + sitterLocations.map { MapLocation.Sitter(it) }
    }
    
    // Load locations when map mode is shown
    LaunchedEffect(Unit) {
        viewModel.loadLocations()
    }
    
    // Refresh locations when subscription becomes active
    val subscriptionActivated by tn.rifq_android.util.SubscriptionManager.subscriptionActivated.collectAsState()
    LaunchedEffect(subscriptionActivated) {
        if (subscriptionActivated) {
            viewModel.refreshLocations()
        }
    }
    
    // Update markers whenever locations change
    LaunchedEffect(allLocations) {
        mapView?.let { mv ->
            mv.mapboxMap.getStyle { style ->
                addMarkers(mv, allLocations) { location ->
                    selectedLocation = location
                }
            }
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { mv ->
                    mapView = mv
                    mv.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(10.1815, 36.8065))
                            .zoom(11.0)
                            .build()
                    )
                    mv.mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                        // Add markers once style is loaded
                        addMarkers(mv, allLocations) { location ->
                            selectedLocation = location
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = VetCanyon,
                    strokeWidth = 3.dp
                )
            }
        }
        
        // Legend (iOS Reference: DiscoverView.swift lines 329-344)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LegendItem(
                color = VetCanyon,
                label = "Available vet"
            )
            LegendItem(
                color = Color.Blue,
                label = "Available sitter"
            )
            LegendItem(
                color = Color.Gray,
                label = "Offline"
            )
        }
        
        // Bottom sheet for location details
        AnimatedVisibility(
            visible = selectedLocation != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            selectedLocation?.let { location ->
                LocationBottomSheet(
                    location = location,
                    onDismiss = { selectedLocation = null },
                    onViewProfile = {
                        when (location) {
                            is MapLocation.Vet -> {
                                navController.navigate("vet_profile/${location.userId}")
                            }
                            is MapLocation.Sitter -> {
                                navController.navigate("sitter_profile/${location.userId}")
                            }
                        }
                        selectedLocation = null
                    }
                )
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}

private fun addMarkers(
    mapView: MapView,
    locations: List<MapLocation>,
    onMarkerClick: (MapLocation) -> Unit
) {
    android.util.Log.d("EmbeddedMapView", "Adding ${locations.size} markers to map")
    
    val annotationApi = mapView.annotations
    val pointAnnotationManager = annotationApi.createPointAnnotationManager()
    pointAnnotationManager.deleteAll()
    
    val markerSize = 60
    
    locations.forEach { location ->
        val markerColor = when {
            !location.isAvailable -> android.graphics.Color.GRAY
            location is MapLocation.Vet -> VetCanyon.toArgb()
            else -> android.graphics.Color.BLUE
        }
        
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val paint = Paint().apply {
            color = markerColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)
        
        val iconPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        val iconText = if (location is MapLocation.Vet) "+" else "🐾"
        canvas.drawText(
            iconText,
            markerSize / 2f,
            markerSize / 2f + 8f,
            iconPaint
        )
        
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(location.coordinate)
            .withIconSize(1.0)
            .withIconImage(bitmap)
        
        val annotation = pointAnnotationManager.create(pointAnnotationOptions)
        
        annotation.setData(com.google.gson.JsonObject().apply {
            addProperty("locationId", location.id)
        })
    }
    
    pointAnnotationManager.addClickListener { annotation ->
        val locationId = annotation.getData()?.asJsonObject?.get("locationId")?.asString
        val location = locations.firstOrNull { it.id == locationId }
        location?.let { onMarkerClick(it) }
        true
    }
    
    android.util.Log.d("EmbeddedMapView", "Successfully added ${locations.size} markers")
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun LocationBottomSheet(
    location: MapLocation,
    onDismiss: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(42.dp)
                    .height(4.dp)
                    .background(VetStroke.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Header with availability badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                AvailabilityBadge(isAvailable = location.isAvailable)
            }
            
            // Address
            Text(
                text = location.address,
                fontSize = 14.sp,
                color = TextSecondary
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = VetStroke.copy(alpha = 0.3f)
            )
            
            // Details
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (location) {
                    is MapLocation.Vet -> {
                        DetailRow(
                            icon = Icons.Default.Favorite,
                            text = "General consultation · Vaccinations"
                        )
                        DetailRow(
                            icon = Icons.Default.Info,
                            text = "Emergency availability 9am – 6pm"
                        )
                        DetailRow(
                            icon = Icons.Default.Phone,
                            text = "Call +216 55 123 456"
                        )
                    }
                    is MapLocation.Sitter -> {
                        DetailRow(
                            icon = Icons.Default.Person,
                            text = "Pet Sitter Services"
                        )
                        DetailRow(
                            icon = Icons.Default.Info,
                            text = "Available for bookings"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // View Profile button
            Button(
                onClick = onViewProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (location is MapLocation.Vet) VetCanyon else Color.Blue
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "View Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AvailabilityBadge(isAvailable: Boolean) {
    Text(
        text = if (isAvailable) "Available" else "Offline",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isAvailable) VetCanyon else Color.Gray,
        modifier = Modifier
            .background(
                color = if (isAvailable) VetCanyon.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextPrimary
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextPrimary
        )
    }
}

