package com.citysemaphores.integration

import com.citysemaphores.domain.collision.CollisionDetector
import com.citysemaphores.domain.collision.CollisionResult
import com.citysemaphores.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * T064: Integration test for collision → additive blocking → unblocking flow.
 * 
 * This test verifies the complete collision lifecycle:
 * 1. Collision detection identifies multiple vehicles at same intersection
 * 2. Intersection is blocked with additive blocking time based on vehicle count
 * 3. Blocking timer counts down over time
 * 4. Intersection unblocks when timer reaches zero
 * 5. Collided vehicles are cleared from the intersection
 * 
 * This is a critical integration test that combines:
 * - CollisionDetector.detectCollision()
 * - Intersection.blockWithCollision()
 * - Intersection.updateBlockTimer()
 */
class CollisionFlowTest {
    
    /**
     * Tests the complete collision flow with 2 vehicles.
     * 
     * Scenario:
     * 1. Two vehicles collide at intersection
     * 2. Intersection blocks for 22.5 seconds (2-vehicle additive time)
     * 3. Timer decrements over multiple frames
     * 4. Intersection unblocks when timer reaches 0
     */
    @Test
    fun shouldHandleFullCollisionFlowWith2Vehicles() {
        // SETUP: Create an intersection and 2 vehicles at collision point
        val intersection = Intersection(GridPosition(1, 1))
        
        val vehicle1 = Vehicle(
            id = "v1",
            position = Position(1.5, 1.5),
            route = createRoute(GridPosition(0, 1), GridPosition(2, 1)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicle2 = Vehicle(
            id = "v2",
            position = Position(1.5, 1.5),
            route = createRoute(GridPosition(1, 0), GridPosition(1, 2)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicles = listOf(vehicle1, vehicle2)
        
        // STEP 1: DETECT COLLISION
        val detector = CollisionDetector()
        val collisionResult = detector.detectCollision(intersection, vehicles)
        
        assertTrue(collisionResult is CollisionResult.Collision, 
            "Expected collision to be detected")
        
        val collision = collisionResult as CollisionResult.Collision
        assertEquals(2, collision.vehicles.size, "Collision should involve 2 vehicles")
        
        // STEP 2: BLOCK INTERSECTION WITH ADDITIVE TIME
        val vehicleIds = collision.vehicles.map { it.id }.toSet()
        var blockedIntersection = intersection.blockWithCollision(vehicleIds)
        
        assertTrue(blockedIntersection.isBlocked, "Intersection should be blocked")
        assertEquals(22.5, blockedIntersection.blockingTimeRemaining, 
            "2-vehicle collision should block for 22.5 seconds")
        assertEquals(vehicleIds, blockedIntersection.collidedVehicles,
            "Collided vehicles should be tracked")
        
        // Verify all lights are red
        Direction.entries.forEach { dir ->
            assertEquals(TrafficLightState.RED, blockedIntersection.trafficLights[dir]?.state,
                "All lights should be RED when blocked")
        }
        
        // STEP 3: TIMER COUNTDOWN - simulate multiple frames
        var wasUnblocked = false
        
        // Frame 1: -10.0 seconds
        val (afterFrame1, unblocked1) = blockedIntersection.updateBlockTimer(10.0)
        blockedIntersection = afterFrame1
        assertFalse(unblocked1, "Should not unblock after 10 seconds")
        assertEquals(12.5, blockedIntersection.blockingTimeRemaining, 0.01)
        assertTrue(blockedIntersection.isBlocked)
        
        // Frame 2: -10.0 seconds (total 20.0)
        val (afterFrame2, unblocked2) = blockedIntersection.updateBlockTimer(10.0)
        blockedIntersection = afterFrame2
        assertFalse(unblocked2, "Should not unblock after 20 seconds")
        assertEquals(2.5, blockedIntersection.blockingTimeRemaining, 0.01)
        assertTrue(blockedIntersection.isBlocked)
        
        // Frame 3: -5.0 seconds (total 25.0, exceeds 22.5)
        val (afterFrame3, unblocked3) = blockedIntersection.updateBlockTimer(5.0)
        blockedIntersection = afterFrame3
        wasUnblocked = unblocked3
        
        // STEP 4: VERIFY UNBLOCKING
        assertTrue(wasUnblocked, "Should signal unblocking when timer reaches 0")
        assertFalse(blockedIntersection.isBlocked, "Intersection should be unblocked")
        assertEquals(0.0, blockedIntersection.blockingTimeRemaining, 
            "Blocking time should be 0")
        assertTrue(blockedIntersection.collidedVehicles.isEmpty(), 
            "Collided vehicles should be cleared")
    }
    
    /**
     * Tests collision flow with 3 vehicles and longer blocking time.
     */
    @Test
    fun shouldHandleFullCollisionFlowWith3Vehicles() {
        // SETUP
        val intersection = Intersection(GridPosition(2, 2))
        
        val vehicles = listOf(
            createVehicle("v1", Position(2.5, 2.5)),
            createVehicle("v2", Position(2.5, 2.5)),
            createVehicle("v3", Position(2.5, 2.5))
        )
        
        // COLLISION DETECTION
        val detector = CollisionDetector()
        val collisionResult = detector.detectCollision(intersection, vehicles)
        
        assertTrue(collisionResult is CollisionResult.Collision)
        val collision = collisionResult as CollisionResult.Collision
        assertEquals(3, collision.vehicles.size)
        
        // BLOCKING
        val vehicleIds = collision.vehicles.map { it.id }.toSet()
        var blockedIntersection = intersection.blockWithCollision(vehicleIds)
        
        assertEquals(52.5, blockedIntersection.blockingTimeRemaining,
            "3-vehicle collision should block for 52.5 seconds")
        
        // COUNTDOWN - fast-forward to near end
        val (afterTime, wasUnblocked) = blockedIntersection.updateBlockTimer(53.0)
        
        // VERIFY UNBLOCKING
        assertTrue(wasUnblocked, "Should unblock after full time")
        assertFalse(afterTime.isBlocked)
        assertEquals(0.0, afterTime.blockingTimeRemaining)
        assertTrue(afterTime.collidedVehicles.isEmpty())
    }
    
    /**
     * Tests that no collision is detected when vehicles are at different positions.
     */
    @Test
    fun shouldNotBlockWhenNoCollision() {
        val intersection = Intersection(GridPosition(1, 1))
        
        // Vehicles at different positions
        val vehicles = listOf(
            createVehicle("v1", Position(1.5, 1.5)), // At intersection (1,1)
            createVehicle("v2", Position(2.5, 2.5))  // At intersection (2,2)
        )
        
        val detector = CollisionDetector()
        val result = detector.detectCollision(intersection, vehicles)
        
        assertTrue(result is CollisionResult.NoCollision,
            "Should not detect collision when vehicles are at different intersections")
        assertFalse(intersection.isBlocked, "Intersection should remain unblocked")
    }
    
    /**
     * Tests that waiting vehicles don't cause collisions.
     */
    @Test
    fun shouldNotDetectCollisionWithWaitingVehicles() {
        val intersection = Intersection(GridPosition(1, 1))
        
        // One moving, one waiting at same position
        val vehicles = listOf(
            createVehicle("v1", Position(1.5, 1.5), VehicleState.Moving),
            createVehicle("v2", Position(1.5, 1.5), VehicleState.Waiting)
        )
        
        val detector = CollisionDetector()
        val result = detector.detectCollision(intersection, vehicles)
        
        assertTrue(result is CollisionResult.NoCollision,
            "Waiting vehicles should not count toward collisions")
    }
    
    // HELPER FUNCTIONS
    
    private fun createRoute(start: GridPosition, end: GridPosition): Route {
        return Route(listOf(Intersection(start), Intersection(end)))
    }
    
    private fun createVehicle(
        id: String, 
        position: Position,
        state: VehicleState = VehicleState.Moving
    ): Vehicle {
        return Vehicle(
            id = id,
            position = position,
            route = createRoute(GridPosition(0, 0), GridPosition(5, 5)),
            speed = 2f,
            state = state
        )
    }
}
