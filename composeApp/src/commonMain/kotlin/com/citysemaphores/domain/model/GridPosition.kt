package com.citysemaphores.domain.model

/**
 * Represents a discrete position in the city grid.
 * Used for identifying intersections and road segments.
 *
 * @property x Column in the grid (0-based)
 * @property y Row in the grid (0-based)
 */
data class GridPosition(val x: Int, val y: Int) {

    /**
     * Returns the neighboring grid position in the specified direction.
     */
    fun neighbor(direction: Direction): GridPosition = when (direction) {
        Direction.NORTH -> GridPosition(x, y - 1)
        Direction.SOUTH -> GridPosition(x, y + 1)
        Direction.EAST -> GridPosition(x + 1, y)
        Direction.WEST -> GridPosition(x - 1, y)
    }

    /**
     * Returns all four neighboring grid positions.
     */
    fun neighbors(): List<GridPosition> = Direction.entries.map { neighbor(it) }

    /**
     * Calculates Manhattan distance to another grid position.
     */
    fun manhattanDistance(other: GridPosition): Int =
        kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)

    /**
     * Checks if this position is within the grid bounds.
     */
    fun isInBounds(gridWidth: Int, gridHeight: Int): Boolean =
        x in 0..<gridWidth && y in 0..<gridHeight

    /**
     * Converts grid position to center position in world coordinates.
     * Assumes each grid cell is 1.0 unit.
     */
    fun toWorldPosition(): Position = Position(x.toDouble() + 0.5, y.toDouble() + 0.5)

    override fun toString(): String = "($x, $y)"
}
