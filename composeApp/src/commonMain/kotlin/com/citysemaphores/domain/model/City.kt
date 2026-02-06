package com.citysemaphores.domain.model

import com.citysemaphores.domain.graph.CityGraph
import com.citysemaphores.domain.graph.Edge

/**
 * Represents the entire city with all intersections and the routing graph.
 *
 * The city is organized as a rectangular grid. Each position in the grid
 * has an intersection. The city also manages the graph for route calculation.
 *
 * Invariants:
 * - Width and height must be between 10 and 20
 * - All grid positions from (0,0) to (width-1, height-1) have intersections
 * - The graph must be connected
 *
 * @property width Width of the city grid
 * @property height Height of the city grid
 * @property intersections Map from GridPosition to Intersection
 * @property graph Graph representation for routing
 */
data class City(
    val width: Int,
    val height: Int,
    val intersections: Map<GridPosition, Intersection>,
    val graph: CityGraph
) {
    init {
        require(width in 10..20) { "City width must be between 10 and 20, got $width" }
        require(height in 10..20) { "City height must be between 10 and 20, got $height" }
        require(intersections.size == width * height) {
            "Expected ${width * height} intersections, got ${intersections.size}"
        }

        // Validate that all grid positions are present
        for (x in 0 until width) {
            for (y in 0 until height) {
                require(intersections.containsKey(GridPosition(x, y))) {
                    "Missing intersection at ($x, $y)"
                }
            }
        }
    }

    /**
     * Returns the intersection at a specific grid position
     */
    fun getIntersection(position: GridPosition): Intersection? =
        intersections[position]

    /**
     * Returns all border intersections (potential spawn/destination positions)
     */
    fun getBorderIntersections(): List<Intersection> =
        intersections.values.filter { intersection ->
            val pos = intersection.position
            pos.x == 0 || pos.x == width - 1 || pos.y == 0 || pos.y == height - 1
        }

    /**
     * Updates an intersection in the city
     */
    fun updateIntersection(intersection: Intersection): City {
        val updatedIntersections = intersections.toMutableMap()
        updatedIntersections[intersection.position] = intersection
        return copy(intersections = updatedIntersections)
    }

    /**
     * Returns all intersections as a list
     */
    fun getAllIntersections(): List<Intersection> =
        intersections.values.toList()

    override fun toString(): String =
        "City(${width}x${height}, ${intersections.size} intersections)"

    companion object {
        /**
         * Creates a new city with the given grid
         *
         * @param width Width of the grid (10-20)
         * @param height Height of the grid (10-20)
         * @return New city with fully connected grid
         */
        fun create(width: Int, height: Int): City {
            require(width in 10..20) { "Width must be between 10 and 20" }
            require(height in 10..20) { "Height must be between 10 and 20" }

            // Create all intersections
            val intersections = mutableMapOf<GridPosition, Intersection>()
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pos = GridPosition(x, y)
                    intersections[pos] = Intersection(pos)
                }
            }

            // Create graph with connections
            val adjacencyList = buildGridGraph(intersections, width, height)
            val graph = CityGraph(adjacencyList)

            return City(
                width = width,
                height = height,
                intersections = intersections,
                graph = graph
            )
        }

        /**
         * Builds a rectangular grid graph
         */
        private fun buildGridGraph(
            intersections: Map<GridPosition, Intersection>,
            width: Int,
            height: Int
        ): Map<Intersection, List<Edge>> {
            return intersections.values.associateWith { intersection ->
                val pos = intersection.position
                val neighbors = mutableListOf<Edge>()

                // North
                if (pos.y > 0) {
                    intersections[GridPosition(pos.x, pos.y - 1)]?.let {
                        neighbors.add(Edge(it, 1))
                    }
                }
                // South
                if (pos.y < height - 1) {
                    intersections[GridPosition(pos.x, pos.y + 1)]?.let {
                        neighbors.add(Edge(it, 1))
                    }
                }
                // West
                if (pos.x > 0) {
                    intersections[GridPosition(pos.x - 1, pos.y)]?.let {
                        neighbors.add(Edge(it, 1))
                    }
                }
                // East
                if (pos.x < width - 1) {
                    intersections[GridPosition(pos.x + 1, pos.y)]?.let {
                        neighbors.add(Edge(it, 1))
                    }
                }

                neighbors
            }
        }
    }
}
