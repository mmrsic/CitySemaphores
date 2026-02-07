package com.citysemaphores.game

import com.citysemaphores.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for TrafficManager functionality.
 * 
 * Tests T065 and T066 verify traffic management behavior:
 * - T065: Safe distance maintenance between vehicles
 * - T066: Queue formation at blocked intersections
 */
class TrafficManagerTest {
    
    /**
     * T065: Unit test for TrafficManager.updateVehicleFollowing() safe distance maintenance.
     * 
     * Scenario:
     * - Vehicle 1 is ahead on the route
     * - Vehicle 2 is following behind
     * - When distance < SAFE_DISTANCE, vehicle 2 should start waiting
     * - When distance >= SAFE_DISTANCE, vehicle 2 should continue moving
     */
    @Test
    fun shouldMaintainSafeDistanceBetweenVehicles() {
        val manager = TrafficManager()
        
        // Create a shared route
        val route = createRoute(
            GridPosition(0, 0),
            GridPosition(5, 0)
        )
        
        // CASE 1: Vehicles too close (< SAFE_DISTANCE = 1.0)
        val vehicle1Close = Vehicle(
            id = "v1",
            position = Position(2.0, 0.5), // Ahead
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicle2Close = Vehicle(
            id = "v2",
            position = Position(1.2, 0.5), // Behind, distance = 0.8 < 1.0
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val updatedClose = manager.updateVehicleFollowing(listOf(vehicle1Close, vehicle2Close))
        
        // Vehicle 1 should continue moving (no one ahead)
        assertEquals(VehicleState.Moving, updatedClose[0].state,
            "Leading vehicle should continue moving")
        
        // Vehicle 2 should start waiting (too close)
        assertEquals(VehicleState.Waiting, updatedClose[1].state,
            "Following vehicle should wait when distance < SAFE_DISTANCE")
        
        // CASE 2: Vehicles at safe distance (>= SAFE_DISTANCE)
        val vehicle1Safe = Vehicle(
            id = "v1",
            position = Position(3.0, 0.5), // Ahead
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicle2Safe = Vehicle(
            id = "v2",
            position = Position(1.5, 0.5), // Behind, distance = 1.5 >= 1.0
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val updatedSafe = manager.updateVehicleFollowing(listOf(vehicle1Safe, vehicle2Safe))
        
        // Both vehicles should continue moving
        assertEquals(VehicleState.Moving, updatedSafe[0].state,
            "Leading vehicle should continue moving")
        assertEquals(VehicleState.Moving, updatedSafe[1].state,
            "Following vehicle should continue moving when distance >= SAFE_DISTANCE")
    }
    
    /**
     * T065: Tests that waiting vehicle resumes moving when safe distance is restored.
     */
    @Test
    fun shouldResumeMovingWhenSafeDistanceRestored() {
        val manager = TrafficManager()
        
        val route = createRoute(GridPosition(0, 0), GridPosition(5, 0))
        
        // Vehicle ahead
        val vehicle1 = Vehicle(
            id = "v1",
            position = Position(3.0, 0.5),
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )
        
        // Vehicle behind, currently waiting
        val vehicle2 = Vehicle(
            id = "v2",
            position = Position(1.5, 0.5), // Distance = 1.5 >= SAFE_DISTANCE
            route = route,
            speed = 2f,
            state = VehicleState.Waiting // Was waiting but distance is now safe
        )
        
        val updated = manager.updateVehicleFollowing(listOf(vehicle1, vehicle2))
        
        // Vehicle 2 should resume moving (safe distance achieved)
        assertEquals(VehicleState.Moving, updated[1].state,
            "Vehicle should resume moving when safe distance is restored")
    }
    
    /**
     * T065: Tests vehicle following with no vehicle ahead.
     */
    @Test
    fun shouldContinueMovingWhenNoVehicleAhead() {
        val manager = TrafficManager()
        
        val vehicle = Vehicle(
            id = "v1",
            position = Position(2.0, 0.5),
            route = createRoute(GridPosition(0, 0), GridPosition(5, 0)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val updated = manager.updateVehicleFollowing(listOf(vehicle))
        
        assertEquals(VehicleState.Moving, updated[0].state,
            "Single vehicle should continue moving")
    }
    
    /**
     * T066: Unit test for TrafficManager.formQueue() at blocked intersections.
     * 
     * Scenario:
     * - An intersection is blocked due to collision
     * - Vehicles approaching within QUEUE_DISTANCE_THRESHOLD should wait
     * - Vehicles beyond threshold should continue moving
     * - When intersection unblocks, queued vehicles should resume
     */
    @Test
    fun shouldFormQueueAtBlockedIntersection() {
        val manager = TrafficManager()
        
        // Create a blocked intersection at (2, 2)
        val blockedIntersection = Intersection(GridPosition(2, 2))
            .blockWithCollision(setOf("collision_v1", "collision_v2"))
        
        val unblockedIntersection = Intersection(GridPosition(3, 3))
        
        val intersections = listOf(blockedIntersection, unblockedIntersection)
        
        // Vehicle 1: Close to blocked intersection (within threshold)
        val vehicle1 = Vehicle(
            id = "v1",
            position = Position(1.8, 2.5), // Distance ≈ 0.7 < QUEUE_DISTANCE_THRESHOLD (1.5)
            route = createRoute(GridPosition(0, 2), GridPosition(4, 2)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        // Vehicle 2: Far from blocked intersection (beyond threshold)
        val vehicle2 = Vehicle(
            id = "v2",
            position = Position(0.5, 2.5), // Distance ≈ 2.0 > QUEUE_DISTANCE_THRESHOLD
            route = createRoute(GridPosition(0, 2), GridPosition(4, 2)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        // Vehicle 3: Approaching different (unblocked) intersection
        val vehicle3 = Vehicle(
            id = "v3",
            position = Position(2.5, 3.5),
            route = createRoute(GridPosition(2, 0), GridPosition(2, 5)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicles = listOf(vehicle1, vehicle2, vehicle3)
        val updated = manager.formQueue(vehicles, intersections)
        
        // Vehicle 1 should wait (within queue threshold)
        assertEquals(VehicleState.Waiting, updated[0].state,
            "Vehicle close to blocked intersection should wait")
        
        // Vehicle 2 should continue moving (beyond queue threshold)
        assertEquals(VehicleState.Moving, updated[1].state,
            "Vehicle far from blocked intersection should continue moving")
        
        // Vehicle 3 should continue moving (approaching unblocked intersection)
        assertEquals(VehicleState.Moving, updated[2].state,
            "Vehicle approaching unblocked intersection should continue moving")
    }
    
    /**
     * T066: Tests that queued vehicles resume when intersection unblocks.
     */
    @Test
    fun shouldResumeFromQueueWhenIntersectionUnblocks() {
        val manager = TrafficManager()
        
        // Create an unblocked intersection (was blocked, now clear)
        val unlockedIntersection = Intersection(GridPosition(2, 2))
        val intersections = listOf(unlockedIntersection)
        
        // Vehicle was waiting but intersection is now unblocked
        val vehicle = Vehicle(
            id = "v1",
            position = Position(1.8, 2.5),
            route = createRoute(GridPosition(0, 2), GridPosition(4, 2)),
            speed = 2f,
            state = VehicleState.Waiting // Was waiting for blocked intersection
        )
        
        val updated = manager.formQueue(listOf(vehicle), intersections)
        
        // Vehicle should resume moving
        assertEquals(VehicleState.Moving, updated[0].state,
            "Vehicle should resume moving when intersection unblocks")
    }
    
    /**
     * T066: Tests queue behavior with multiple vehicles at different distances.
     */
    @Test
    fun shouldFormQueueWithMultipleVehicles() {
        val manager = TrafficManager()
        
        val blockedIntersection = Intersection(GridPosition(3, 0))
            .blockWithCollision(setOf("c1"))
        
        val intersections = listOf(blockedIntersection)
        
        // Create 4 vehicles at increasing distances from blocked intersection (3, 0)
        val vehicles = listOf(
            createVehicleAtDistance("v1", 0.3, blockedIntersection.position), // Very close
            createVehicleAtDistance("v2", 0.8, blockedIntersection.position), // Close
            createVehicleAtDistance("v3", 1.4, blockedIntersection.position), // At threshold
            createVehicleAtDistance("v4", 2.0, blockedIntersection.position)  // Beyond threshold
        )
        
        val updated = manager.formQueue(vehicles, intersections)
        
        // First 3 vehicles should wait (within or at threshold)
        assertEquals(VehicleState.Waiting, updated[0].state, "v1 should wait")
        assertEquals(VehicleState.Waiting, updated[1].state, "v2 should wait")
        assertEquals(VehicleState.Waiting, updated[2].state, "v3 should wait")
        
        // Last vehicle should continue moving (beyond threshold)
        assertEquals(VehicleState.Moving, updated[3].state, "v4 should continue moving")
    }
    
    /**
     * T066: Tests that Arrived and Crashed vehicles are not affected by queue formation.
     */
    @Test
    fun shouldNotAffectArrivedOrCrashedVehicles() {
        val manager = TrafficManager()
        
        val blockedIntersection = Intersection(GridPosition(2, 0))
            .blockWithCollision(setOf("c1"))
        val intersections = listOf(blockedIntersection)
        
        val vehicles = listOf(
            Vehicle(
                id = "v1",
                position = Position(1.5, 0.5),
                route = createRoute(GridPosition(0, 0), GridPosition(5, 0)),
                speed = 2f,
                state = VehicleState.Arrived
            ),
            Vehicle(
                id = "v2",
                position = Position(1.5, 0.5),
                route = createRoute(GridPosition(0, 0), GridPosition(5, 0)),
                speed = 2f,
                state = VehicleState.Crashed
            )
        )
        
        val updated = manager.formQueue(vehicles, intersections)
        
        // States should remain unchanged
        assertEquals(VehicleState.Arrived, updated[0].state)
        assertEquals(VehicleState.Crashed, updated[1].state)
    }
    
    // HELPER FUNCTIONS
    
    private fun createRoute(start: GridPosition, end: GridPosition): Route {
        val intersections = mutableListOf<Intersection>()
        
        // Create a simple horizontal or vertical route
        if (start.y == end.y) {
            // Horizontal route
            val minX = minOf(start.x, end.x)
            val maxX = maxOf(start.x, end.x)
            for (x in minX..maxX) {
                intersections.add(Intersection(GridPosition(x, start.y)))
            }
        } else {
            // Vertical route
            val minY = minOf(start.y, end.y)
            val maxY = maxOf(start.y, end.y)
            for (y in minY..maxY) {
                intersections.add(Intersection(GridPosition(start.x, y)))
            }
        }
        
        return Route(intersections)
    }
    
    private fun createVehicleAtDistance(
        id: String,
        distance: Double,
        targetIntersection: GridPosition
    ): Vehicle {
        // Place vehicle at specified distance west of target intersection
        val targetCenter = Position(
            targetIntersection.x.toDouble() + 0.5,
            targetIntersection.y.toDouble() + 0.5
        )
        
        val vehiclePos = Position(
            targetCenter.x - distance,
            targetCenter.y
        )
        
        return Vehicle(
            id = id,
            position = vehiclePos,
            route = createRoute(
                GridPosition(0, targetIntersection.y),
                GridPosition(5, targetIntersection.y)
            ),
            speed = 2f,
            state = VehicleState.Moving
        )
    }
}
