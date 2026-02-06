package com.citysemaphores.domain.graph

import com.citysemaphores.domain.model.GridPosition
import com.citysemaphores.domain.model.Intersection
import kotlin.test.*
import kotlin.time.measureTime

/**
 * Tests for DijkstraRouter shortest path functionality
 */
class DijkstraRouterTest {

    @Test
    fun shouldFindShortestPathInSimpleLinearGraph() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(1, 0))
        val i3 = Intersection(GridPosition(2, 0))

        val graph = CityGraph(
            mapOf(
                i1 to listOf(Edge(i2, 1)),
                i2 to listOf(Edge(i1, 1), Edge(i3, 1)),
                i3 to listOf(Edge(i2, 1))
            )
        )

        val router = DijkstraRouter(graph)

        // When
        val route = router.findShortestPath(i1, i3)

        // Then
        assertNotNull(route)
        assertEquals(3, route.path.size)
        assertEquals(i1, route.path[0])
        assertEquals(i2, route.path[1])
        assertEquals(i3, route.path[2])
    }

    @Test
    fun shouldFindShortestPathWithMultipleOptions() {
        // Given - Diamond topology
        //    i2
        //   /  \
        //  i1  i4
        //   \  /
        //    i3
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(1, 0))
        val i3 = Intersection(GridPosition(1, 1))
        val i4 = Intersection(GridPosition(2, 0))

        val graph = CityGraph(
            mapOf(
                i1 to listOf(Edge(i2, 1), Edge(i3, 2)),
                i2 to listOf(Edge(i1, 1), Edge(i4, 1)),
                i3 to listOf(Edge(i1, 2), Edge(i4, 1)),
                i4 to listOf(Edge(i2, 1), Edge(i3, 1))
            )
        )

        val router = DijkstraRouter(graph)

        // When
        val route = router.findShortestPath(i1, i4)

        // Then
        assertNotNull(route)
        // Shortest path: i1 -> i2 -> i4 (weight 2)
        assertEquals(3, route.path.size)
        assertEquals(i1, route.path[0])
        assertEquals(i2, route.path[1])
        assertEquals(i4, route.path[2])
    }

    @Test
    fun shouldReturnNullWhenNoPathExists() {
        // Given - Two isolated islands
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(1, 0))
        val i3 = Intersection(GridPosition(5, 5))
        val i4 = Intersection(GridPosition(6, 5))

        val graph = CityGraph(
            mapOf(
                i1 to listOf(Edge(i2, 1)),
                i2 to listOf(Edge(i1, 1)),
                i3 to listOf(Edge(i4, 1)),
                i4 to listOf(Edge(i3, 1))
            )
        )

        val router = DijkstraRouter(graph)

        // When
        val route = router.findShortestPath(i1, i3)

        // Then
        assertNull(route)
    }

    @Test
    fun shouldHandlePathFromNodeToItself() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val graph = CityGraph(mapOf(i1 to emptyList()))
        val router = DijkstraRouter(graph)

        // When
        val route = router.findShortestPath(i1, i1)

        // Then
        assertNotNull(route)
        assertEquals(1, route.path.size)
        assertEquals(i1, route.path[0])
    }

    @Test
    fun shouldFindPathIn3x3Grid() {
        // Given - 3x3 Grid
        val intersections = (0..2).flatMap { x ->
            (0..2).map { y -> Intersection(GridPosition(x, y)) }
        }

        val adjacencyList = buildGridGraph(intersections, 3, 3)
        val graph = CityGraph(adjacencyList)
        val router = DijkstraRouter(graph)

        val start = intersections.first { it.position == GridPosition(0, 0) }
        val end = intersections.first { it.position == GridPosition(2, 2) }

        // When
        val route = router.findShortestPath(start, end)

        // Then
        assertNotNull(route)
        assertTrue(route.path.size >= 5) // At least 5 steps in 3x3 grid
        assertEquals(start, route.path.first())
        assertEquals(end, route.path.last())
    }

    @Test
    fun shouldCompletePathfindingInUnder100msFor20x20Grid() {
        // Given - 20x20 Grid (400 nodes, ~1600 edges)
        val intersections = (0..19).flatMap { x ->
            (0..19).map { y -> Intersection(GridPosition(x, y)) }
        }

        val adjacencyList = buildGridGraph(intersections, 20, 20)
        val graph = CityGraph(adjacencyList)
        val router = DijkstraRouter(graph)

        val start = intersections.first { it.position == GridPosition(0, 0) }
        val end = intersections.first { it.position == GridPosition(19, 19) }

        // When
        val duration = measureTime {
            router.findShortestPath(start, end)
        }

        // Then
        println("Dijkstra 20x20 Grid: ${duration.inWholeMilliseconds}ms")
        assertTrue(duration.inWholeMilliseconds < 100, "Pathfinding took ${duration.inWholeMilliseconds}ms, expected < 100ms")
    }

    @Test
    fun shouldHandleWeightedEdgesCorrectly() {
        // Given - Graph with different weights
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(1, 0))
        val i3 = Intersection(GridPosition(2, 0))

        val graph = CityGraph(
            mapOf(
                i1 to listOf(Edge(i2, 1), Edge(i3, 5)),
                i2 to listOf(Edge(i1, 1), Edge(i3, 1)),
                i3 to listOf(Edge(i2, 1), Edge(i1, 5))
            )
        )

        val router = DijkstraRouter(graph)

        // When
        val route = router.findShortestPath(i1, i3)

        // Then - Should take the path via i2 (weight 2 instead of 5)
        assertNotNull(route)
        assertEquals(3, route.path.size)
        assertEquals(i1, route.path[0])
        assertEquals(i2, route.path[1])
        assertEquals(i3, route.path[2])
    }

    /**
     * Helper: Creates a rectangular grid graph
     */
    private fun buildGridGraph(
        intersections: List<Intersection>,
        width: Int,
        height: Int
    ): Map<Intersection, List<Edge>> {
        val positionMap = intersections.associateBy { it.position }

        return intersections.associateWith { intersection ->
            val pos = intersection.position
            val neighbors = mutableListOf<Edge>()

            // North
            if (pos.y > 0) {
                positionMap[GridPosition(pos.x, pos.y - 1)]?.let {
                    neighbors.add(Edge(it, 1))
                }
            }
            // South
            if (pos.y < height - 1) {
                positionMap[GridPosition(pos.x, pos.y + 1)]?.let {
                    neighbors.add(Edge(it, 1))
                }
            }
            // West
            if (pos.x > 0) {
                positionMap[GridPosition(pos.x - 1, pos.y)]?.let {
                    neighbors.add(Edge(it, 1))
                }
            }
            // East
            if (pos.x < width - 1) {
                positionMap[GridPosition(pos.x + 1, pos.y)]?.let {
                    neighbors.add(Edge(it, 1))
                }
            }

            neighbors
        }
    }
}
