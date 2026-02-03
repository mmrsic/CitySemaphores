package com.citysemaphores.domain.model

/**
 * Represents game events that occur during simulation.
 * Used for event logging, statistics tracking, and UI feedback.
 */
sealed interface GameEvent {
    /**
     * Timestamp when the event occurred (simulation time in seconds).
     */
    val timestamp: Double

    /**
     * A vehicle spawned at a city border entry point.
     */
    data class VehicleSpawned(
        override val timestamp: Double,
        val vehicleId: String,
        val position: GridPosition,
        val direction: Direction,
        val route: List<GridPosition>
    ) : GameEvent

    /**
     * A vehicle successfully crossed an intersection.
     */
    data class IntersectionCrossed(
        override val timestamp: Double,
        val vehicleId: String,
        val intersection: GridPosition,
        val score: Int
    ) : GameEvent

    /**
     * A vehicle reached its destination and exited the city.
     */
    data class VehicleReachedGoal(
        override val timestamp: Double,
        val vehicleId: String,
        val totalScore: Int,
        val crossings: Int,
        val routeDistance: Int,
        val waitTime: Double
    ) : GameEvent

    /**
     * A collision occurred at an intersection.
     */
    data class CollisionOccurred(
        override val timestamp: Double,
        val intersection: GridPosition,
        val vehicleIds: List<String>,
        val blockingTime: Double
    ) : GameEvent

    /**
     * An intersection was blocked due to a collision.
     */
    data class IntersectionBlocked(
        override val timestamp: Double,
        val intersection: GridPosition,
        val blockingTime: Double
    ) : GameEvent

    /**
     * A blocked intersection was unblocked and collided vehicles were removed.
     */
    data class IntersectionUnblocked(
        override val timestamp: Double,
        val intersection: GridPosition,
        val removedVehicles: List<String>
    ) : GameEvent

    /**
     * A vehicle started waiting (stopped at red light or behind another vehicle).
     */
    data class VehicleStartedWaiting(
        override val timestamp: Double,
        val vehicleId: String,
        val position: GridPosition,
        val reason: WaitReason
    ) : GameEvent

    /**
     * A vehicle stopped waiting and resumed movement.
     */
    data class VehicleStoppedWaiting(
        override val timestamp: Double,
        val vehicleId: String,
        val waitDuration: Double
    ) : GameEvent

    /**
     * Traffic light state changed at an intersection.
     */
    data class TrafficLightChanged(
        override val timestamp: Double,
        val intersection: GridPosition,
        val direction: Direction,
        val isGreen: Boolean
    ) : GameEvent

    /**
     * Game ended due to gridlock (all entry points blocked for 5+ seconds).
     */
    data class GameOver(
        override val timestamp: Double,
        val totalScore: Int,
        val vehiclesSpawned: Int,
        val vehiclesCompleted: Int,
        val collisions: Int,
        val duration: Double
    ) : GameEvent
}

/**
 * Reason why a vehicle is waiting.
 */
enum class WaitReason {
    RED_LIGHT,           // Stopped at red traffic light
    VEHICLE_AHEAD,       // Waiting behind another vehicle
    BLOCKED_INTERSECTION // Intersection is blocked by collision
}
