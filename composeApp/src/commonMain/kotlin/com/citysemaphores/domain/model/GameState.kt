package com.citysemaphores.domain.model

/**
 * Represents the complete game state.
 *
 * GameState is the central data structure that encapsulates the entire state
 * of the simulation at a specific point in time.
 *
 * @property city The city with all intersections
 * @property vehicles List of all active vehicles
 * @property totalScore Total score of the player
 * @property gameTime Elapsed game time in seconds
 * @property isPlaying Whether the game is actively running
 * @property vehiclesSpawned Number of vehicles spawned so far
 * @property vehiclesCompleted Number of vehicles that reached their destination
 * @property vehiclesCrashed Number of crashed vehicles
 */
data class GameState(
    val city: City,
    val vehicles: List<Vehicle> = emptyList(),
    val totalScore: Int = 0,
    val gameTime: Float = 0f,
    val isPlaying: Boolean = false,
    val vehiclesSpawned: Int = 0,
    val vehiclesCompleted: Int = 0,
    val vehiclesCrashed: Int = 0
) {

    /**
     * Adds a new vehicle to the game
     */
    fun addVehicle(vehicle: Vehicle): GameState =
        copy(
            vehicles = vehicles + vehicle,
            vehiclesSpawned = vehiclesSpawned + 1
        )

    /**
     * Removes a vehicle from the game
     *
     * @param vehicleId ID of the vehicle to remove
     * @param addScore Whether to add the vehicle's score to the total score
     * @return Updated game state
     */
    fun removeVehicle(vehicleId: String, addScore: Boolean = true): GameState {
        val vehicle = vehicles.find { it.id == vehicleId } ?: return this
        val scoreToAdd = if (addScore) vehicle.calculateScore() else 0

        return copy(
            vehicles = vehicles.filter { it.id != vehicleId },
            totalScore = totalScore + scoreToAdd
        )
    }

    /**
     * Marks a vehicle as having reached its destination and removes it
     */
    fun vehicleReachedDestination(vehicleId: String): GameState {
        val vehicle = vehicles.find { it.id == vehicleId } ?: return this
        val score = vehicle.calculateScore()

        return copy(
            vehicles = vehicles.filter { it.id != vehicleId },
            totalScore = totalScore + score,
            vehiclesCompleted = vehiclesCompleted + 1
        )
    }

    /**
     * Marks a vehicle as crashed
     */
    fun vehicleCrashed(vehicleId: String): GameState {
        val updatedVehicles = vehicles.map { vehicle ->
            if (vehicle.id == vehicleId) vehicle.markAsCrashed() else vehicle
        }

        return copy(
            vehicles = updatedVehicles,
            vehiclesCrashed = vehiclesCrashed + 1
        )
    }

    /**
     * Updates a vehicle in the game state
     */
    fun updateVehicle(vehicleId: String, transform: (Vehicle) -> Vehicle): GameState {
        val updatedVehicles = vehicles.map { vehicle ->
            if (vehicle.id == vehicleId) transform(vehicle) else vehicle
        }
        return copy(vehicles = updatedVehicles)
    }

    /**
     * Updates the city (e.g., after traffic light switching)
     */
    fun updateCity(newCity: City): GameState =
        copy(city = newCity)

    /**
     * Updates the game time
     */
    fun updateTime(deltaTime: Float): GameState =
        copy(gameTime = gameTime + deltaTime)

    /**
     * Starts the game
     */
    fun start(): GameState =
        copy(isPlaying = true)

    /**
     * Pauses the game
     */
    fun pause(): GameState =
        copy(isPlaying = false)

    /**
     * Returns the number of active vehicles
     */
    fun activeVehicleCount(): Int =
        vehicles.size

    override fun toString(): String =
        "GameState(vehicles=${vehicles.size}, score=$totalScore, time=${gameTime.toInt()}s)"
}
