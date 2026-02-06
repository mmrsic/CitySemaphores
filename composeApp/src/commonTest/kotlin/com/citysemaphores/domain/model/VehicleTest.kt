package com.citysemaphores.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Vehicle functionality
 */
class VehicleTest {

    @Test
    fun shouldCreateVehicleWithInitialState() {
        // Given
        val route = createSimpleRoute()
        val startPosition = Position.ZERO

        // When
        val vehicle = Vehicle(
            id = "v1",
            position = startPosition,
            route = route,
            speed = 2f
        )

        // Then
        assertEquals("v1", vehicle.id)
        assertEquals(startPosition, vehicle.position)
        assertEquals(route, vehicle.route)
        assertEquals(2f, vehicle.speed)
        assertEquals(VehicleState.Moving, vehicle.state)
        assertEquals(0, vehicle.crossingsPassed)
        assertEquals(0f, vehicle.waitTime)
    }

    @Test
    fun shouldInterpolatePositionWhenMoving() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(2, 0))
        val route = Route(listOf(i1, i2))

        val vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 1f,
            state = VehicleState.Moving
        )

        // When
        val moved = vehicle.move(1f) // 1 second at speed 1

        // Then
        assertTrue(moved.position.x > 0f)
        assertTrue(moved.position.x <= 2f)
    }

    @Test
    fun shouldNotMoveWhenWaiting() {
        // Given
        val route = createSimpleRoute()
        val startPosition = Position(5.0, 5.0)

        val vehicle = Vehicle(
            id = "v1",
            position = startPosition,
            route = route,
            speed = 2f,
            state = VehicleState.Waiting
        )

        // When
        val moved = vehicle.move(1f)

        // Then
        assertEquals(startPosition, moved.position)
        assertEquals(1f, moved.waitTime)
    }

    @Test
    fun shouldNotMoveWhenArrived() {
        // Given
        val route = createSimpleRoute()
        val vehicle = Vehicle(
            id = "v1",
            position = Position(10.0, 10.0),
            route = route,
            speed = 2f,
            state = VehicleState.Arrived
        )

        // When
        val moved = vehicle.move(1f)

        // Then
        assertEquals(vehicle.position, moved.position)
        assertEquals(VehicleState.Arrived, moved.state)
    }

    @Test
    fun shouldNotMoveWhenCrashed() {
        // Given
        val route = createSimpleRoute()
        val vehicle = Vehicle(
            id = "v1",
            position = Position(5.0, 5.0),
            route = route,
            speed = 2f,
            state = VehicleState.Crashed
        )

        // When
        val moved = vehicle.move(1f)

        // Then
        assertEquals(vehicle.position, moved.position)
        assertEquals(VehicleState.Crashed, moved.state)
    }

    @Test
    fun shouldMoveTowardsTargetPosition() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(10, 0))
        val route = Route(listOf(i1, i2))

        val vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )

        // When - Move for 2 seconds
        val moved = vehicle.move(2f)

        // Then - Should have moved 4 units (2 seconds * 2 speed)
        assertTrue(moved.position.x in 3.9f..4.1f)
        assertEquals(0.0, moved.position.y)
    }

    @Test
    fun shouldHandleMultipleMoveSteps() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(10, 0))
        val route = Route(listOf(i1, i2))

        var vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 1f,
            state = VehicleState.Moving
        )

        // When - Move in small steps
        repeat(5) {
            vehicle = vehicle.move(0.1f)
        }

        // Then - Total movement should be 0.5 units
        assertTrue(vehicle.position.x in 0.4f..0.6f)
    }

    @Test
    fun shouldAccumulateWaitTimeWhenWaiting() {
        // Given
        val route = createSimpleRoute()
        var vehicle = Vehicle(
            id = "v1",
            position = Position(5.0, 5.0),
            route = route,
            speed = 2f,
            state = VehicleState.Waiting,
            waitTime = 0f
        )

        // When - Wait for multiple time steps
        vehicle = vehicle.move(1f)
        vehicle = vehicle.move(0.5f)
        vehicle = vehicle.move(2f)

        // Then
        assertEquals(3.5f, vehicle.waitTime)
    }

    @Test
    fun shouldIncrementCrossingsPassed() {
        // Given
        val route = createSimpleRoute()
        val vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 2f,
            crossingsPassed = 5
        )

        // When
        val updated = vehicle.passCrossing()

        // Then
        assertEquals(6, updated.crossingsPassed)
    }

    @Test
    fun shouldCalculateScoreWithNoWaitTime() {
        // Given
        val route = Route(
            listOf(
                Intersection(GridPosition(0, 0)),
                Intersection(GridPosition(1, 0)),
                Intersection(GridPosition(2, 0)),
                Intersection(GridPosition(3, 0))
            )
        )

        val vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 2f,
            crossingsPassed = 3,
            waitTime = 0f
        )

        // When
        val score = vehicle.calculateScore()

        // Then
        // Score = crossingsPassed + max(0, totalDistance - waitTime)
        // Score = 3 + max(0, 3 - 0) = 6
        assertEquals(6, score)
    }

    @Test
    fun shouldCalculateScoreWithWaitTimePenalty() {
        // Given
        val route = Route(
            listOf(
                Intersection(GridPosition(0, 0)),
                Intersection(GridPosition(1, 0)),
                Intersection(GridPosition(2, 0))
            )
        )

        val vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 2f,
            crossingsPassed = 2,
            waitTime = 1f
        )

        // When
        val score = vehicle.calculateScore()

        // Then
        // Score = 2 + max(0, 2 - 1) = 3
        assertEquals(3, score)
    }

    @Test
    fun shouldCalculateMinimumScoreWhenWaitTimeExceedsDistance() {
        // Given
        val route = Route(
            listOf(
                Intersection(GridPosition(0, 0)),
                Intersection(GridPosition(1, 0))
            )
        )

        val vehicle = Vehicle(
            id = "v1",
            position = Position.ZERO,
            route = route,
            speed = 2f,
            crossingsPassed = 1,
            waitTime = 5f
        )

        // When
        val score = vehicle.calculateScore()

        // Then
        // Score = 1 + max(0, 1 - 5) = 1 (only base points)
        assertEquals(1, score)
    }

    private fun createSimpleRoute(): Route {
        return Route(
            listOf(
                Intersection(GridPosition(0, 0)),
                Intersection(GridPosition(1, 0)),
                Intersection(GridPosition(2, 0))
            )
        )
    }
}
