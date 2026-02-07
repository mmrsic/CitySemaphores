package com.citysemaphores.domain.collision

import com.citysemaphores.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for CollisionDetector functionality.
 * 
 * Tests follow TDD approach - written FIRST before implementation.
 * These tests verify collision detection logic at intersections.
 */
class CollisionDetectorTest {

    /**
     * T060: Basic collision detection test
     * 
     * Given: An intersection at position (1,1) with 2 moving vehicles at the same position
     * When: CollisionDetector.detectCollision() is called
     * Then: Should return CollisionResult.Collision with both vehicles
     * 
     * This is the fundamental collision detection case - 2 vehicles occupy
     * the same intersection simultaneously while both are moving.
     */
    @Test
    fun shouldDetectCollisionWhenTwoVehiclesAtSameIntersection() {
        // Given: An intersection at (1, 1)
        val intersection = Intersection(GridPosition(1, 1))
        
        // Given: Two vehicles at the same intersection position (1.5, 1.5)
        // Both vehicles are in Moving state
        val vehicle1 = Vehicle(
            id = "v1",
            position = Position(1.5, 1.5), // Center of intersection (1,1)
            route = createSimpleRoute(GridPosition(0, 1), GridPosition(2, 1)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicle2 = Vehicle(
            id = "v2",
            position = Position(1.5, 1.5), // Same position as v1
            route = createSimpleRoute(GridPosition(1, 0), GridPosition(1, 2)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicles = listOf(vehicle1, vehicle2)
        
        // When: Collision detection is performed
        val detector = CollisionDetector()
        val result = detector.detectCollision(intersection, vehicles)
        
        // Then: Should detect a collision
        assertTrue(result is CollisionResult.Collision, "Expected collision to be detected")
        
        // Then: The collision should include both vehicles
        val collision = result as CollisionResult.Collision
        assertEquals(2, collision.vehicles.size, "Collision should involve exactly 2 vehicles")
        assertTrue(collision.vehicles.contains(vehicle1), "Collision should include vehicle1")
        assertTrue(collision.vehicles.contains(vehicle2), "Collision should include vehicle2")
    }
    
    /**
     * T061: No collision detection test
     * 
     * Given: An intersection at position (1,1) with vehicles at different positions
     * When: CollisionDetector.detectCollision() is called
     * Then: Should return CollisionResult.NoCollision
     * 
     * This verifies that the collision detector correctly identifies when
     * vehicles are NOT colliding (at different positions).
     */
    @Test
    fun shouldNotDetectCollisionWhenVehiclesAtDifferentPositions() {
        // Given: An intersection at (1, 1)
        val intersection = Intersection(GridPosition(1, 1))
        
        // Given: Two vehicles at different positions
        // Vehicle 1 at intersection (1,1) center
        val vehicle1 = Vehicle(
            id = "v1",
            position = Position(1.5, 1.5),
            route = createSimpleRoute(GridPosition(0, 1), GridPosition(2, 1)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        // Vehicle 2 at a different intersection (2,1)
        val vehicle2 = Vehicle(
            id = "v2",
            position = Position(2.5, 1.5),
            route = createSimpleRoute(GridPosition(1, 0), GridPosition(1, 2)),
            speed = 2f,
            state = VehicleState.Moving
        )
        
        val vehicles = listOf(vehicle1, vehicle2)
        
        // When: Collision detection is performed
        val detector = CollisionDetector()
        val result = detector.detectCollision(intersection, vehicles)
        
        // Then: Should NOT detect a collision
        assertTrue(result is CollisionResult.NoCollision, 
            "Expected no collision when vehicles are at different positions")
    }
    
    /**
     * Helper function to create a simple route for testing.
     * Creates a route from start to end intersection.
     */
    private fun createSimpleRoute(start: GridPosition, end: GridPosition): Route {
        val startIntersection = Intersection(start)
        val endIntersection = Intersection(end)
        return Route(listOf(startIntersection, endIntersection))
    }
}
