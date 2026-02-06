package com.citysemaphores.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for Route functionality
 */
class RouteTest {

    @Test
    fun shouldCreateRouteWithValidPath() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0))
        )

        // When
        val route = Route(intersections)

        // Then
        assertEquals(intersections[0], route.start)
        assertEquals(intersections[2], route.destination)
        assertEquals(intersections[0], route.current)
        assertEquals(intersections[1], route.next)
    }

    @Test
    fun shouldAdvanceToNextIntersection() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0))
        )
        val route = Route(intersections, currentIndex = 0)

        // When
        val advanced = route.advance()

        // Then
        assertEquals(1, advanced.currentIndex)
        assertEquals(intersections[1], advanced.current)
        assertEquals(intersections[2], advanced.next)
    }

    @Test
    fun shouldNotAdvanceBeyondDestination() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0))
        )
        val route = Route(intersections, currentIndex = 1)

        // When
        val advanced = route.advance()

        // Then
        assertEquals(1, advanced.currentIndex)
        assertEquals(route, advanced)
    }

    @Test
    fun shouldDetectWhenNotAtDestination() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0))
        )
        val route = Route(intersections, currentIndex = 0)

        // When
        val atDestination = route.isAtDestination()

        // Then
        assertFalse(atDestination)
    }

    @Test
    fun shouldDetectWhenAtDestination() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0))
        )
        val route = Route(intersections, currentIndex = 2)

        // When
        val atDestination = route.isAtDestination()

        // Then
        assertTrue(atDestination)
    }

    @Test
    fun shouldReturnNullForNextWhenAtDestination() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0))
        )
        val route = Route(intersections, currentIndex = 1)

        // When
        val next = route.next

        // Then
        assertNull(next)
    }

    @Test
    fun shouldCalculateTotalDistanceCorrectly() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0)),
            Intersection(GridPosition(3, 0))
        )
        val route = Route(intersections)

        // When
        val distance = route.totalDistance

        // Then
        assertEquals(3, distance) // 4 nodes = 3 edges
    }

    @Test
    fun shouldHandleSingleIntersectionRoute() {
        // Given
        val intersections = listOf(Intersection(GridPosition(0, 0)))
        val route = Route(intersections)

        // Then
        assertEquals(0, route.totalDistance)
        assertTrue(route.isAtDestination())
        assertNull(route.next)
        assertEquals(route.start, route.destination)
    }

    @Test
    fun shouldAdvanceThroughCompleteRoute() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0))
        )
        var route = Route(intersections)

        // When & Then
        assertFalse(route.isAtDestination())
        assertEquals(0, route.currentIndex)

        route = route.advance()
        assertFalse(route.isAtDestination())
        assertEquals(1, route.currentIndex)

        route = route.advance()
        assertTrue(route.isAtDestination())
        assertEquals(2, route.currentIndex)

        // Further advance() should not change anything
        val finalRoute = route.advance()
        assertEquals(route, finalRoute)
    }

    @Test
    fun shouldProvideCorrectCurrentAndNextThroughoutJourney() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(1, 0))
        val i3 = Intersection(GridPosition(2, 0))
        val intersections = listOf(i1, i2, i3)

        // When
        var route = Route(intersections)

        // Then - Start
        assertEquals(i1, route.current)
        assertEquals(i2, route.next)

        // When - Advance
        route = route.advance()

        // Then - Middle
        assertEquals(i2, route.current)
        assertEquals(i3, route.next)

        // When - Advance
        route = route.advance()

        // Then - End
        assertEquals(i3, route.current)
        assertNull(route.next)
    }
}
