package com.citysemaphores.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.citysemaphores.domain.model.Vehicle
import com.citysemaphores.domain.model.VehicleState

/**
 * Renders a single vehicle on the game field.
 *
 * The appearance varies by state:
 * - Moving: Blue vehicle
 * - Waiting: Yellow vehicle with pulsing border (queue indicator)
 * - Crashed: Red vehicle
 * - Arrived: Green vehicle (shortly before removal)
 *
 * @param vehicle The vehicle to render
 * @param gridSize Size of a grid cell in pixels
 * @param modifier Optional modifier
 */
@Composable
fun VehicleView(
    vehicle: Vehicle,
    gridSize: Float = 60f,
    modifier: Modifier = Modifier
) {
    val color = when (vehicle.state) {
        VehicleState.Moving -> Color(0xFF2196F3) // Blue
        VehicleState.Waiting -> Color(0xFFFFC107) // Yellow/Amber
        VehicleState.Crashed -> Color(0xFFF44336) // Red
        VehicleState.Arrived -> Color(0xFF4CAF50) // Green
    }
    
    // Pulsing animation for waiting vehicles (queue indicator)
    val infiniteTransition = rememberInfiniteTransition()
    val pulseBorderWidth by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size((gridSize * 0.4f).dp)
    ) {
        Box(
            modifier = Modifier
                .size((gridSize * 0.4f).dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    // Add pulsing border for waiting vehicles
                    if (vehicle.state == VehicleState.Waiting) {
                        Modifier.border(
                            width = pulseBorderWidth.dp,
                            color = Color.White.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        )
        
        // Show wait time for waiting vehicles (debugging/info)
        if (vehicle.state == VehicleState.Waiting && vehicle.waitTime > 1.0f) {
            Text(
                text = "${vehicle.waitTime.toInt()}",
                fontSize = (gridSize * 0.15f).sp,
                color = Color.White,
            )
        }
    }
}

/**
 * Calculates the screen position of a vehicle based on its world position.
 *
 * @param vehicle The vehicle
 * @param gridSize Size of a grid cell in pixels
 * @return Pair of (x, y) screen coordinates in dp
 */
fun vehicleScreenPosition(vehicle: Vehicle, gridSize: Float): Pair<Float, Float> {
    val x = vehicle.position.x.toFloat() * gridSize
    val y = vehicle.position.y.toFloat() * gridSize
    return Pair(x, y)
}
