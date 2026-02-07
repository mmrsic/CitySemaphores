package com.citysemaphores.game

import com.citysemaphores.domain.model.*

/**
 * Manages traffic behavior including vehicle following and queue formation.
 * 
 * The TrafficManager is responsible for:
 * - Maintaining safe distances between vehicles on the same route
 * - Forming queues at blocked intersections
 * - Updating vehicle states (Moving/Waiting) based on traffic conditions
 */
class TrafficManager {
    
    companion object {
        /**
         * Minimum safe distance between vehicles on the same route (in grid units).
         * Vehicles must maintain at least this distance from the vehicle ahead.
         */
        const val SAFE_DISTANCE = 1.0
        
        /**
         * Distance threshold for queue formation at blocked intersections.
         * Vehicles within this distance of a blocked intersection will enter waiting state.
         */
        const val QUEUE_DISTANCE_THRESHOLD = 1.5
    }
    
    /**
     * Updates vehicle following behavior to maintain safe distances.
     * 
     * For each vehicle:
     * 1. Check if there's another vehicle ahead on the same route
     * 2. If distance to vehicle ahead < SAFE_DISTANCE, switch to Waiting
     * 3. If distance is safe and vehicle is waiting, resume Moving
     * 
     * Algorithm:
     * - Group vehicles by their current route intersection
     * - For each intersection, sort vehicles by progress on route
     * - Check distance between consecutive vehicles
     * - Update vehicle state if needed
     * 
     * @param vehicles List of active vehicles
     * @return Updated list of vehicles with adjusted states
     */
    fun updateVehicleFollowing(vehicles: List<Vehicle>): List<Vehicle> {
        if (vehicles.size < 2) return vehicles
        
        // Group vehicles by their current target intersection to find potential followers
        val vehiclesByRoute = vehicles.groupBy { it.route.current.position }
        
        return vehicles.map { vehicle ->
            if (vehicle.state != VehicleState.Moving && vehicle.state != VehicleState.Waiting) {
                // Don't process Arrived or Crashed vehicles
                return@map vehicle
            }
            
            // Find vehicles on similar route path (heading to same intersection)
            val nearbyVehicles = vehiclesByRoute[vehicle.route.current.position] ?: emptyList()
            
            // Find the closest vehicle ahead on the route
            val vehicleAhead = findVehicleAhead(vehicle, nearbyVehicles)
            
            if (vehicleAhead != null) {
                val distance = vehicle.position.distanceTo(vehicleAhead.position)
                
                if (distance < SAFE_DISTANCE) {
                    // Too close - start waiting
                    vehicle.startWaiting()
                } else if (vehicle.state == VehicleState.Waiting) {
                    // Safe distance restored - resume moving
                    vehicle.continueMoving()
                } else {
                    vehicle
                }
            } else if (vehicle.state == VehicleState.Waiting) {
                // No vehicle ahead - can resume moving
                vehicle.continueMoving()
            } else {
                vehicle
            }
        }
    }
    
    /**
     * Finds the vehicle directly ahead of the given vehicle on the same route.
     * 
     * A vehicle is "ahead" if it's further along the route path toward the destination.
     * We determine this by checking which vehicle is closer to the final destination.
     * 
     * @param vehicle The vehicle to check
     * @param candidates Potential vehicles ahead
     * @return The closest vehicle ahead, or null if none found
     */
    private fun findVehicleAhead(vehicle: Vehicle, candidates: List<Vehicle>): Vehicle? {
        // Use the route destination to determine who is ahead
        val destinationPos = Position(
            vehicle.route.destination.position.x.toDouble() + 0.5,
            vehicle.route.destination.position.y.toDouble() + 0.5
        )
        
        val ourDistanceToDestination = vehicle.position.distanceTo(destinationPos)
        
        return candidates
            .filter { it.id != vehicle.id }
            .filter { candidate ->
                // A vehicle is ahead if it's closer to the destination
                val candidateDistanceToDestination = candidate.position.distanceTo(destinationPos)
                candidateDistanceToDestination < ourDistanceToDestination
            }
            .minByOrNull { candidate ->
                // Return the closest vehicle ahead
                vehicle.position.distanceTo(candidate.position)
            }
    }
    
