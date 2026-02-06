package com.citysemaphores.game

import com.citysemaphores.domain.graph.DijkstraRouter
import com.citysemaphores.domain.model.*

/**
 * Die Game Engine orchestriert alle Spiel-Systeme und verarbeitet den Game Loop.
 *
 * Verantwortlichkeiten:
 * - Fahrzeug-Spawning über VehicleSpawner
 * - Fahrzeug-Bewegung und -Updates
 * - Erkennung von Ziel-Erreichen
 * - Integration zukünftiger Systeme (Kollision, Scoring, etc.)
 *
 * @property initialState Initialer Spielzustand
 */
class GameEngine(
    initialState: GameState
) {
    private var currentState: GameState = initialState
    private var vehicleSpawner: VehicleSpawner

    init {
        vehicleSpawner = VehicleSpawner(
            city = initialState.city,
            router = DijkstraRouter(initialState.city.graph),
            spawnInterval = 3f
        )
    }

    /**
     * Gibt den aktuellen Spielzustand zurück
     */
    fun getState(): GameState = currentState

    /**
     * Hauptupdate-Methode, wird pro Frame aufgerufen
     *
     * @param deltaTime Vergangene Zeit seit dem letzten Frame in Sekunden
     */
    fun update(deltaTime: Float) {
        if (!currentState.isPlaying) return

        // 1. Update game time
        currentState = currentState.updateTime(deltaTime)

        // 2. Update vehicle spawner
        vehicleSpawner = vehicleSpawner.update(deltaTime)

        // 3. Try to spawn new vehicle
        val (newVehicle, updatedSpawner) = vehicleSpawner.spawnAndReset()
        vehicleSpawner = updatedSpawner
        newVehicle?.let { vehicle ->
            currentState = currentState.addVehicle(vehicle)
        }

        // 4. Update all vehicles
        updateVehicles(deltaTime)

        // 5. Check for vehicles reaching destination
        checkVehicleArrivals()
    }

    /**
     * Aktualisiert alle Fahrzeuge (Bewegung)
     */
    private fun updateVehicles(deltaTime: Float) {
        val updatedVehicles = currentState.vehicles.map { vehicle ->
            vehicle.move(deltaTime)
        }
        currentState = currentState.copy(vehicles = updatedVehicles)
    }

    /**
     * Prüft, ob Fahrzeuge ihr Ziel erreicht haben
     */
    private fun checkVehicleArrivals() {
        val arrivedVehicleIds = mutableListOf<String>()

        currentState.vehicles.forEach { vehicle ->
            // Prüfe, ob Fahrzeug am Ende der Route ist
            if (vehicle.route.isAtDestination()) {
                // Prüfe, ob Fahrzeug die Zielposition erreicht hat
                val targetPos = vehicle.route.destination.position
                val distance = vehicle.position.distanceTo(
                    Position(targetPos.x.toDouble(), targetPos.y.toDouble())
                )

                // Wenn Fahrzeug nahe genug am Ziel ist (weniger als 0.5 Einheiten)
                if (distance < 0.5) {
                    arrivedVehicleIds.add(vehicle.id)
                }
            }
        }

        // Entferne angekommene Fahrzeuge und vergebe Punkte
        arrivedVehicleIds.forEach { vehicleId ->
            currentState = currentState.vehicleReachedDestination(vehicleId)
        }
    }

    /**
     * Schaltet eine Ampel an einer Kreuzung
     *
     * @param position Position der Kreuzung
     * @param direction Richtung der Ampel
     */
    fun toggleTrafficLight(position: GridPosition, direction: Direction) {
        val intersection = currentState.city.getIntersection(position) ?: return
        val updatedIntersection = intersection.toggleTrafficLight(direction)
        val updatedCity = currentState.city.updateIntersection(updatedIntersection)
        currentState = currentState.updateCity(updatedCity)
    }

    /**
     * Startet das Spiel
     */
    fun start() {
        currentState = currentState.start()
    }

    /**
     * Pausiert das Spiel
     */
    fun pause() {
        currentState = currentState.pause()
    }

    /**
     * Resettet das Spiel zum Anfangszustand
     */
    fun reset() {
        val newCity = City.create(currentState.city.width, currentState.city.height)
        currentState = GameState(
            city = newCity,
            isPlaying = false
        )
        vehicleSpawner = VehicleSpawner(
            city = newCity,
            router = DijkstraRouter(newCity.graph),
            spawnInterval = 3f
        )
    }

    companion object {
        /**
         * Erstellt eine neue Game Engine mit einer Stadt der angegebenen Größe
         *
         * @param width Breite der Stadt (10-20)
         * @param height Höhe der Stadt (10-20)
         * @return Neue Game Engine
         */
        fun create(width: Int = 15, height: Int = 15): GameEngine {
            val city = City.create(width, height)
            val initialState = GameState(city = city)
            return GameEngine(initialState)
        }
    }
}
