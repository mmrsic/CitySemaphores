package com.citysemaphores.domain.model

import kotlin.math.sqrt

/**
 * Represents a 2D vector for physics calculations and movement.
 * Used for velocity, direction, and displacement calculations.
 *
 * @property x X-component
 * @property y Y-component
 */
data class Vector2D(val x: Double, val y: Double) {

    /**
     * Calculates the magnitude (length) of this vector.
     */
    fun magnitude(): Double = sqrt(x * x + y * y)

    /**
     * Returns a normalized version of this vector (unit vector).
     * Returns zero vector if magnitude is zero.
     */
    fun normalized(): Vector2D {
        val mag = magnitude()
        return if (mag > 0.0) {
            Vector2D(x / mag, y / mag)
        } else {
            ZERO
        }
    }

    /**
     * Scales this vector by a scalar value.
     */
    operator fun times(scalar: Double): Vector2D =
        Vector2D(x * scalar, y * scalar)

    /**
     * Divides this vector by a scalar value.
     */
    operator fun div(scalar: Double): Vector2D =
        Vector2D(x / scalar, y / scalar)

    /**
     * Adds another vector to this vector.
     */
    operator fun plus(other: Vector2D): Vector2D =
        Vector2D(x + other.x, y + other.y)

    /**
     * Subtracts another vector from this vector.
     */
    operator fun minus(other: Vector2D): Vector2D =
        Vector2D(x - other.x, y - other.y)

    /**
     * Negates this vector.
     */
    operator fun unaryMinus(): Vector2D =
        Vector2D(-x, -y)

    /**
     * Calculates dot product with another vector.
     */
    fun dot(other: Vector2D): Double =
        x * other.x + y * other.y

    /**
     * Rotates this vector by 90 degrees counter-clockwise.
     */
    fun perpendicular(): Vector2D =
        Vector2D(-y, x)

    override fun toString(): String = "Vector2D(x=$x, y=$y)"

    companion object {
        val ZERO = Vector2D(0.0, 0.0)
        val UP = Vector2D(0.0, -1.0)
        val DOWN = Vector2D(0.0, 1.0)
        val LEFT = Vector2D(-1.0, 0.0)
        val RIGHT = Vector2D(1.0, 0.0)
    }
}
