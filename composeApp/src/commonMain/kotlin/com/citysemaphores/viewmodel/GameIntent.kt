package com.citysemaphores.viewmodel

import com.citysemaphores.domain.model.Direction
import com.citysemaphores.domain.model.GridPosition

/**
 * User intents/actions in the game.
 * Follows MVI architecture pattern.
 */
sealed interface GameIntent {
    /**
     * Start a new game with specified grid dimensions.
     */
    data class StartGame(
        val gridWidth: Int = 5,
        val gridHeight: Int = 5
    ) : GameIntent

    /**
     * Pause the game simulation.
     */
    data object PauseGame : GameIntent

    /**
     * Resume the paused game.
     */
    data object ResumeGame : GameIntent

    /**
     * Stop the current game and return to menu.
     */
    data object StopGame : GameIntent

    /**
     * Restart the game with the same settings.
     */
    data object RestartGame : GameIntent

    /**
     * Toggle a specific traffic light at an intersection.
     *
     * @param intersection The intersection position
     * @param direction The direction of the traffic light to toggle
     */
    data class ToggleTrafficLight(
        val intersection: GridPosition,
        val direction: Direction
    ) : GameIntent

    /**
     * Set all traffic lights at an intersection to the specified state.
     *
     * @param intersection The intersection position
     * @param greenDirections Directions that should have green lights
     */
    data class SetIntersectionLights(
        val intersection: GridPosition,
        val greenDirections: Set<Direction>
    ) : GameIntent

    /**
     * Toggle all traffic lights at an intersection (green -> red, red -> green).
     */
    data class ToggleAllLights(
        val intersection: GridPosition
    ) : GameIntent

    /**
     * Set a specific traffic light state.
     */
    data class SetTrafficLight(
        val intersection: GridPosition,
        val direction: Direction,
        val isGreen: Boolean
    ) : GameIntent

    /**
     * Clear recent events from the UI.
     */
    data object ClearEvents : GameIntent

    /**
     * Acknowledge game over and return to menu.
     */
    data object AcknowledgeGameOver : GameIntent
}
