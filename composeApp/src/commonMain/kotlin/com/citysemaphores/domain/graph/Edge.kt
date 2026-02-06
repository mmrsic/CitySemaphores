package com.citysemaphores.domain.graph

import com.citysemaphores.domain.model.Intersection

/**
 * Represents an edge in the city graph.
 *
 * An edge connects two intersections and has a weight for route calculation.
 * In the MVP, all edges have the same weight (1).
 *
 * @property target The target intersection of this edge
 * @property weight The weight of the edge (default: 1)
 */
data class Edge(
    val target: Intersection,
    val weight: Int = 1
) {
    init {
        require(weight > 0) { "Edge weight must be positive, got $weight" }
    }

    override fun toString(): String =
        "Edge(to=${target.position}, weight=$weight)"
}
