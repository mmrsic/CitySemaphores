package com.citysemaphores.integration

import com.citysemaphores.domain.graph.CityGraph
import com.citysemaphores.domain.graph.DijkstraRouter
import com.citysemaphores.domain.graph.Edge
import com.citysemaphores.domain.model.*
import com.citysemaphores.game.VehicleSpawner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the Vehicle Spawning and Routing System
 */
class VehicleSpawningTest {

    @Test
    fun shouldSpawnVehicleAndCalculateValidRouteThroughCity() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val router = DijkstraRouter(city.graph)
        val spawner = VehicleSpawner(
            city = city,
            router = router,
            spawnInterval = 1f
        )

        // When
        val vehicle = spawner.trySpawn()

        // Then
        assertNotNull(vehicle, "Vehicle should be spawned")

        // Verify route is valid
        assertTrue(vehicle.route.path.size >= 2, "Route should have at least 2 intersections")

        // Verify start and destination are at borders
        val start = vehicle.route.start.position
        val dest = vehicle.route.destination.position
        assertTrue(isBorderPosition(start, 10, 10), "Start should be at border")
        assertTrue(isBorderPosition(dest, 10, 10), "Destination should be at border")

        // Verify initial vehicle state
        assertEquals(VehicleState.Moving, vehicle.state)
        assertEquals(0, vehicle.crossingsPassed)
        assertEquals(0f, vehicle.waitTime)
    }

    @Test
    fun shouldFollowCompleteRouteFromSpawnToDestination() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val router = DijkstraRouter(city.graph)
        val spawner = VehicleSpawner(
            city = city,
            router = router,
            spawnInterval = 1f
        )

        val initialVehicle = spawner.trySpawn()
        assertNotNull(initialVehicle)

        // When - Advance through entire route
        var currentVehicle: Vehicle = initialVehicle
        val totalSteps = currentVehicle.route.path.size - 1

        repeat(totalSteps) {
            currentVehicle = currentVehicle.copy(route = currentVehicle.route.advance())
        }

        // Then
        assertTrue(currentVehicle.route.isAtDestination(), "Vehicle should reach destination")
        assertEquals(currentVehicle.route.destination, currentVehicle.route.current)
    }

    @Test
    fun shouldSpawnMultipleVehiclesWithDifferentRoutes() {
        // Given
        val city = createTestCity(width = 10, height = 10)
        val router = DijkstraRouter(city.graph)
        var spawner = VehicleSpawner(
            city = city,
            router = router,
            spawnInterval = 0.1f
        )

        // When - Spawn multiple vehicles
        val vehicles = mutableListOf<Vehicle>()
        repeat(10) {
            val vehicle = spawner.trySpawn()
            if (vehicle != null) {
                vehicles.add(vehicle)
                spawner = spawner.resetTimer()
            }
        }

        // Then
        assertTrue(vehicles.size >= 5, "Should spawn multiple vehicles")

        // All vehicles should have unique IDs
        val uniqueIds = vehicles.map { it.id }.toSet()
        assertEquals(vehicles.size, uniqueIds.size, "All IDs should be unique")

        // At least some vehicles should have different start positions
        val startPositions = vehicles.map { it.route.start.position }.toSet()
        assertTrue(startPositions.size > 1, "Should use different spawn points")
    }

    @Test
    fun shouldVerifyDijkstraCalculatesShortestPath() {
        // Given - Create a simple grid where the shortest path is clear
        val i00 = Intersection(GridPosition(0, 0))
        val i10 = Intersection(GridPosition(1, 0))
        val i20 = Intersection(GridPosition(2, 0))
        val i01 = Intersection(GridPosition(0, 1))
        val i11 = Intersection(GridPosition(1, 1))
        val i21 = Intersection(GridPosition(2, 1))

        val graph = CityGraph(
            mapOf(
                i00 to listOf(Edge(i10, 1), Edge(i01, 1)),
                i10 to listOf(Edge(i00, 1), Edge(i20, 1), Edge(i11, 1)),
                i20 to listOf(Edge(i10, 1), Edge(i21, 1)),
                i01 to listOf(Edge(i00, 1), Edge(i11, 1)),
                i11 to listOf(Edge(i10, 1), Edge(i01, 1), Edge(i21, 1)),
                i21 to listOf(Edge(i20, 1), Edge(i11, 1))
            )
        )

        val router = DijkstraRouter(graph)

        // When - Find shortest path from (0,0) to (2,1)
        val route = router.findShortestPath(i00, i21)

        // Then
        assertNotNull(route)
        // Shortest path should be: (0,0) -> (1,0) -> (2,0) -> (2,1) = 4 nodes, 3 edges
        // OR: (0,0) -> (1,0) -> (1,1) -> (2,1) = 4 nodes, 3 edges
        assertEquals(4, route.path.size, "Shortest path should have 4 nodes")
        assertEquals(i00, route.start)
        assertEquals(i21, route.destination)
    }

    @Test
    fun shouldHandleVehicleMovementAlongRoute() {
        // Given
        val i1 = Intersection(GridPosition(0, 0))
        val i2 = Intersection(GridPosition(5, 0))
        val i3 = Intersection(GridPosition(10, 0))
        val route = Route(listOf(i1, i2, i3))

        var vehicle = Vehicle(
            id = "test-vehicle",
            position = Position(0.0, 0.0),
            route = route,
            speed = 2f,
            state = VehicleState.Moving
        )

        // When - Move vehicle for several time steps
        repeat(10) {
            vehicle = vehicle.move(0.5f) // Move 1 unit per step (2 * 0.5)
        }

        // Then - Vehicle should have moved significantly
        // Total movement: 10 steps * 0.5s * 2 speed = 10 units
        // Should be past the first waypoint at x=5
        assertTrue(vehicle.position.x > 4.0, "Vehicle should have moved forward, but is at ${vehicle.position.x}")
    }

    @Test
    fun shouldCalculateCorrectTotalDistanceForRoute() {
        // Given
        val intersections = listOf(
            Intersection(GridPosition(0, 0)),
            Intersection(GridPosition(1, 0)),
            Intersection(GridPosition(2, 0)),
            Intersection(GridPosition(3, 0)),
            Intersection(GridPosition(4, 0))
        )
        val route = Route(intersections)

        // When
        val distance = route.totalDistance

        // Then
        assertEquals(4, distance, "Distance should be number of road segments")
    }

    @Test
    fun shouldIntegrateSpawnerWithCityGraph() {
        // Given
        val city = createTestCity(width = 15, height = 15)
        val router = DijkstraRouter(city.graph)
        var spawner = VehicleSpawner(
            city = city,
            router = router,
            spawnInterval = 2f
        )

        // When - Simulate game loop
        val spawnedVehicles = mutableListOf<Vehicle>()
        repeat(20) { _ ->
            spawner = spawner.update(0.5f)
            spawner.trySpawn()?.let { spawnedVehicles.add(it) }
        }

        // Then
        assertTrue(spawnedVehicles.isNotEmpty(), "Should spawn vehicles over time")

        // All vehicles should have valid routes
        spawnedVehicles.forEach { vehicle ->
            assertTrue(vehicle.route.path.size >= 2)
            assertTrue(isBorderPosition(vehicle.route.start.position, 15, 15))
            assertTrue(isBorderPosition(vehicle.route.destination.position, 15, 15))
        }
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
