package com.citysemaphores.viewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Main ViewModel for the game.
 * Manages game state and handles user intents following MVI architecture.
 */
class GameViewModel {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GameUiState.initial())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /**
     * Handle user intents.
     */
    fun handleIntent(intent: GameIntent) {
        viewModelScope.launch {
            when (intent) {
                is GameIntent.StartGame -> startGame(intent.gridWidth, intent.gridHeight)
                is GameIntent.PauseGame -> pauseGame()
                is GameIntent.ResumeGame -> resumeGame()
                is GameIntent.StopGame -> stopGame()
                is GameIntent.RestartGame -> restartGame()
                is GameIntent.ToggleTrafficLight -> toggleTrafficLight(intent.intersection, intent.direction)
                is GameIntent.SetIntersectionLights -> setIntersectionLights(intent.intersection, intent.greenDirections)
                is GameIntent.ToggleAllLights -> toggleAllLights(intent.intersection)
                is GameIntent.SetTrafficLight -> setTrafficLight(intent.intersection, intent.direction, intent.isGreen)
                is GameIntent.ClearEvents -> clearEvents()
                is GameIntent.AcknowledgeGameOver -> acknowledgeGameOver()
            }
        }
    }

    private fun startGame(gridWidth: Int, gridHeight: Int) {
        // Initialize intersections for the grid
        val intersections = mutableListOf<IntersectionUiState>()
        for (y in 0 until gridHeight) {
            for (x in 0 until gridWidth) {
                val position = com.citysemaphores.domain.model.GridPosition(x, y)
                intersections.add(
                    IntersectionUiState(
                        position = position,
                        trafficLights = mapOf(
                            com.citysemaphores.domain.model.Direction.NORTH to false,
                            com.citysemaphores.domain.model.Direction.SOUTH to false,
                            com.citysemaphores.domain.model.Direction.EAST to false,
                            com.citysemaphores.domain.model.Direction.WEST to false
                        ),
                        isBlocked = false,
                        blockingTimeRemaining = 0.0,
                        vehiclesOnIntersection = emptyMap()
                    )
                )
            }
        }

        _uiState.value = GameUiState.initial(gridWidth, gridHeight).copy(
            isGameRunning = true,
            intersections = intersections
        )
    }

    private fun pauseGame() {
        _uiState.value = _uiState.value.copy(isGameRunning = false)
    }

    private fun resumeGame() {
        _uiState.value = _uiState.value.copy(isGameRunning = true)
    }

    private fun stopGame() {
        _uiState.value = GameUiState.initial()
    }

    private fun restartGame() {
        val currentState = _uiState.value
        startGame(currentState.gridWidth, currentState.gridHeight)
    }

    private fun toggleTrafficLight(intersection: com.citysemaphores.domain.model.GridPosition, direction: com.citysemaphores.domain.model.Direction) {
        val currentState = _uiState.value
        val intersections = currentState.intersections.map { intersectionState ->
            if (intersectionState.position == intersection) {
                val currentLights = intersectionState.trafficLights
                val newState = !(currentLights[direction] ?: false)
                intersectionState.copy(
                    trafficLights = currentLights + (direction to newState)
                )
            } else {
                intersectionState
            }
        }
        _uiState.value = currentState.copy(intersections = intersections)
    }

    private fun setIntersectionLights(intersection: com.citysemaphores.domain.model.GridPosition, greenDirections: Set<com.citysemaphores.domain.model.Direction>) {
        val currentState = _uiState.value
        val intersections = currentState.intersections.map { intersectionState ->
            if (intersectionState.position == intersection) {
                val newLights = com.citysemaphores.domain.model.Direction.entries.associateWith { it in greenDirections }
                intersectionState.copy(trafficLights = newLights)
            } else {
                intersectionState
            }
        }
        _uiState.value = currentState.copy(intersections = intersections)
    }

    private fun toggleAllLights(intersection: com.citysemaphores.domain.model.GridPosition) {
        val currentState = _uiState.value
        val intersections = currentState.intersections.map { intersectionState ->
            if (intersectionState.position == intersection) {
                val newLights = intersectionState.trafficLights.mapValues { !it.value }
                intersectionState.copy(trafficLights = newLights)
            } else {
                intersectionState
            }
        }
        _uiState.value = currentState.copy(intersections = intersections)
    }

    private fun setTrafficLight(intersection: com.citysemaphores.domain.model.GridPosition, direction: com.citysemaphores.domain.model.Direction, isGreen: Boolean) {
        val currentState = _uiState.value
        val intersections = currentState.intersections.map { intersectionState ->
            if (intersectionState.position == intersection) {
                val newLights = intersectionState.trafficLights + (direction to isGreen)
                intersectionState.copy(trafficLights = newLights)
            } else {
                intersectionState
            }
        }
        _uiState.value = currentState.copy(intersections = intersections)
    }

    private fun clearEvents() {
        _uiState.value = _uiState.value.copy(recentEvents = emptyList())
    }

    private fun acknowledgeGameOver() {
        _uiState.value = GameUiState.initial()
    }

    /**
     * Clean up resources when ViewModel is no longer needed.
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}
