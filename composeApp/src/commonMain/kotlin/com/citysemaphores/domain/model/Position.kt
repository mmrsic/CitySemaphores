package com.citysemaphores.domain.model

import kotlin.math.sqrt

/**
 * Represents a continuous position in world coordinates.
 * Used for smooth vehicle movement and rendering.
 *
 * @property x X-coordinate in world space
 * @property y Y-coordinate in world space
 */
data class Position(val x: Double, val y: Double) {

    /**
     * Linearly interpolates between this position and another.
     *
     * @param target Target position
     * @param t Interpolation factor (0.0 = this position, 1.0 = target position)
     * @return Interpolated position
     */
    fun lerp(target: Position, t: Double): Position {
        val clampedT = t.coerceIn(0.0, 1.0)
        return Position(
            x = x + (target.x - x) * clampedT,
            y = y + (target.y - y) * clampedT
        )
    }

    /**
     * Calculates Euclidean distance to another position.
     */
    fun distanceTo(other: Position): Double {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Adds a vector to this position.
     */
    operator fun plus(vector: Vector2D): Position =
        Position(x + vector.x, y + vector.y)

    /**
     * Subtracts another position, returning the vector between them.
     */
    operator fun minus(other: Position): Vector2D =
        Vector2D(x - other.x, y - other.y)

    /**
     * Converts to the nearest grid position.
     */
    fun toGridPosition(): GridPosition =
        GridPosition(x.toInt(), y.toInt())

    override fun toString(): String = "Position(x=$x, y=$y)"

    companion object {
        val ZERO = Position(0.0, 0.0)
    }
}
