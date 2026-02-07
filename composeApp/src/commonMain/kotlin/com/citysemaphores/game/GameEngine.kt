package com.citysemaphores.game

import com.citysemaphores.domain.collision.CollisionDetector
import com.citysemaphores.domain.graph.DijkstraRouter
import com.citysemaphores.domain.model.*

/**
 * Die Game Engine orchestriert alle Spiel-Systeme und verarbeitet den Game Loop.
 *
 * Verantwortlichkeiten:
 * - Fahrzeug-Spawning über VehicleSpawner
 * - Fahrzeug-Bewegung und -Updates
 * - Erkennung von Ziel-Erreichen
 * - Kollisionserkennung und -behandlung
 * - Traffic Management (Warteschlangen, Fahrzeug-Folgen)
 * - Integration zukünftiger Systeme (Scoring, etc.)
 *
 * @property initialState Initialer Spielzustand
 */
class GameEngine(
    initialState: GameState
) {
    private var currentState: GameState = initialState
    private var vehicleSpawner: VehicleSpawner
    private val collisionDetector = CollisionDetector()
    private val trafficManager = TrafficManager()

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

        // 2. Update intersection blocking timers
        updateIntersectionBlockingTimers(deltaTime)

        // 3. Update vehicle spawner
        vehicleSpawner = vehicleSpawner.update(deltaTime)

        // 4. Try to spawn new vehicle
        val (newVehicle, updatedSpawner) = vehicleSpawner.spawnAndReset()
        vehicleSpawner = updatedSpawner
        newVehicle?.let { vehicle ->
            currentState = currentState.addVehicle(vehicle)
        }

        // 5. Update traffic management (queues and following)
        updateTrafficManagement()

        // 6. Update all vehicles (movement and wait time)
        updateVehicles(deltaTime)

        // 7. Detect and handle collisions
        detectAndHandleCollisions()

        // 8. Check for vehicles reaching destination
        checkVehicleArrivals()
    }

    /**
     * Aktualisiert alle Fahrzeuge (Bewegung)
     */
    private fun updateVehicles(deltaTime: Float) {
        val updatedVehicles = currentState.vehicles.map { vehicle ->
            // Accumulate wait time for vehicles in waiting state
            val vehicleWithWaitTime = if (vehicle.state == VehicleState.Waiting) {
                vehicle.waitAtIntersection(deltaTime)
            } else {
                vehicle
            }
            
            // Move the vehicle
            vehicleWithWaitTime.move(deltaTime)
        }
        currentState = currentState.copy(vehicles = updatedVehicles)
    }
    
    /**
     * Aktualisiert Traffic Management (Warteschlangen und Fahrzeug-Folgen)
     */
    private fun updateTrafficManagement() {
        val updatedVehicles = trafficManager.update(
            currentState.vehicles,
            currentState.city.intersections.values.toList()
        )
        currentState = currentState.copy(vehicles = updatedVehicles)
    }
    
    /**
     * Erkennt und behandelt Kollisionen an allen Kreuzungen
     */
    private fun detectAndHandleCollisions() {
        val collisions = collisionDetector.detectCollisions(
            currentState.city.intersections.values.toList(),
            currentState.vehicles
        )
        
        if (collisions.isEmpty()) return
        
        // Handle each collision
        var updatedCity = currentState.city
        var updatedVehicles = currentState.vehicles
        
        for ((intersectionPos, collidingVehicleIds) in collisions) {
            val intersection = updatedCity.getIntersection(intersectionPos) ?: continue
            
            // Handle collision and get updated intersection and vehicles
            val (blockedIntersection, vehiclesAfterCollision) = collisionDetector.handleCollision(
                intersection,
                collidingVehicleIds,
                updatedVehicles
            )
            
            // Update city with blocked intersection
            updatedCity = updatedCity.updateIntersection(blockedIntersection)
            updatedVehicles = vehiclesAfterCollision
        }
        
        currentState = currentState.copy(
            city = updatedCity,
            vehicles = updatedVehicles
        )
    }
    
    /**
     * Aktualisiert die Blocking-Timer aller Kreuzungen
     * Entfernt Fahrzeuge, die in Kollisionen verwickelt waren, wenn die Kreuzung entsperrt wird
     */
    private fun updateIntersectionBlockingTimers(deltaTime: Float) {
        var updatedCity = currentState.city
        val vehiclesToRemove = mutableSetOf<String>()
        
        // Update all intersections
        val updatedIntersections = currentState.city.intersections.values.map { intersection ->
            val updateResult: Pair<Intersection, Boolean> = intersection.updateBlockTimer(deltaTime.toDouble())
            val (updated, wasUnblocked) = updateResult
            
            // If intersection was just unblocked, mark collided vehicles for removal
            if (wasUnblocked && intersection.collidedVehicles.isNotEmpty()) {
                vehiclesToRemove.addAll(intersection.collidedVehicles)
            }
            
            updated
        }
        
        // Update city with new intersections
        updatedIntersections.forEach { intersection ->
            updatedCity = updatedCity.updateIntersection(intersection)
        }
        
        // Remove crashed vehicles after unblocking
        var updatedVehicles = currentState.vehicles
        if (vehiclesToRemove.isNotEmpty()) {
            updatedVehicles = updatedVehicles.filter { it.id !in vehiclesToRemove }
        }
        
        currentState = currentState.copy(
            city = updatedCity,
            vehicles = updatedVehicles
        )
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
