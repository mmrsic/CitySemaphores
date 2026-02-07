package com.citysemaphores.domain.model

import kotlin.math.max

/**
 * Represents a vehicle in the simulation.
 *
 * A vehicle follows a precalculated route and can be in different states.
 * The score is composed of:
 * - Base points: +1 per crossed intersection
 * - Bonus points: Length of the route (totalDistance) minus wait time
 *
 * @property id Unique ID of the vehicle
 * @property position Current position (continuous, not discrete)
 * @property route Route that the vehicle follows
 * @property speed Speed in units per second
 * @property state Current state of the vehicle
 * @property crossingsPassed Number of intersections already passed (base points)
 * @property waitTime Accumulated wait time in seconds
 * @property isInCollision Whether the vehicle was involved in a collision
 */
data class Vehicle(
    val id: String,
    val position: Position,
    val route: Route,
    val speed: Float,
    val state: VehicleState = VehicleState.Moving,
    val crossingsPassed: Int = 0,
    val waitTime: Float = 0f,
    val isInCollision: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Vehicle ID cannot be blank" }
        require(speed > 0f) { "Vehicle speed must be positive, got $speed" }
        require(crossingsPassed >= 0) { "crossingsPassed cannot be negative" }
        require(waitTime >= 0f) { "waitTime cannot be negative" }
    }

    /**
     * Moves the vehicle based on elapsed time.
     *
     * - **Moving**: Moves towards the next intersection
     * - **Waiting**: Position remains the same, waitTime is increased
     * - **Arrived/Crashed**: No movement
     *
     * @param deltaTime Elapsed time in seconds
     * @return Updated vehicle
     */
    fun move(deltaTime: Float): Vehicle {
        return when (state) {
            VehicleState.Moving -> {
                val target = route.next?.let { Position(it.position.x.toDouble(), it.position.y.toDouble()) }
                    ?: Position(route.current.position.x.toDouble(), route.current.position.y.toDouble())

                val direction = (target - position).normalized()
                val distance = speed * deltaTime
                val newPosition = position + (direction * distance.toDouble())

                copy(position = newPosition)
            }
            VehicleState.Waiting -> {
                // Position remains the same, but wait time increases
                copy(waitTime = waitTime + deltaTime)
            }
            VehicleState.Arrived, VehicleState.Crashed -> {
                // No movement
                this
            }
        }
    }

    /**
     * Increases the counter for passed intersections by 1.
     * Called when the vehicle leaves an intersection.
     */
    fun passCrossing(): Vehicle =
        copy(crossingsPassed = crossingsPassed + 1)

    /**
     * Calculates the total score for this vehicle.
     *
     * Formula: score = crossingsPassed + max(0, totalDistance - waitTime)
     *
     * - crossingsPassed: Base points (+1 per intersection)
     * - totalDistance: Bonus points for route length
     * - waitTime: Deduction for wait time (can reduce bonus to 0)
     *
     * The player receives at least the base points, even if the wait time
     * exceeds the distance.
     *
     * @return The total score
     */
    fun calculateScore(): Int {
        val baseScore = crossingsPassed
        val bonus = max(0, route.totalDistance - waitTime.toInt())
        return baseScore + bonus
    }

    /**
     * Sets the vehicle to waiting state and increases the wait time.
     * This method is called when a vehicle must wait at an intersection.
     *
     * @param deltaTime Elapsed time in seconds
     * @return Updated vehicle with increased wait time
     */
    fun waitAtIntersection(deltaTime: Float): Vehicle =
        copy(
            state = VehicleState.Waiting,
            waitTime = waitTime + deltaTime
        )

    /**
     * Sets the vehicle to waiting state
     */
    fun startWaiting(): Vehicle =
        copy(state = VehicleState.Waiting)

    /**
     * Sets the vehicle back to moving state
     */
    fun continueMoving(): Vehicle =
        if (state == VehicleState.Waiting) copy(state = VehicleState.Moving) else this

    /**
     * Marks the vehicle as arrived
     */
    fun markAsArrived(): Vehicle =
        copy(state = VehicleState.Arrived)

    /**
     * Marks the vehicle as crashed
     */
    fun markAsCrashed(): Vehicle =
        copy(
            state = VehicleState.Crashed,
            isInCollision = true
        )

    override fun toString(): String =
        "Vehicle($id, state=$state, at=${route.currentIndex}/${route.path.size-1}, crossings=$crossingsPassed, wait=${waitTime.toInt()}s)"
}
