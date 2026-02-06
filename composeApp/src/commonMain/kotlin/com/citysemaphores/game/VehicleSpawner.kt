package com.citysemaphores.game

import com.citysemaphores.domain.graph.DijkstraRouter
import com.citysemaphores.domain.model.*

/**
 * Verwaltet das Spawning von Fahrzeugen in der Stadt.
 *
 * Der Spawner wählt zufällige Start- und Zielpositionen am Stadtrand
 * und berechnet die optimale Route mittels Dijkstra-Algorithmus.
 *
 * @property city Die Stadt, in der Fahrzeuge spawnen
 * @property router Der Router für die Routenberechnung
 * @property spawnInterval Zeitintervall zwischen Spawns in Sekunden
 * @property timeSinceLastSpawn Vergangene Zeit seit dem letzten Spawn
 * @property nextVehicleId Zähler für die nächste Fahrzeug-ID
 */
data class VehicleSpawner(
    val city: City,
    val router: DijkstraRouter,
    val spawnInterval: Float = 3f,
    private val timeSinceLastSpawn: Float = 0f,
    private val nextVehicleId: Int = 1
) {

    /**
     * Aktualisiert den Spawn-Timer
     *
     * @param deltaTime Vergangene Zeit in Sekunden
     * @return Aktualisierter Spawner
     */
    fun update(deltaTime: Float): VehicleSpawner =
        copy(timeSinceLastSpawn = timeSinceLastSpawn + deltaTime)

    /**
     * Versucht ein neues Fahrzeug zu spawnen, wenn das Intervall verstrichen ist.
     * Beim ersten Aufruf (timeSinceLastSpawn == 0) wird sofort gespawnt.
     *
     * @return Neues Fahrzeug oder null, wenn noch nicht genug Zeit vergangen ist
     */
    fun trySpawn(): Vehicle? {
        // Erlaube Spawn beim ersten Mal oder wenn Intervall erreicht
        if (timeSinceLastSpawn > 0f && timeSinceLastSpawn < spawnInterval) {
            return null
        }

        return spawnVehicle()
    }

    /**
     * Spawnt ein neues Fahrzeug mit zufälliger Route
     *
     * @return Neues Fahrzeug oder null, wenn keine Route gefunden wurde
     */
    private fun spawnVehicle(): Vehicle? {
        val borderIntersections = city.getBorderIntersections()

        if (borderIntersections.size < 2) {
            return null // Nicht genug Rand-Kreuzungen
        }

        // Wähle zufällige Start- und Zielposition
        val start = borderIntersections.random()
        val destination = borderIntersections
            .filter { it != start }
            .randomOrNull() ?: return null

        // Berechne Route
        val route = router.findShortestPath(start, destination) ?: return null

        // Erstelle Fahrzeug an der Startposition
        val startPos = Position(
            start.position.x.toDouble(),
            start.position.y.toDouble()
        )

        val vehicle = Vehicle(
            id = "vehicle-$nextVehicleId",
            position = startPos,
            route = route,
            speed = 2f, // Standard-Geschwindigkeit
            state = VehicleState.Moving
        )

        return vehicle
    }

    /**
     * Resettet den Spawn-Timer nach erfolgreichem Spawn
     */
    fun resetTimer(): VehicleSpawner =
        copy(
            timeSinceLastSpawn = 0f,
            nextVehicleId = nextVehicleId + 1
        )

    /**
     * Spawnt ein Fahrzeug und resettet den Timer
     *
     * @return Pair aus optionalem Fahrzeug und aktualisiertem Spawner
     */
    fun spawnAndReset(): Pair<Vehicle?, VehicleSpawner> {
        val vehicle = trySpawn()
        val updatedSpawner = if (vehicle != null) resetTimer() else this
        return Pair(vehicle, updatedSpawner)
    }
}
