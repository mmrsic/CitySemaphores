package com.citysemaphores.viewmodel

import com.citysemaphores.domain.model.Direction
import com.citysemaphores.domain.model.GameEvent
import com.citysemaphores.domain.model.GridPosition
import com.citysemaphores.domain.model.Position

/**
 * Immutable UI state for the game screen.
 * Follows MVI architecture pattern.
 */
data class GameUiState(
    val isGameRunning: Boolean = false,
    val isGameOver: Boolean = false,
    val currentTime: Double = 0.0,
    val score: Int = 0,

    // Grid configuration
    val gridWidth: Int = 5,
    val gridHeight: Int = 5,

    // Game entities
    val vehicles: List<VehicleUiState> = emptyList(),
    val intersections: List<IntersectionUiState> = emptyList(),
    val roads: List<RoadUiState> = emptyList(),

    // Statistics
    val statistics: GameStatistics = GameStatistics(),

    // Event log (last N events for UI display)
    val recentEvents: List<GameEvent> = emptyList(),

    // Game over screen data
    val gameOverData: GameOverData? = null
) {
    companion object {
        /**
         * Creates initial state for a new game.
         */
        fun initial(gridWidth: Int = 5, gridHeight: Int = 5): GameUiState {
            return GameUiState(
                gridWidth = gridWidth,
                gridHeight = gridHeight
            )
        }
    }
}

/**
 * UI representation of a vehicle.
 */
data class VehicleUiState(
    val id: String,
    val position: Position,
    val direction: Direction,
    val isWaiting: Boolean = false,
    val score: Int = 0,
    val route: List<GridPosition> = emptyList(),
    val currentSegmentIndex: Int = 0
)

/**
 * UI representation of an intersection with traffic lights.
 */
data class IntersectionUiState(
    val position: GridPosition,
    val trafficLights: Map<Direction, Boolean>, // Direction -> isGreen
    val isBlocked: Boolean = false,
    val blockingTimeRemaining: Double = 0.0,
    val vehiclesOnIntersection: Map<Direction, String?> = emptyMap() // Direction -> vehicleId
)

/**
 * UI representation of a road segment.
 */
data class RoadUiState(
    val from: GridPosition,
    val to: GridPosition,
    val direction: Direction
)

/**
 * Game statistics for display.
 */
data class GameStatistics(
    val vehiclesSpawned: Int = 0,
    val vehiclesCompleted: Int = 0,
    val totalCrossings: Int = 0,
    val collisions: Int = 0,
    val totalWaitTime: Double = 0.0,
    val averageWaitTime: Double = 0.0,
    val longestWaitTime: Double = 0.0
)

/**
 * Data shown on game over screen.
 */
data class GameOverData(
    val finalScore: Int,
    val duration: Double,
    val statistics: GameStatistics,
    val reason: String
)
