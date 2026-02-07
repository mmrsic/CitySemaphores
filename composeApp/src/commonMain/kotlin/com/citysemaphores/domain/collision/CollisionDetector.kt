package com.citysemaphores.domain.collision

import com.citysemaphores.domain.model.*
import kotlin.math.abs

/**
 * Detects collisions between vehicles at intersections.
 * 
 * A collision occurs when multiple vehicles occupy the same intersection
 * simultaneously while in Moving state. The collision detection uses
 * grid-based positioning to determine if vehicles are at the same intersection.
 */
class CollisionDetector {
    
    /**
     * Detects if there is a collision at the given intersection.
     * 
     * Algorithm:
     * 1. Filter vehicles that are at this intersection (within grid boundaries)
     * 2. Only consider vehicles in Moving state
     * 3. If 2 or more vehicles meet these criteria, it's a collision
     * 
     * @param intersection The intersection to check
     * @param vehicles All active vehicles in the simulation
     * @return CollisionResult indicating whether a collision occurred
     */
    fun detectCollision(intersection: Intersection, vehicles: List<Vehicle>): CollisionResult {
        // Filter vehicles that are at this intersection and moving
        val vehiclesAtIntersection = vehicles.filter { vehicle ->
            vehicle.state == VehicleState.Moving &&
            isVehicleAtIntersection(vehicle, intersection)
        }
        
        // Collision occurs when 2 or more moving vehicles are at the same intersection
        return if (vehiclesAtIntersection.size >= 2) {
            CollisionResult.Collision(vehiclesAtIntersection)
        } else {
            CollisionResult.NoCollision
        }
    }
    
    /**
     * Checks if a vehicle is currently at the given intersection.
     * 
     * A vehicle is considered "at" an intersection if its position falls within
     * the grid cell boundaries of that intersection. Grid cells are centered
     * on integer coordinates with 1.0 unit width/height.
     * 
     * For example, intersection at GridPosition(1, 1) covers the area:
     * - X: [1.0, 2.0)
     * - Y: [1.0, 2.0)
     * 
     * @param vehicle The vehicle to check
     * @param intersection The intersection to check against
     * @return true if the vehicle is at the intersection
     */
    private fun isVehicleAtIntersection(vehicle: Vehicle, intersection: Intersection): Boolean {
        val gridPos = vehicle.position.toGridPosition()
        return gridPos == intersection.position
    }
    
    /**
     * Detects collisions at all intersections in the city.
     * 
     * Checks each intersection for multiple moving vehicles occupying the same space.
     * Returns a map of intersection positions to the sets of colliding vehicle IDs.
     * 
     * @param intersections All intersections in the city
     * @param vehicles All active vehicles
     * @return Map of intersection positions to sets of colliding vehicle IDs (empty if no collisions)
     */
    fun detectCollisions(intersections: List<Intersection>, vehicles: List<Vehicle>): Map<GridPosition, Set<String>> {
        val collisions = mutableMapOf<GridPosition, Set<String>>()
        
        for (intersection in intersections) {
            when (val result = detectCollision(intersection, vehicles)) {
                is CollisionResult.Collision -> {
                    collisions[intersection.position] = result.vehicles.map { it.id }.toSet()
                }
                is CollisionResult.NoCollision -> {
                    // No collision at this intersection
                }
            }
        }
        
        return collisions
    }
    
    /**
     * Handles a collision by updating the intersection and marking vehicles as crashed.
     * 
     * Algorithm:
     * 1. Block the intersection with the colliding vehicle IDs (applies additive blocking time)
     * 2. Mark all vehicles involved as crashed (isInCollision = true)
     * 3. Return updated intersection and vehicles
     * 
     * @param intersection The intersection where collision occurred
     * @param collidingVehicleIds Set of vehicle IDs involved in the collision
     * @param vehicles All active vehicles
     * @return Pair of updated intersection and vehicles list
     */
    fun handleCollision(
        intersection: Intersection,
        collidingVehicleIds: Set<String>,
        vehicles: List<Vehicle>
    ): Pair<Intersection, List<Vehicle>> {
        // Block the intersection with collision
        val blockedIntersection = intersection.blockWithCollision(collidingVehicleIds)
        
        // Mark vehicles as crashed
        val updatedVehicles = vehicles.map { vehicle ->
            if (vehicle.id in collidingVehicleIds) {
                vehicle.copy(
                    isInCollision = true,
                    state = VehicleState.Crashed
                )
            } else {
                vehicle
            }
        }
        
        return blockedIntersection to updatedVehicles
    }
}

/**
 * Result of collision detection.
 */
sealed interface CollisionResult {
    /**
     * No collision detected at this intersection.
     */
    data object NoCollision : CollisionResult
    
    /**
     * Collision detected with the specified vehicles.
     * 
     * @property vehicles List of vehicles involved in the collision (2 or more)
     */
    data class Collision(val vehicles: List<Vehicle>) : CollisionResult
}
