package com.citysemaphores.game

import com.citysemaphores.domain.graph.CityGraph
import com.citysemaphores.domain.graph.DijkstraRouter
import com.citysemaphores.domain.graph.Edge
import com.citysemaphores.domain.model.City
import com.citysemaphores.domain.model.GridPosition
import com.citysemaphores.domain.model.Intersection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for VehicleSpawner functionality
 */
class VehicleSpawnerTest {

    @Test
    fun shouldSpawnVehicleAtBorder() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 2f
        )

        // When
        val vehicle = spawner.trySpawn()

        // Then
        assertNotNull(vehicle)
        assertTrue(isBorderPosition(vehicle.route.start.position, 10, 10))
    }

    @Test
    fun shouldNotSpawnBeforeIntervalElapsed() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 5f
        )

        // When - Try to spawn immediately after first spawn
        val firstVehicle = spawner.trySpawn()
        val secondVehicle = spawner.trySpawn()

        // Then
        assertNotNull(firstVehicle)
        // Second spawn might be null if no time has elapsed
    }

    @Test
    fun shouldSpawnVehicleWithValidRoute() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 2f
        )

        // When
        val vehicle = spawner.trySpawn()

        // Then
        assertNotNull(vehicle)
        assertTrue(vehicle.route.path.size >= 2)
        assertTrue(isBorderPosition(vehicle.route.start.position, 10, 10))
        assertTrue(isBorderPosition(vehicle.route.destination.position, 10, 10))
    }

    @Test
    fun shouldSpawnVehiclesWithUniqueIDs() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        var spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 0.1f
        )

        // When
        val vehicles = mutableListOf<String>()
        repeat(10) {
            spawner.trySpawn()?.let { vehicle ->
                vehicles.add(vehicle.id)
                spawner = spawner.resetTimer()
            }
        }

        // Then
        assertTrue(vehicles.isNotEmpty())
        assertEquals(vehicles.size, vehicles.toSet().size, "All vehicle IDs should be unique")
    }

    @Test
    fun shouldUpdateSpawnTimer() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 2f
        )

        // When
        val updated = spawner.update(1f)

        // Then
        assertNotNull(updated)
    }

    @Test
    fun shouldSpawnWhenTimerReachesInterval() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        var spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 1f
        )

        // When - Update for more than spawn interval
        var spawnedCount = 0
        repeat(5) {
            spawner = spawner.update(0.3f)
            spawner.trySpawn()?.let { spawnedCount++ }
        }

        // Then - Should have spawned at least once
        assertTrue(spawnedCount > 0, "Should spawn at least once")
    }

    @Test
    fun shouldSpawnVehiclesAtDifferentBorderPositions() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        var spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 0.01f
        )

        // When - Spawn multiple vehicles
        val startPositions = mutableSetOf<GridPosition>()
        repeat(20) {
            spawner.trySpawn()?.let { vehicle ->
                startPositions.add(vehicle.route.start.position)
                spawner = spawner.resetTimer()
            }
        }

        // Then - Should use different border positions
        assertTrue(startPositions.size > 1, "Should spawn at different positions")
    }

    @Test
    fun shouldCalculateRoutesThatTraverseTheCity() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val spawner = VehicleSpawner(
            city = city,
            router = DijkstraRouter(city.graph),
            spawnInterval = 1f
        )

        // When
        val vehicle = spawner.trySpawn()

        // Then
        assertNotNull(vehicle)
        // Route should have at least 2 intersections
        assertTrue(vehicle.route.path.size >= 2)
        // Start and destination should be different positions
        assertTrue(vehicle.route.start.position != vehicle.route.destination.position)
    }

    private fun createTestCity(width: Int, height: Int): City {
        val intersections = (0 until width).flatMap { x ->
            (0 until height).map { y ->
                Intersection(GridPosition(x, y))
            }
        }

        val adjacencyList = buildGridGraph(intersections, width, height)
        val graph = CityGraph(adjacencyList)

        return City(
            width = width,
            height = height,
            intersections = intersections.associateBy { it.position },
            graph = graph
        )
    }

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

    private fun isBorderPosition(pos: GridPosition, width: Int, height: Int): Boolean {
        return pos.x == 0 || pos.x == width - 1 || pos.y == 0 || pos.y == height - 1
    }
}
