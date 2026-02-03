package com.citysemaphores.domain.model

/**
 * Represents an intersection in the city grid with 4 independent traffic lights.
 * Tracks blocking state from collisions and directional vehicle occupancy.
 *
 * @property position Grid position of this intersection
 * @property trafficLights Map of traffic lights per direction
 * @property isBlocked Whether this intersection is blocked due to collision
 * @property blockingTimeRemaining Time remaining until intersection unblocks (seconds)
 * @property collidedVehicles Set of vehicle IDs involved in collision
 * @property directionalOccupancy Map tracking which vehicle (if any) occupies each direction
 */
data class Intersection(
    val position: GridPosition,
    val trafficLights: Map<Direction, TrafficLight> = Direction.entries.associateWith {
        TrafficLight(it, TrafficLightState.RED)
    },
    val isBlocked: Boolean = false,
    val blockingTimeRemaining: Double = 0.0,
    val collidedVehicles: Set<String> = emptySet(),
    val directionalOccupancy: Map<Direction, String?> = Direction.entries.associateWith { null }
) {
    /**
     * Checks if a vehicle from the specified direction can pass through this intersection.
     * A vehicle can pass if:
     * - The intersection is not blocked
     * - The traffic light for that direction is green
     */
    fun canVehiclePass(from: Direction): Boolean {
        if (isBlocked) return false
        val light = trafficLights[from] ?: return false
        return light.canPass()
    }

    /**
     * Toggles the traffic light for the specified direction.
     */
    fun toggleTrafficLight(direction: Direction): Intersection {
        val currentLight = trafficLights[direction] ?: return this
        val newLight = currentLight.toggle()
        return copy(trafficLights = trafficLights + (direction to newLight))
    }

    /**
     * Sets the traffic light for the specified direction to the given state.
     */
    fun setTrafficLight(direction: Direction, state: TrafficLightState): Intersection {
        val currentLight = trafficLights[direction] ?: return this
        val newLight = currentLight.setState(state)
        return copy(trafficLights = trafficLights + (direction to newLight))
    }

    /**
     * Sets all traffic lights to the specified state.
     */
    fun setAllLights(state: TrafficLightState): Intersection {
        val newLights = trafficLights.mapValues { (dir, _) ->
            TrafficLight(dir, state)
        }
        return copy(trafficLights = newLights)
    }

    /**
     * Checks if a vehicle from the specified direction can enter the intersection.
     * Based on directional occupancy: max 1 vehicle per direction on intersection.
     */
    fun canVehicleEnter(from: Direction, vehicleId: String): Boolean {
        if (isBlocked) return false
        val occupyingVehicle = directionalOccupancy[from]
        return occupyingVehicle == null || occupyingVehicle == vehicleId
    }

    /**
     * Records that a vehicle has entered the intersection from the specified direction.
     */
    fun enterIntersection(from: Direction, vehicleId: String): Intersection {
        return copy(
            directionalOccupancy = directionalOccupancy + (from to vehicleId)
        )
    }

    /**
     * Records that a vehicle has left the intersection.
     */
    fun leaveIntersection(from: Direction): Intersection {
        return copy(
            directionalOccupancy = directionalOccupancy + (from to null)
        )
    }

    /**
     * Blocks this intersection with the specified colliding vehicles.
     * Calculates blocking time using ADDITIVE formula:
     * - 1 vehicle: 7.5s
     * - 2 vehicles: 22.5s (7.5 + 15)
     * - 3 vehicles: 52.5s (7.5 + 15 + 30)
     * - 4 vehicles: 112.5s (7.5 + 15 + 30 + 60)
     */
    fun blockWithCollision(collidingVehicleIds: Set<String>): Intersection {
        val count = collidingVehicleIds.size.coerceAtMost(4)
        val blockingTime = when (count) {
            1 -> 7.5
            2 -> 22.5  // 7.5 + 15
            3 -> 52.5  // 7.5 + 15 + 30
            4 -> 112.5 // 7.5 + 15 + 30 + 60
            else -> 0.0
        }
        return copy(
            isBlocked = true,
            blockingTimeRemaining = blockingTime,
            collidedVehicles = collidingVehicleIds,
            trafficLights = setAllLights(TrafficLightState.RED).trafficLights
        )
    }

    /**
     * Updates the blocking timer by the given delta time.
     * Returns updated intersection and whether it was unblocked this frame.
     */
    fun updateBlockTimer(deltaTime: Double): Pair<Intersection, Boolean> {
        if (!isBlocked) return this to false

        val newTime = (blockingTimeRemaining - deltaTime).coerceAtLeast(0.0)
        val wasUnblocked = newTime == 0.0 && blockingTimeRemaining > 0.0

        return if (newTime == 0.0) {
            copy(
                isBlocked = false,
                blockingTimeRemaining = 0.0,
                collidedVehicles = emptySet()
            ) to wasUnblocked
        } else {
            copy(blockingTimeRemaining = newTime) to false
        }
    }
}
