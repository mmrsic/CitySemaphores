package com.citysemaphores.domain.model

/**
 * Represents the four cardinal directions in the city grid.
 * Used for vehicle movement, traffic light orientation, and collision detection.
 */
enum class Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    /**
     * Returns the opposite direction.
     * Used for determining incoming/outgoing directions at intersections.
     */
    fun opposite(): Direction = when (this) {
        NORTH -> SOUTH
        SOUTH -> NORTH
        EAST -> WEST
        WEST -> EAST
    }

    /**
     * Returns a unit vector representing this direction.
     * North: (0, -1), South: (0, 1), East: (1, 0), West: (-1, 0)
     */
    fun toVector(): Vector2D = when (this) {
        NORTH -> Vector2D(0.0, -1.0)
        SOUTH -> Vector2D(0.0, 1.0)
        EAST -> Vector2D(1.0, 0.0)
        WEST -> Vector2D(-1.0, 0.0)
    }
}
