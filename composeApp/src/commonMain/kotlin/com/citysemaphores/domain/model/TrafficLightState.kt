package com.citysemaphores.domain.model

/**
 * Represents the state of a traffic light.
 * Each traffic light can only be Red or Green (no Yellow in this game).
 */
enum class TrafficLightState {
    RED,
    GREEN;

    /**
     * Toggles between Red and Green.
     */
    fun toggle(): TrafficLightState = when (this) {
        RED -> GREEN
        GREEN -> RED
    }

    /**
     * Returns true if vehicles can pass (Green light).
     */
    fun canPass(): Boolean = this == GREEN
}
