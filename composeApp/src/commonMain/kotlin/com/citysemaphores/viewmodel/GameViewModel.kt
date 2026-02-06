package com.citysemaphores.viewmodel

import com.citysemaphores.domain.model.Direction
import com.citysemaphores.domain.model.GridPosition
import com.citysemaphores.domain.model.VehicleState
import com.citysemaphores.game.GameEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.TimeSource

/**
 * Main ViewModel for the game.
 * Manages game state and handles user intents following MVI architecture.
 * Integrates GameEngine for game loop and vehicle simulation.
 */
class GameViewModel {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(GameUiState.initial())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameEngine: GameEngine? = null
    private var gameLoopJob: Job? = null
    private val timeSource = TimeSource.Monotonic
    private var lastFrameMark: TimeSource.Monotonic.ValueTimeMark? = null

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
        // Stop any existing game loop
        gameLoopJob?.cancel()

        // Create GameEngine with validated city size (10-20)
        val validatedWidth = gridWidth.coerceIn(10, 20)
        val validatedHeight = gridHeight.coerceIn(10, 20)

        gameEngine = GameEngine.create(validatedWidth, validatedHeight)
        gameEngine?.start()

        // Initialize UI state
        val city = gameEngine?.getState()?.city
        val intersections = city?.getAllIntersections()?.map { intersection ->
            IntersectionUiState(
                position = intersection.position,
                trafficLights = mapOf(
                    Direction.NORTH to false,
                    Direction.SOUTH to false,
                    Direction.EAST to false,
                    Direction.WEST to false
                ),
                isBlocked = intersection.isBlocked,
                blockingTimeRemaining = intersection.blockingTimeRemaining,
                vehiclesOnIntersection = emptyMap()
            )
        } ?: emptyList()

        _uiState.value = GameUiState.initial(validatedWidth, validatedHeight).copy(
            isGameRunning = true,
            intersections = intersections
        )

        // Start game loop
        startGameLoop()
    }

    private fun startGameLoop() {
        lastFrameMark = timeSource.markNow()

        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val currentMark = timeSource.markNow()
                val deltaTime = lastFrameMark?.let {
                    (currentMark - it).inWholeMilliseconds / 1000f
                } ?: 0.033f // Default to 33ms if no previous mark

                lastFrameMark = currentMark

                // Update game engine
                gameEngine?.update(deltaTime)

                // Update UI state from game engine
                updateUiState()

                // Target 30 FPS (33ms per frame)
                delay(33)
            }
        }
    }

    private fun updateUiState() {
        val gameState = gameEngine?.getState() ?: return
        val currentUiState = _uiState.value

        // Convert vehicles to UI state
        val vehiclesUi = gameState.vehicles.map { vehicle ->
            VehicleUiState(
                id = vehicle.id,
                position = vehicle.position,
                direction = calculateDirection(vehicle),
                isWaiting = vehicle.state == VehicleState.Waiting,
                score = vehicle.calculateScore(),
                route = vehicle.route.path.map { it.position },
                currentSegmentIndex = vehicle.route.currentIndex
            )
        }

        // Convert intersections to UI state
        val intersectionsUi = gameState.city.getAllIntersections().map { intersection ->
            IntersectionUiState(
                position = intersection.position,
                trafficLights = intersection.trafficLights.mapValues { it.value.canPass() },
                isBlocked = intersection.isBlocked,
                blockingTimeRemaining = intersection.blockingTimeRemaining,
                vehiclesOnIntersection = intersection.directionalOccupancy
            )
        }

        _uiState.value = currentUiState.copy(
            vehicles = vehiclesUi,
            intersections = intersectionsUi,
            score = gameState.totalScore,
            currentTime = gameState.gameTime.toDouble(),
            statistics = GameStatistics(
                vehiclesSpawned = gameState.vehiclesSpawned,
                vehiclesCompleted = gameState.vehiclesCompleted,
                totalCrossings = gameState.vehicles.sumOf { it.crossingsPassed },
                collisions = gameState.vehiclesCrashed
            )
        )
    }

    private fun calculateDirection(vehicle: com.citysemaphores.domain.model.Vehicle): Direction {
        val next = vehicle.route.next
        val current = vehicle.route.current

        return if (next != null) {
            val dx = next.position.x - current.position.x
            val dy = next.position.y - current.position.y

            when {
                dx > 0 -> Direction.EAST
                dx < 0 -> Direction.WEST
                dy > 0 -> Direction.SOUTH
                dy < 0 -> Direction.NORTH
                else -> Direction.EAST // Default
            }
        } else {
            Direction.EAST // Default when at destination
        }
    }

    private fun pauseGame() {
        gameLoopJob?.cancel()
        gameEngine?.pause()
        _uiState.value = _uiState.value.copy(isGameRunning = false)
    }

    private fun resumeGame() {
        gameEngine?.start()
        _uiState.value = _uiState.value.copy(isGameRunning = true)
        startGameLoop()
    }

    private fun stopGame() {
        gameLoopJob?.cancel()
        gameEngine = null
        _uiState.value = GameUiState.initial()
    }

    private fun restartGame() {
        val currentState = _uiState.value
        stopGame()
        startGame(currentState.gridWidth, currentState.gridHeight)
    }

    private fun toggleTrafficLight(intersection: GridPosition, direction: Direction) {
        gameEngine?.toggleTrafficLight(intersection, direction)
    }

    private fun setIntersectionLights(intersection: GridPosition, @Suppress("UNUSED_PARAMETER") greenDirections: Set<Direction>) {
        // Set all lights for this intersection
        Direction.entries.forEach { dir ->
            gameEngine?.toggleTrafficLight(intersection, dir)
        }
    }

    private fun toggleAllLights(intersection: GridPosition) {
        // Toggle all lights at once
        Direction.entries.forEach { dir ->
            gameEngine?.toggleTrafficLight(intersection, dir)
        }
    }

    private fun setTrafficLight(intersection: GridPosition, direction: Direction, @Suppress("UNUSED_PARAMETER") isGreen: Boolean) {
        gameEngine?.toggleTrafficLight(intersection, direction)
    }

    private fun clearEvents() {
        _uiState.value = _uiState.value.copy(recentEvents = emptyList())
    }

    private fun acknowledgeGameOver() {
        stopGame()
    }

    /**
     * Clean up resources when ViewModel is no longer needed.
     */
    fun onCleared() {
        gameLoopJob?.cancel()
        viewModelScope.cancel()
    }
}
