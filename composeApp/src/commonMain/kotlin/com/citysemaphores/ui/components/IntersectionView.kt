package com.citysemaphores.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.citysemaphores.domain.model.Direction
import com.citysemaphores.ui.theme.BlockedIntersection
import com.citysemaphores.ui.theme.IntersectionGray
import com.citysemaphores.ui.theme.TrafficLightGreen
import com.citysemaphores.ui.theme.TrafficLightRed
import com.citysemaphores.viewmodel.IntersectionUiState

/**
 * Renders a single intersection with its 4 traffic lights.
 *
 * @param state The intersection state
 * @param cellSize Size of the grid cell in dp
 * @param onTrafficLightClick Callback when a traffic light is clicked
 */
@Composable
fun IntersectionView(
    state: IntersectionUiState,
    cellSize: Float,
    onTrafficLightClick: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(cellSize.dp)
            .background(
                if (state.isBlocked) BlockedIntersection else IntersectionGray
            ),
        contentAlignment = Alignment.Center
    ) {
        // Render traffic lights for each direction
        TrafficLightIndicator(
            direction = Direction.NORTH,
            isGreen = state.trafficLights[Direction.NORTH] ?: false,
            isBlocked = state.isBlocked,
            onClick = { onTrafficLightClick(Direction.NORTH) },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
        )

        TrafficLightIndicator(
            direction = Direction.SOUTH,
            isGreen = state.trafficLights[Direction.SOUTH] ?: false,
            isBlocked = state.isBlocked,
            onClick = { onTrafficLightClick(Direction.SOUTH) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
        )

        TrafficLightIndicator(
            direction = Direction.EAST,
            isGreen = state.trafficLights[Direction.EAST] ?: false,
            isBlocked = state.isBlocked,
            onClick = { onTrafficLightClick(Direction.EAST) },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
        )

        TrafficLightIndicator(
            direction = Direction.WEST,
            isGreen = state.trafficLights[Direction.WEST] ?: false,
            isBlocked = state.isBlocked,
            onClick = { onTrafficLightClick(Direction.WEST) },
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
        )

        // Show blocking timer if blocked
        if (state.isBlocked && state.blockingTimeRemaining > 0) {
            val timeText = "${(state.blockingTimeRemaining * 10).toInt() / 10.0}"
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(2.dp)
            )
        }
    }
}

/**
 * Renders a single traffic light indicator with hover/touch feedback.
 */
@Composable
private fun TrafficLightIndicator(
    direction: Direction,
    isGreen: Boolean,
    isBlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val lightColor = when {
        isBlocked -> Color.DarkGray // All lights gray when blocked
        isGreen -> TrafficLightGreen
        else -> TrafficLightRed
    }

    Box(
        modifier = modifier
            .size(if (isHovered) 14.dp else 12.dp) // Grow on hover
            .shadow(
                elevation = if (isHovered) 4.dp else 2.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(lightColor)
            .hoverable(interactionSource = interactionSource)
            .clickable(
                enabled = !isBlocked,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Visual indicator for interactivity
        if (isHovered && !isBlocked) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }
    }
}
