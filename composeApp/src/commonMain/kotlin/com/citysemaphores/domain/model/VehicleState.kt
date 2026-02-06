package com.citysemaphores.domain.model

/**
 * State of a vehicle
 *
 * - **Moving**: Vehicle is moving along its route
 * - **Waiting**: Vehicle is waiting before an intersection (red light or traffic jam)
 * - **Arrived**: Vehicle has reached its destination
 * - **Crashed**: Vehicle was involved in a collision
 */
enum class VehicleState {
    /**
     * Vehicle is actively moving along its route
     */
    Moving,

    /**
     * Vehicle is waiting (e.g., at red light or in traffic jam)
     */
    Waiting,

    /**
     * Vehicle has reached its destination and will be removed
     */
    Arrived,

    /**
     * Vehicle was involved in a collision
     */
    Crashed
}