    /**
     * Forms queues at blocked intersections.
     * 
     * When an intersection is blocked due to collision:
     * 1. Find all vehicles approaching this intersection
     * 2. If vehicle is within QUEUE_DISTANCE_THRESHOLD, set to Waiting
     * 3. Vehicles beyond threshold continue Moving
     * 
     * This creates a natural queue formation where vehicles stop
     * and wait for the intersection to clear.
     * 
     * @param vehicles List of active vehicles
     * @param intersections List of intersections in the city
     * @return Updated list of vehicles with adjusted states
     */
    fun formQueue(vehicles: List<Vehicle>, intersections: List<Intersection>): List<Vehicle> {
        // Find all blocked intersections
        val blockedIntersections = intersections.filter { it.isBlocked }
        
        // If no blocked intersections, resume all waiting vehicles
        if (blockedIntersections.isEmpty()) {
            return vehicles.map { vehicle ->
                if (vehicle.state == VehicleState.Waiting) {
                    vehicle.continueMoving()
                } else {
                    vehicle
                }
            }
        }
        
        return vehicles.map { vehicle ->
            if (vehicle.state != VehicleState.Moving && vehicle.state != VehicleState.Waiting) {
                // Don't process Arrived or Crashed vehicles
                return@map vehicle
            }
            
            // Check if vehicle is approaching any blocked intersection on its route
            // Find the closest blocked intersection ahead on the route
            val blockedAhead = blockedIntersections
                .filter { blockedIntersection ->
                    // Check if this blocked intersection is on the vehicle's route
                    vehicle.route.path.any { it.position == blockedIntersection.position }
                }
                .filter { blockedIntersection ->
                    // Only consider intersections that are ahead (closer to destination)
                    val blockedPos = Position(
                        blockedIntersection.position.x.toDouble() + 0.5,
                        blockedIntersection.position.y.toDouble() + 0.5
                    )
                    val destinationPos = Position(
                        vehicle.route.destination.position.x.toDouble() + 0.5,
                        vehicle.route.destination.position.y.toDouble() + 0.5
                    )
                    val blockedDistToDestination = blockedPos.distanceTo(destinationPos)
                    val vehicleDistToDestination = vehicle.position.distanceTo(destinationPos)
                    blockedDistToDestination < vehicleDistToDestination
                }
                .minByOrNull { blockedIntersection ->
                    // Get the closest blocked intersection ahead
                    val blockedPos = Position(
                        blockedIntersection.position.x.toDouble() + 0.5,
                        blockedIntersection.position.y.toDouble() + 0.5
                    )
                    vehicle.position.distanceTo(blockedPos)
                }
            
            if (blockedAhead != null) {
                // Calculate distance to the blocked intersection
                val targetPos = Position(
                    blockedAhead.position.x.toDouble() + 0.5,
                    blockedAhead.position.y.toDouble() + 0.5
                )
                val distance = vehicle.position.distanceTo(targetPos)
                
                if (distance <= QUEUE_DISTANCE_THRESHOLD) {
                    // Within queue threshold - start waiting
                    vehicle.startWaiting()
                } else {
                    // Beyond threshold - can continue moving
                    if (vehicle.state == VehicleState.Waiting) {
                        vehicle.continueMoving()
                    } else {
                        vehicle
                    }
                }
            } else if (vehicle.state == VehicleState.Waiting) {
                // No blocked intersection ahead - resume moving
                vehicle.continueMoving()
            } else {
                vehicle
            }
        }
    }
    
    /**
     * Checks if a vehicle can enter an intersection.
     * 
     * A vehicle can enter if:
     * 1. The intersection is not blocked
     * 2. The directional slot for the vehicle's approach direction is available
     * 3. The traffic light from that direction is green
     * 
     * @param vehicle The vehicle attempting to enter
     * @param intersection The intersection to enter
     * @param approachDirection The direction from which the vehicle is approaching
     * @return true if the vehicle can enter
     */
    fun canVehicleEnterIntersection(
        vehicle: Vehicle,
        intersection: Intersection,
        approachDirection: Direction
    ): Boolean {
        // Check if intersection allows entry (not blocked, directional slot available)
        if (!intersection.canVehicleEnter(approachDirection, vehicle.id)) {
            return false
        }
        
        // Check if traffic light allows passage
        if (!intersection.canVehiclePass(approachDirection)) {
            return false
        }
        
        return true
    }
    
    /**
     * Updates all traffic management rules for the given vehicles.
     * 
     * This is a convenience method that applies both following behavior
     * and queue formation in the correct order.
     * 
     * Order matters:
     * 1. First form queues at blocked intersections (higher priority)
     * 2. Then apply vehicle following rules
     * 
     * @param vehicles List of active vehicles
     * @param intersections List of intersections in the city
     * @return Updated list of vehicles
     */
    fun update(vehicles: List<Vehicle>, intersections: List<Intersection>): List<Vehicle> {
        var updated = formQueue(vehicles, intersections)
        updated = updateVehicleFollowing(updated)
        return updated
    }
}
