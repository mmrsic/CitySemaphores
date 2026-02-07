package com.citysemaphores.game

import com.citysemaphores.domain.graph.DijkstraRouter
import com.citysemaphores.domain.model.*

/**
 * Manages the spawning of vehicles in the city.
 *
 * The spawner selects random start and destination positions at the city border
 * and calculates the optimal route using Dijkstra's algorithm.
 *
 * @property city The city in which vehicles spawn
 * @property router The router for route calculation
 * @property spawnInterval Time interval between spawns in seconds
 * @property timeSinceLastSpawn Elapsed time since the last spawn
 * @property nextVehicleId Counter for the next vehicle ID
 */
data class VehicleSpawner(
    val city: City,
    val router: DijkstraRouter,
    val spawnInterval: Float = 3f,
    private val timeSinceLastSpawn: Float = 0f,
    private val nextVehicleId: Int = 1
) {

    /**
     * Updates the spawn timer
     *
     * @param deltaTime Elapsed time in seconds
     * @return Updated spawner
     */
    fun update(deltaTime: Float): VehicleSpawner =
        copy(timeSinceLastSpawn = timeSinceLastSpawn + deltaTime)

    /**
     * Attempts to spawn a new vehicle if the interval has elapsed.
     * On first call (timeSinceLastSpawn == 0) spawns immediately.
     *
     * @return New vehicle or null if not enough time has elapsed yet
     */
    fun trySpawn(): Vehicle? {
        // Allow spawn on first time or when interval is reached
        if (timeSinceLastSpawn > 0f && timeSinceLastSpawn < spawnInterval) {
            return null
        }

        return spawnVehicle()
    }

    /**
     * Spawns a new vehicle with random route
     *
     * @return New vehicle or null if no route was found
     */
    private fun spawnVehicle(): Vehicle? {
        val borderIntersections = city.getBorderIntersections()

        if (borderIntersections.size < 2) {
            return null // Not enough border intersections
        }

        // Select random start and destination position
        val start = borderIntersections.random()
        val destination = borderIntersections
            .filter { it != start }
            .randomOrNull() ?: return null

        // Calculate route
        val route = router.findShortestPath(start, destination) ?: return null

        // Create vehicle at start position
        val startPos = Position(
            start.position.x.toDouble(),
            start.position.y.toDouble()
        )

        val vehicle = Vehicle(
            id = "vehicle-$nextVehicleId",
            position = startPos,
            route = route,
            speed = 2f, // Standard speed
            state = VehicleState.Moving
        )

        return vehicle
    }

    /**
     * Resets the spawn timer after successful spawn
     */
    fun resetTimer(): VehicleSpawner =
        copy(
            timeSinceLastSpawn = 0f,
            nextVehicleId = nextVehicleId + 1
        )

    /**
     * Spawns a vehicle and resets the timer
     *
     * @return Pair of optional vehicle and updated spawner
     */
    fun spawnAndReset(): Pair<Vehicle?, VehicleSpawner> {
        val vehicle = trySpawn()
        val updatedSpawner = if (vehicle != null) resetTimer() else this
        return Pair(vehicle, updatedSpawner)
    }
}
