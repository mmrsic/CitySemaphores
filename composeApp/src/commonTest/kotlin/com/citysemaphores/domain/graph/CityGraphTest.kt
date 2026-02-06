package com.citysemaphores.domain.graph

import com.citysemaphores.domain.model.GridPosition
import com.citysemaphores.domain.model.Intersection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for CityGraph adjacency list functionality
 */
class CityGraphTest {

    @Test
    fun shouldCreateEmptyGraph() {
        // Given
        val emptyGraph = CityGraph(emptyMap())

        // Then
        assertNotNull(emptyGraph)
        assertEquals(0, emptyGraph.size)
    }

    @Test
    fun shouldCreateGraphWithSingleIntersection() {
        // Given
        val intersection = Intersection(GridPosition(0, 0))
        val adjacencyList = mapOf(intersection to emptyList<Edge>())

        // When
        val graph = CityGraph(adjacencyList)

        // Then
        assertEquals(1, graph.size)
        assertTrue(graph.hasIntersection(intersection))
    }

    @Test
    fun shouldCreateGraphWithConnectedIntersections() {
        // Given
        val intersection1 = Intersection(GridPosition(0, 0))
        val intersection2 = Intersection(GridPosition(1, 0))
        val intersection3 = Intersection(GridPosition(0, 1))

        val edge1to2 = Edge(intersection2, weight = 1)
        val edge1to3 = Edge(intersection3, weight = 1)
        val edge2to1 = Edge(intersection1, weight = 1)

        val adjacencyList = mapOf(
            intersection1 to listOf(edge1to2, edge1to3),
            intersection2 to listOf(edge2to1),
            intersection3 to emptyList()
        )

        // When
        val graph = CityGraph(adjacencyList)

        // Then
        assertEquals(3, graph.size)
        assertEquals(2, graph.getNeighbors(intersection1).size)
        assertEquals(1, graph.getNeighbors(intersection2).size)
        assertEquals(0, graph.getNeighbors(intersection3).size)
    }

    @Test
    fun shouldRetrieveNeighborsOfIntersection() {
        // Given
        val intersection1 = Intersection(GridPosition(0, 0))
        val intersection2 = Intersection(GridPosition(1, 0))
        val intersection3 = Intersection(GridPosition(2, 0))

        val edge1to2 = Edge(intersection2, weight = 1)
        val edge1to3 = Edge(intersection3, weight = 1)

        val adjacencyList = mapOf(
            intersection1 to listOf(edge1to2, edge1to3),
            intersection2 to emptyList(),
            intersection3 to emptyList()
        )

        // When
        val graph = CityGraph(adjacencyList)
        val neighbors = graph.getNeighbors(intersection1)

        // Then
        assertEquals(2, neighbors.size)
        assertTrue(neighbors.any { it.target == intersection2 })
        assertTrue(neighbors.any { it.target == intersection3 })
    }

    @Test
    fun shouldReturnEmptyListForIntersectionWithoutNeighbors() {
        // Given
        val intersection = Intersection(GridPosition(0, 0))
        val adjacencyList = mapOf(intersection to emptyList<Edge>())

        // When
        val graph = CityGraph(adjacencyList)
        val neighbors = graph.getNeighbors(intersection)

        // Then
        assertEquals(0, neighbors.size)
    }

    @Test
    fun shouldCorrectlyIdentifyGridTopology() {
        // Given - 2x2 grid
        val i00 = Intersection(GridPosition(0, 0))
        val i10 = Intersection(GridPosition(1, 0))
        val i01 = Intersection(GridPosition(0, 1))
        val i11 = Intersection(GridPosition(1, 1))

        val adjacencyList = mapOf(
            i00 to listOf(Edge(i10), Edge(i01)),
            i10 to listOf(Edge(i00), Edge(i11)),
            i01 to listOf(Edge(i00), Edge(i11)),
            i11 to listOf(Edge(i10), Edge(i01))
        )

        // When
        val graph = CityGraph(adjacencyList)

        // Then
        assertEquals(4, graph.size)
        // All nodes have exactly 2 neighbors (grid topology)
        assertEquals(2, graph.getNeighbors(i00).size)
        assertEquals(2, graph.getNeighbors(i10).size)
        assertEquals(2, graph.getNeighbors(i01).size)
        assertEquals(2, graph.getNeighbors(i11).size)
    }
}
