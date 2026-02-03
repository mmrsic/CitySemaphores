package com.citysemaphores.domain.model

/**
 * Represents a single directional traffic light at an intersection.
 * Each intersection has 4 independent traffic lights (North, South, East, West).
 *
 * @property direction The direction this traffic light controls
 * @property state Current state (Red or Green)
 */
data class TrafficLight(
    val direction: Direction,
    val state: TrafficLightState = TrafficLightState.RED
) {
    /**
     * Toggles the traffic light between Red and Green.
     */
    fun toggle(): TrafficLight = copy(state = state.toggle())

    /**
     * Returns true if vehicles from this direction can pass.
     */
    fun canPass(): Boolean = state.canPass()

    /**
     * Sets the traffic light to the specified state.
     */
    fun setState(newState: TrafficLightState): TrafficLight = copy(state = newState)

    /**
     * Sets the traffic light to Green.
     */
    fun setGreen(): TrafficLight = setState(TrafficLightState.GREEN)

    /**
     * Sets the traffic light to Red.
     */
    fun setRed(): TrafficLight = setState(TrafficLightState.RED)
}
