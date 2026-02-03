package com.citysemaphores.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntersectionTest {

    @Test
    fun intersectionShouldInitializeWithAllRedLights() {
        val intersection = Intersection(GridPosition(0, 0))

        Direction.entries.forEach { direction ->
            val light = intersection.trafficLights[direction]
            assertEquals(TrafficLightState.RED, light?.state)
        }
    }

    @Test
    fun canVehiclePassShouldReturnFalseForRedLight() {
        val intersection = Intersection(GridPosition(0, 0))
        assertFalse(intersection.canVehiclePass(Direction.NORTH))
    }

    @Test
    fun canVehiclePassShouldReturnTrueForGreenLight() {
        val intersection = Intersection(GridPosition(0, 0))
            .setTrafficLight(Direction.NORTH, TrafficLightState.GREEN)

        assertTrue(intersection.canVehiclePass(Direction.NORTH))
    }

    @Test
    fun canVehiclePassShouldReturnFalseWhenBlocked() {
        val intersection = Intersection(GridPosition(0, 0))
            .setTrafficLight(Direction.NORTH, TrafficLightState.GREEN)
            .copy(isBlocked = true)

        assertFalse(intersection.canVehiclePass(Direction.NORTH))
    }

    @Test
    fun toggleTrafficLightShouldChangeLightState() {
        val intersection = Intersection(GridPosition(0, 0))
        val toggled = intersection.toggleTrafficLight(Direction.NORTH)

        assertEquals(TrafficLightState.GREEN, toggled.trafficLights[Direction.NORTH]?.state)
    }

    @Test
    fun setAllLightsShouldSetAllLightsToSameState() {
        val intersection = Intersection(GridPosition(0, 0))
            .setAllLights(TrafficLightState.GREEN)

        Direction.entries.forEach { direction ->
            assertEquals(TrafficLightState.GREEN, intersection.trafficLights[direction]?.state)
        }
    }

    @Test
    fun canVehicleEnterShouldCheckDirectionalOccupancy() {
        val intersection = Intersection(GridPosition(0, 0))

        // Initially, no vehicle occupies any direction
        assertTrue(intersection.canVehicleEnter(Direction.NORTH, "vehicle1"))

        // After a vehicle enters, same vehicle can still "enter" (pass through)
        val occupied = intersection.enterIntersection(Direction.NORTH, "vehicle1")
        assertTrue(occupied.canVehicleEnter(Direction.NORTH, "vehicle1"))

        // Different vehicle cannot enter from same direction
        assertFalse(occupied.canVehicleEnter(Direction.NORTH, "vehicle2"))

        // Vehicle from different direction can enter
        assertTrue(occupied.canVehicleEnter(Direction.SOUTH, "vehicle2"))
    }

    @Test
    fun leaveIntersectionShouldClearDirectionalOccupancy() {
        val intersection = Intersection(GridPosition(0, 0))
            .enterIntersection(Direction.NORTH, "vehicle1")
            .leaveIntersection(Direction.NORTH)

        assertTrue(intersection.canVehicleEnter(Direction.NORTH, "vehicle2"))
    }

    @Test
    fun blockWithCollisionShouldCalculateCorrectAdditiveBlockingTimeFor1Vehicle() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1"))

        assertTrue(intersection.isBlocked)
        assertEquals(7.5, intersection.blockingTimeRemaining)
    }

    @Test
    fun blockWithCollisionShouldCalculateCorrectAdditiveBlockingTimeFor2Vehicles() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1", "v2"))

        assertTrue(intersection.isBlocked)
        assertEquals(22.5, intersection.blockingTimeRemaining) // 7.5 + 15
    }

    @Test
    fun blockWithCollisionShouldCalculateCorrectAdditiveBlockingTimeFor3Vehicles() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1", "v2", "v3"))

        assertTrue(intersection.isBlocked)
        assertEquals(52.5, intersection.blockingTimeRemaining) // 7.5 + 15 + 30
    }

    @Test
    fun blockWithCollisionShouldCalculateCorrectAdditiveBlockingTimeFor4Vehicles() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1", "v2", "v3", "v4"))

        assertTrue(intersection.isBlocked)
        assertEquals(112.5, intersection.blockingTimeRemaining) // 7.5 + 15 + 30 + 60
    }

    @Test
    fun blockWithCollisionShouldCapAt4VehiclesMax() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1", "v2", "v3", "v4", "v5"))

        assertTrue(intersection.isBlocked)
        assertEquals(112.5, intersection.blockingTimeRemaining) // Still 4-vehicle max
    }

    @Test
    fun blockWithCollisionShouldSetAllLightsToRed() {
        val intersection = Intersection(GridPosition(0, 0))
            .setAllLights(TrafficLightState.GREEN)
            .blockWithCollision(setOf("v1"))

        Direction.entries.forEach { direction ->
            assertEquals(TrafficLightState.RED, intersection.trafficLights[direction]?.state)
        }
    }

    @Test
    fun updateBlockTimerShouldDecreaseBlockingTime() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1"))

        val (updated, wasUnblocked) = intersection.updateBlockTimer(2.0)

        assertEquals(5.5, updated.blockingTimeRemaining)
        assertFalse(wasUnblocked)
        assertTrue(updated.isBlocked)
    }

    @Test
    fun updateBlockTimerShouldUnblockWhenTimeReachesZero() {
        val intersection = Intersection(GridPosition(0, 0))
            .blockWithCollision(setOf("v1"))

        val (updated, wasUnblocked) = intersection.updateBlockTimer(10.0)

        assertEquals(0.0, updated.blockingTimeRemaining)
        assertTrue(wasUnblocked)
        assertFalse(updated.isBlocked)
        assertTrue(updated.collidedVehicles.isEmpty())
    }
}
