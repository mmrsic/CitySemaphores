package com.citysemaphores.domain.model

/**
 * Represents a route through the city.
 *
 * A route is a list of intersections that a vehicle traverses from start to destination.
 * The route is calculated at vehicle creation using Dijkstra's algorithm.
 *
 * @property path List of intersections from start to destination
 * @property currentIndex Current index in the route (which intersection is being approached)
 */
data class Route(
    val path: List<Intersection>,
    val currentIndex: Int = 0
) {
    init {
        require(path.isNotEmpty()) { "Route path cannot be empty" }
        require(currentIndex in path.indices) { "Current index $currentIndex out of bounds for path size ${path.size}" }
    }

    /**
     * Starting intersection of the route
     */
    val start: Intersection
        get() = path.first()

    /**
     * Destination intersection of the route
     */
    val destination: Intersection
        get() = path.last()

    /**
     * Current intersection (or next one to approach)
     */
    val current: Intersection
        get() = path[currentIndex]

    /**
     * Next intersection after the current one, or null if at destination
     */
    val next: Intersection?
        get() = path.getOrNull(currentIndex + 1)

    /**
     * Total distance of the route (number of road segments)
     * Used for bonus score calculation
     */
    val totalDistance: Int
        get() = path.size - 1  // Number of edges = number of nodes - 1

    /**
     * Advances the route to the next intersection
     * @return New route with updated index, or unchanged if already at destination
     */
    fun advance(): Route =
        if (currentIndex < path.size - 1)
            copy(currentIndex = currentIndex + 1)
        else
            this

    /**
     * Checks if the vehicle has reached its destination
     */
    fun isAtDestination(): Boolean =
        currentIndex == path.size - 1

    override fun toString(): String =
        "Route(${path.size} intersections, at $currentIndex/${path.size - 1})"
}
