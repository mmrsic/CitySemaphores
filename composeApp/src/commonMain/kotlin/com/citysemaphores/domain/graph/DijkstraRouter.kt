package com.citysemaphores.domain.graph

import com.citysemaphores.domain.model.Intersection
import com.citysemaphores.domain.model.Route

/**
 * Implements Dijkstra's algorithm for route calculation.
 *
 * The router finds the shortest path between two intersections in the city graph.
 * Target performance: <100ms for 20×20 grid (400 vertices, ~1600 edges)
 *
 * @property graph The city graph on which routing is performed
 */
class DijkstraRouter(private val graph: CityGraph) {

    /**
     * Finds the shortest path from start to destination.
     *
     * Uses Dijkstra's algorithm with a priority queue.
     * Complexity: O((V + E) log V)
     *
     * @param start Starting intersection
     * @param destination Target intersection
     * @return Route from start to destination, or null if no path exists
     */
    fun findShortestPath(start: Intersection, destination: Intersection): Route? {
        // Special case: start = destination
        if (start == destination) {
            return Route(listOf(start))
        }

        // Initialization
        val distances = mutableMapOf<Intersection, Int>().withDefault { Int.MAX_VALUE }
        val previous = mutableMapOf<Intersection, Intersection>()
        val visited = mutableSetOf<Intersection>()
        val queue = PriorityQueueImpl<Node>()

        distances[start] = 0
        queue.add(Node(start, 0))

        // Dijkstra main loop
        while (queue.isNotEmpty()) {
            val current = queue.poll()

            // Already visited?
            if (current.intersection in visited) {
                continue
            }

            visited.add(current.intersection)

            // Destination reached?
            if (current.intersection == destination) {
                break
            }

            // Examine neighbors
            val currentDistance = distances.getValue(current.intersection)

            graph.getNeighbors(current.intersection).forEach { edge ->
                if (edge.target !in visited) {
                    val newDistance = currentDistance + edge.weight
                    val oldDistance = distances.getValue(edge.target)

                    if (newDistance < oldDistance) {
                        distances[edge.target] = newDistance
                        previous[edge.target] = current.intersection
                        queue.add(Node(edge.target, newDistance))
                    }
                }
            }
        }

        // Reconstruct path
        return reconstructPath(previous, start, destination)
    }

    /**
     * Reconstructs the path from the previous-map
     */
    private fun reconstructPath(
        previous: Map<Intersection, Intersection>,
        start: Intersection,
        destination: Intersection
    ): Route? {
        val path = mutableListOf<Intersection>()
        var current: Intersection? = destination

        // Backwards from destination to start
        while (current != null) {
            path.add(0, current)
            current = previous[current]
        }

        // Check if a valid path was found
        return if (path.isNotEmpty() && path.first() == start) {
            Route(path)
        } else {
            null
        }
    }

    /**
     * Node for the priority queue
     */
    private data class Node(
        val intersection: Intersection,
        val distance: Int
    ) : Comparable<Node> {
        override fun compareTo(other: Node): Int =
            this.distance.compareTo(other.distance)
    }
}

/**
 * Simple priority queue implementation for Kotlin Multiplatform.
 *
 * Uses a sorted list. Sufficient performance for small to medium graphs.
 * Can be replaced with a more efficient implementation if needed.
 */
private class PriorityQueueImpl<T : Comparable<T>> {
    private val list = mutableListOf<T>()

    fun add(element: T) {
        val index = list.binarySearch(element)
        val insertIndex = if (index < 0) -(index + 1) else index
        list.add(insertIndex, element)
    }

    fun poll(): T = list.removeAt(0)

    fun isNotEmpty(): Boolean = list.isNotEmpty()

    val size: Int get() = list.size
}
