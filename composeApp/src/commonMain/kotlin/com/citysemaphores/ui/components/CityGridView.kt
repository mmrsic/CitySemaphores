package com.citysemaphores.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.citysemaphores.domain.model.City

/**
 * Renders the city grid with all roads and intersections.
 *
 * Draws:
 * - Road lines between intersections (gray)
 * - Intersection points (light gray)
 *
 * @param city The city to render
 * @param gridSize Size of a grid cell in pixels
 * @param modifier Optional modifier
 */
@Composable
fun CityGridView(
    city: City,
    gridSize: Float = 60f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val roadColor = Color(0xFF757575) // Gray
        val intersectionColor = Color(0xFFBDBDBD) // Light gray
        val roadWidth = 4f
        val intersectionRadius = 8f

        // Draw horizontal roads
        for (y in 0 until city.height) {
            val startY = y * gridSize
            val endX = (city.width - 1) * gridSize

            drawLine(
                color = roadColor,
                start = Offset(0f, startY),
                end = Offset(endX, startY),
                strokeWidth = roadWidth
            )
        }

        // Draw vertical roads
        for (x in 0 until city.width) {
            val startX = x * gridSize
            val endY = (city.height - 1) * gridSize

            drawLine(
                color = roadColor,
                start = Offset(startX, 0f),
                end = Offset(startX, endY),
                strokeWidth = roadWidth
            )
        }

        // Draw intersection points
        for (y in 0 until city.height) {
            for (x in 0 until city.width) {
                val centerX = x * gridSize
                val centerY = y * gridSize

                drawCircle(
                    color = intersectionColor,
                    radius = intersectionRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}
