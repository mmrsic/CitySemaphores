package com.citysemaphores.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.citysemaphores.domain.model.Direction
import com.citysemaphores.domain.model.GridPosition
import com.citysemaphores.ui.components.IntersectionView
import com.citysemaphores.ui.theme.RoadGray
import com.citysemaphores.viewmodel.GameIntent
import com.citysemaphores.viewmodel.GameUiState
import com.citysemaphores.viewmodel.GameViewModel

/**
 * Main game screen showing the city grid with intersections and traffic lights.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with title and controls
        GameHeader(
            isRunning = uiState.isGameRunning,
            score = uiState.score,
            onStartClick = { viewModel.handleIntent(GameIntent.StartGame(10, 10)) },
            onPauseClick = { viewModel.handleIntent(GameIntent.PauseGame) },
            onResumeClick = { viewModel.handleIntent(GameIntent.ResumeGame) },
            onStopClick = { viewModel.handleIntent(GameIntent.StopGame) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // City grid
        CityGridView(
            uiState = uiState,
            onTrafficLightClick = { intersection, direction ->
                viewModel.handleIntent(
                    GameIntent.ToggleTrafficLight(
                        intersection = intersection,
                        direction = direction
                    )
                )
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics panel
        StatisticsPanel(uiState = uiState)
    }
}

@Composable
private fun GameHeader(
    isRunning: Boolean,
    score: Int,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "City Semaphores",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isRunning) {
                    Button(onClick = onStartClick) {
                        Text("Start")
                    }
                } else {
                    Button(onClick = onPauseClick) {
                        Text("Pause")
                    }
                }

                if (isRunning) {
                    Button(onClick = onStopClick) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
private fun CityGridView(
    uiState: GameUiState,
    onTrafficLightClick: (GridPosition, Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellSize = 80f // Size of each grid cell in dp

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isGameRunning || uiState.intersections.isNotEmpty()) {
                Box {
                    // Draw the grid (bottom layer)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        for (y in 0 until uiState.gridHeight) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                for (x in 0 until uiState.gridWidth) {
                                    val position = GridPosition(x, y)
                                    val intersection = uiState.intersections.find { it.position == position }

                                    if (intersection != null) {
                                        IntersectionView(
                                            state = intersection,
                                            cellSize = cellSize,
                                            onTrafficLightClick = { direction ->
                                                onTrafficLightClick(position, direction)
                                            }
                                        )
                                    } else {
                                        // Road segment
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize.dp)
                                                .background(RoadGray)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Draw vehicles (top layer)
                    uiState.vehicles.forEach { vehicle ->
                        VehicleOverlay(
                            vehicle = vehicle,
                            cellSize = cellSize
                        )
                    }
                }
            } else {
                // Welcome message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Welcome to City Semaphores!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Click 'Start' to begin the game.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Prototype: Vehicle Spawning & Routing (US2)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleOverlay(
    vehicle: com.citysemaphores.viewmodel.VehicleUiState,
    cellSize: Float
) {
    val x = vehicle.position.x.toFloat() * cellSize
    val y = vehicle.position.y.toFloat() * cellSize

    Box(
        modifier = Modifier
            .offset(x = x.dp, y = y.dp)
            .size((cellSize * 0.5f).dp)
    ) {
        com.citysemaphores.ui.components.VehicleView(
            vehicle = com.citysemaphores.domain.model.Vehicle(
                id = vehicle.id,
                position = vehicle.position,
                route = com.citysemaphores.domain.model.Route(vehicle.route.map {
                    com.citysemaphores.domain.model.Intersection(it)
                }, vehicle.currentSegmentIndex),
                speed = 2f,
                state = if (vehicle.isWaiting)
                    com.citysemaphores.domain.model.VehicleState.Waiting
                else
                    com.citysemaphores.domain.model.VehicleState.Moving
            ),
            gridSize = cellSize
        )
    }
}

@Composable
private fun StatisticsPanel(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Vehicles Spawned", uiState.statistics.vehiclesSpawned.toString())
                StatItem("Completed", uiState.statistics.vehiclesCompleted.toString())
                StatItem("Collisions", uiState.statistics.collisions.toString())
                StatItem("Total Crossings", uiState.statistics.totalCrossings.toString())
            }

            if (uiState.isGameRunning) {
                val timeText = "${(uiState.currentTime * 10).toInt() / 10.0}"
                Text(
                    text = "Game Time: ${timeText}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
