package com.citysemaphores.domain.graph

import com.citysemaphores.domain.model.Intersection

/**
 * Represents the city graph for routing calculations.
 *
 * The graph uses an adjacency list to store connections between
 * intersections. Each intersection is connected to its neighbors via
 * weighted edges.
 *
 * Invariants:
 * - The graph must be connected (all intersections reachable)
 * - All edge weights are positive
 * - No self-loops (intersection cannot connect to itself)
 *
 * @property adjacencyList Map from intersections to their outgoing edges
 */
data class CityGraph(
    private val adjacencyList: Map<Intersection, List<Edge>>
) {

    /**
     * Number of intersections in the graph
     */
    val size: Int
        get() = adjacencyList.size

    /**
     * Returns all neighbor edges of an intersection
     *
     * @param intersection The intersection whose neighbors are being queried
     * @return List of outgoing edges, or empty list if not present
     */
    fun getNeighbors(intersection: Intersection): List<Edge> =
        adjacencyList[intersection] ?: emptyList()

    /**
     * Checks if an intersection exists in the graph
     *
     * @param intersection The intersection to check
     * @return true if the intersection is in the graph
     */
    fun hasIntersection(intersection: Intersection): Boolean =
        adjacencyList.containsKey(intersection)

    /**
     * Returns all intersections in the graph
     */
    fun getAllIntersections(): Set<Intersection> =
        adjacencyList.keys

    /**
     * Validates the graph for consistency
     *
     * @throws IllegalStateException if the graph is inconsistent
     */
    fun validate() {
        // Check that all targets exist in the adjacency list
        adjacencyList.forEach { (source, edges) ->
            edges.forEach { edge ->
                require(adjacencyList.containsKey(edge.target)) {
                    "Edge target ${edge.target.position} from ${source.position} not in graph"
                }
                require(edge.target != source) {
                    "Self-loop detected at ${source.position}"
                }
            }
        }
    }

    override fun toString(): String =
        "CityGraph(${adjacencyList.size} intersections, ${adjacencyList.values.sumOf { it.size }} edges)"
}
