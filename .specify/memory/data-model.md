# Data Model: CitySemaphores

**Date**: 2026-02-02  
**Status**: Complete  
**Version**: 2.0

## Overview

This document defines the domain model for CitySemaphores, including all entities, their properties, relationships, and invariants.

## Domain Model Diagram

```
┌─────────────────────────────────────────────────────┐
│                     City                            │
│  - width: Int                                       │
│  - height: Int                                      │
│  - intersections: List<Intersection>                │
│  - graph: CityGraph                                 │
└─────────────────────────────────────────────────────┘
                      │
                      │ contains
                      ▼
┌─────────────────────────────────────────────────────┐
│                 Intersection                        │
│  - id: String                                       │
│  - gridPosition: GridPosition                       │
│  - trafficLights: Map<Direction, TrafficLight>     │
│  - status: IntersectionStatus                       │
│  - blockTimer: Float?                               │
│  - occupancy: Map<Direction, Vehicle?>              │
│  - collidedVehicles: Set<String>                    │
└─────────────────────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
┌──────────────┐          ┌─────────────────┐
│TrafficLight  │          │  VehicleSpawner │
│- state       │          │  - spawns       │
│- direction   │          │  - vehicles     │
└──────────────┘          └─────────────────┘
                                   │
                                   │ creates
                                   ▼
                          ┌─────────────────┐
                          │    Vehicle      │
                          │  - id           │
                          │  - position     │
                          │  - route        │
                          │  - waitTime     │
                          └─────────────────┘
                                   │
                                   │ follows
                                   ▼
                          ┌─────────────────┐
                          │     Route       │
                          │  - path         │
                          │  - currentIdx   │
                          └─────────────────┘
                                   │
                                   │ calculated by
                                   ▼
                          ┌─────────────────┐
                          │   CityGraph     │
                          │  - adjacency    │
                          │  - dijkstra()   │
                          └─────────────────┘
```

## Core Entities

### 1. City

The top-level entity representing the entire game world.

```kotlin
data class City(
    val width: Int,
    val height: Int,
    val intersections: Map<GridPosition, Intersection>,
    val graph: CityGraph
) {
    init {
        require(width in 10..20) { "Width must be between 10 and 20" }
        require(height in 10..20) { "Height must be between 10 and 20" }
    }
    
    fun getIntersection(position: GridPosition): Intersection? =
        intersections[position]
    
    fun getBorderIntersections(): List<Intersection> =
        intersections.values.filter { it.isBorder() }
}
```

**Properties**:
- `width`: Grid width (10-20 intersections)
- `height`: Grid height (10-20 intersections)
- `intersections`: Map of grid positions to intersections
- `graph`: Graph representation for routing

**Invariants**:
- Width and height must be between 10 and 20
- All grid positions from (0,0) to (width-1, height-1) have intersections
- Graph must be connected (all intersections reachable)

---

### 2. Intersection

A single intersection in the city grid with traffic lights.

```kotlin
data class Intersection(
    val id: String,
    val gridPosition: GridPosition,
    val trafficLights: Map<Direction, TrafficLight>,
    val status: IntersectionStatus = IntersectionStatus.Normal,
    val blockTimer: Float? = null,
    val occupancy: Map<Direction, String?> = mapOf(
        Direction.North to null,
        Direction.South to null,
        Direction.East to null,
        Direction.West to null
    ),
    val collidedVehicles: Set<String> = emptySet(),
    val baseBlockingTime: Float = 7.5f  // Configurable: 5-10 seconds
) {
    companion object {
        const val MAX_COLLISION_VEHICLES = 4
    }

    fun isBorder(): Boolean =
        gridPosition.x == 0 || gridPosition.y == 0 ||
        gridPosition.x == maxX || gridPosition.y == maxY
    
    fun canVehicleEnter(from: Direction, vehicleId: String): Boolean =
        status == IntersectionStatus.Normal &&
        trafficLights[from]?.state == TrafficLightState.Green &&
        occupancy[from] == null

    fun enterIntersection(from: Direction, vehicleId: String): Intersection =
        copy(occupancy = occupancy + (from to vehicleId))

    fun leaveIntersection(from: Direction): Intersection =
        copy(occupancy = occupancy + (from to null))

    fun isDirectionOccupied(direction: Direction): Boolean =
        occupancy[direction] != null

    fun blockWithCollision(collidingVehicleIds: Set<String>): Intersection {
        val newCollidedVehicles = collidedVehicles + collidingVehicleIds
        val collisionCount = newCollidedVehicles.size.coerceAtMost(MAX_COLLISION_VEHICLES)
        
        val additiveTime = when (collisionCount) {
            1 -> baseBlockingTime
            2 -> baseBlockingTime + 15f
            3 -> baseBlockingTime + 15f + 30f
            4 -> baseBlockingTime + 15f + 30f + 60f
            else -> baseBlockingTime
        }
        
        return copy(
            status = IntersectionStatus.Blocked,
            blockTimer = additiveTime,
            collidedVehicles = newCollidedVehicles,
            occupancy = occupancy.mapValues { null } // Clear all occupancy
        )
    }

    fun canAcceptCollision(): Boolean =
        collidedVehicles.size < MAX_COLLISION_VEHICLES
    
    fun updateBlockTimer(deltaTime: Float): Intersection =
        if (blockTimer != null && blockTimer > 0) {
            val newTimer = blockTimer - deltaTime
            if (newTimer <= 0) {
                copy(
                    status = IntersectionStatus.Normal,
                    blockTimer = null,
                    collidedVehicles = emptySet()
                )
            } else {
                copy(blockTimer = newTimer)
            }
        } else {
            this
        }
}

enum class IntersectionStatus {
    Normal,
    Blocked
}
```

**Properties**:
- `id`: Unique identifier (e.g., "intersection_5_7")
- `gridPosition`: Position in the grid
- `trafficLights`: Map of directions to traffic lights (4 independent: N, S, E, W)
- `status`: Normal or Blocked
- `blockTimer`: Remaining block time in seconds (null if not blocked)
- `occupancy`: Map tracking which vehicle (by ID) occupies each direction (null = free)
- `collidedVehicles`: Set of vehicle IDs that have collided during current blocking period
- `baseBlockingTime`: Base blocking duration (configurable 5-10s, default 7.5s)

**Invariants**:
- ID must be unique within the city
- Grid position must be within city bounds
- Must have 4 independent traffic lights (North, South, East, West)
- blockTimer is null when status is Normal
- blockTimer > 0 when status is Blocked
- Each direction can have at most one vehicle ID in occupancy map
- collidedVehicles.size <= 4 (MAX_COLLISION_VEHICLES)
- Blocking time is calculated additively:
  - 1 vehicle: 7.5 seconds (base)
  - 2 vehicles: 22.5 seconds (7.5 + 15)
  - 3 vehicles: 52.5 seconds (7.5 + 15 + 30)
  - 4 vehicles: 112.5 seconds (7.5 + 15 + 30 + 60)
- When collidedVehicles reaches 4, additional vehicles from same direction wait in queue
- occupancy is cleared when intersection becomes blocked

**Design Note**: The directional occupancy system ensures only one vehicle per direction can be on the intersection at any time. This implicitly limits collisions to a maximum of 4 vehicles (one from each direction). The additive blocking time provides escalating but predictable penalties without exponential growth.

---

### 3. TrafficLight

Controls vehicle flow through an intersection in a specific direction.

```kotlin
data class TrafficLight(
    val direction: Direction,
    val state: TrafficLightState = TrafficLightState.Red
) {
    fun toggle(): TrafficLight =
        copy(state = if (state == TrafficLightState.Red) 
            TrafficLightState.Green 
        else 
            TrafficLightState.Red)
}

enum class TrafficLightState {
    Red,
    Green
}

enum class Direction {
    North,
    South,
    East,
    West;
    
    fun opposite(): Direction = when (this) {
        North -> South
        South -> North
        East -> West
        West -> East
    }
}
```

**Properties**:
- `direction`: Direction the light controls (North/South/East/West)
- `state`: Current state (Red/Green)

**Invariants**:
- Only two states: Red or Green (no Yellow in MVP)
- Direction must be one of four cardinal directions

**Design Note**: Traffic lights are simple two-state systems. Each intersection has separate lights for different directions, allowing independent control of horizontal (East/West) and vertical (North/South) traffic flow.

---

### 4. Vehicle

A moving entity following a predetermined route.

```kotlin
data class Vehicle(
    val id: String,
    val position: Position,
    val route: Route,
    val speed: Float = 1.0f,
    val crossingsPassed: Int = 0,
    val state: VehicleState = VehicleState.Moving,
    val waitTime: Float = 0f,
    val isInCollision: Boolean = false
) {
    companion object {
        const val SAFE_FOLLOWING_DISTANCE = 2.0f
    }

    fun move(deltaTime: Float): Vehicle {
        // Calculate new position based on route and speed
        // ...
    }
    
    fun waitAtIntersection(deltaTime: Float): Vehicle =
        copy(
            state = VehicleState.Waiting,
            waitTime = waitTime + deltaTime
        )
    
    fun continueMoving(): Vehicle =
        copy(state = VehicleState.Moving)
    
    fun passCrossing(): Vehicle =
        copy(crossingsPassed = crossingsPassed + 1)
    
    fun markAsCollided(): Vehicle =
        copy(
            state = VehicleState.Crashed,
            isInCollision = true
        )

    fun calculateScore(): Int {
        val baseScore = crossingsPassed
        val distanceBonus = route.totalDistance
        val waitPenalty = waitTime.toInt()
        val finalBonus = (distanceBonus - waitPenalty).coerceAtLeast(0)
        
        return baseScore + finalBonus
    }
}

enum class VehicleState {
    Moving,
    Waiting,
    Arrived,
    Crashed
}
```

**Properties**:
- `id`: Unique identifier
- `position`: Current position (interpolated between intersections)
- `route`: Predetermined path through the city
- `speed`: Movement speed (grid units per second)
- `crossingsPassed`: Count of successfully passed intersections
- `state`: Current state (Moving/Waiting/Arrived/Crashed)
- `waitTime`: Accumulated waiting time in seconds
- `isInCollision`: Flag indicating if vehicle is involved in a collision

**Invariants**:
- ID must be unique
- Position must be on or between valid intersections
- Route must be non-empty
- Speed must be positive
- crossingsPassed >= 0
- waitTime >= 0
- isInCollision is true only when state is Crashed

**Score Calculation**:
- Base score: Number of crossings passed
- Distance bonus: Total route distance (length of path)
- Wait penalty: Each second of waiting reduces bonus by 1 point
- Final bonus: max(0, distance bonus - wait penalty)
- Total score: base score + final bonus

**Design Note**: The wait time penalty encourages efficient traffic light management while still rewarding successful crossings. Vehicles in collisions are removed after the intersection unblocks, but the player keeps all points earned before the collision.

---

### 5. Route

A path through the city calculated via Dijkstra's algorithm.

```kotlin
data class Route(
    val path: List<Intersection>,
    val currentIndex: Int = 0
) {
    init {
        require(path.isNotEmpty()) { "Route path cannot be empty" }
        require(currentIndex in path.indices) { "Current index out of bounds" }
    }
    
    val start: Intersection
        get() = path.first()
    
    val destination: Intersection
        get() = path.last()
    
    val current: Intersection
        get() = path[currentIndex]
    
    val next: Intersection?
        get() = path.getOrNull(currentIndex + 1)
    
    val totalDistance: Int
        get() = path.size - 1  // Number of road segments
    
    fun advance(): Route =
        if (currentIndex < path.size - 1)
            copy(currentIndex = currentIndex + 1)
        else
            this
    
    fun isAtDestination(): Boolean =
        currentIndex == path.size - 1
}
```

**Properties**:
- `path`: Ordered list of intersections from start to destination
- `currentIndex`: Current position in the path (0-based)
- `totalDistance`: Length of route (number of road segments = intersections - 1)

**Invariants**:
- Path must have at least 2 intersections (start and destination)
- Path must be connected (consecutive intersections are adjacent)
- Current index must be within valid range [0, path.size-1]
- totalDistance = path.size - 1

**Design Note**: The total distance is used as the bonus scoring component, representing the length of the journey the vehicle completes.

---

### 6. CityGraph

Graph representation of the city for routing calculations.

```kotlin
data class CityGraph(
    private val adjacencyList: Map<Intersection, List<Edge>>
) {
    fun findShortestPath(
        start: Intersection,
        destination: Intersection
    ): Route? {
        // Dijkstra's algorithm implementation
        val distances = mutableMapOf<Intersection, Int>()
        val previous = mutableMapOf<Intersection, Intersection>()
        val queue = PriorityQueue<Node>(compareBy { it.distance })
        
        distances[start] = 0
        queue.add(Node(start, 0))
        
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            
            if (current.intersection == destination) break
            
            adjacencyList[current.intersection]?.forEach { edge ->
                val newDistance = distances[current.intersection]!! + edge.weight
                if (newDistance < distances.getOrDefault(edge.target, Int.MAX_VALUE)) {
                    distances[edge.target] = newDistance
                    previous[edge.target] = current.intersection
                    queue.add(Node(edge.target, newDistance))
                }
            }
        }
        
        return reconstructPath(previous, start, destination)
    }
    
    private fun reconstructPath(
        previous: Map<Intersection, Intersection>,
        start: Intersection,
        destination: Intersection
    ): Route? {
        val path = mutableListOf<Intersection>()
        var current: Intersection? = destination
        
        while (current != null) {
            path.add(0, current)
            current = previous[current]
        }
        
        return if (path.first() == start) Route(path) else null
    }
    
    private data class Node(
        val intersection: Intersection,
        val distance: Int
    )
}

data class Edge(
    val target: Intersection,
    val weight: Int = 1 // All edges have equal weight in MVP
)
```

**Properties**:
- `adjacencyList`: Map of intersections to their connected edges

**Invariants**:
- Graph must be connected (all intersections reachable from any start point)
- All edge weights are positive
- No self-loops (intersection cannot connect to itself)

**Performance**:
- Dijkstra complexity: O((V + E) log V) where V = intersections, E = edges
- Target: <100ms for 20×20 grid (400 vertices, ~1600 edges)

---

### 7. GameState

The complete state of the game at any point in time.

```kotlin
data class GameState(
    val city: City,
    val vehicles: List<Vehicle>,
    val totalScore: Int = 0,
    val gameTime: Float = 0f,
    val isPlaying: Boolean = false,
    val lastSpawnTime: Float = 0f,
    val isGameOver: Boolean = false,
    val vehiclesRouted: Int = 0,
    val vehiclesRemoved: Int = 0,
    val gridlockStatus: GridlockStatus = GridlockStatus.Clear
) {
    fun addVehicle(vehicle: Vehicle): GameState =
        copy(vehicles = vehicles + vehicle)
    
    fun removeVehicle(vehicleId: String): GameState {
        val vehicle = vehicles.find { it.id == vehicleId }
        val scoreToAdd = vehicle?.calculateScore() ?: 0
        
        return copy(
            vehicles = vehicles.filter { it.id != vehicleId },
            totalScore = totalScore + scoreToAdd,
            vehiclesRemoved = vehiclesRemoved + 1
        )
    }
    
    fun updateVehicle(vehicleId: String, update: (Vehicle) -> Vehicle): GameState =
        copy(vehicles = vehicles.map { if (it.id == vehicleId) update(it) else it })
    
    fun addScore(points: Int): GameState =
        copy(totalScore = totalScore + points)
    
    fun updateTime(deltaTime: Float): GameState =
        copy(gameTime = gameTime + deltaTime)
    
    fun vehicleReachedDestination(vehicle: Vehicle): GameState {
        val finalScore = vehicle.calculateScore()
        return copy(
            vehicles = vehicles.filter { it.id != vehicle.id },
            totalScore = totalScore + finalScore,
            vehiclesRouted = vehiclesRouted + 1
        )
    }
    
    fun triggerGameOver(): GameState =
        copy(isGameOver = true, isPlaying = false)
    
    fun updateGridlockStatus(status: GridlockStatus): GameState =
        copy(gridlockStatus = status)
}
```

**Properties**:
- `city`: The city layout (immutable during gameplay)
- `vehicles`: All active vehicles
- `totalScore`: Accumulated score
- `gameTime`: Elapsed time in seconds
- `isPlaying`: Game paused/running status
- `lastSpawnTime`: Time of last vehicle spawn
- `isGameOver`: Whether the game has ended
- `vehiclesRouted`: Count of vehicles that successfully reached their destination
- `vehiclesRemoved`: Count of vehicles removed due to collisions
- `gridlockStatus`: Current gridlock detection status (Clear/Warning/GameOver)

**Invariants**:
- All vehicles have unique IDs
- Total score >= 0
- Game time >= 0
- All vehicle positions are within city bounds
- vehiclesRouted >= 0
- vehiclesRemoved >= 0

**Design Note**: Vehicles involved in collisions are removed after the intersection unblocks. The score calculation happens at removal time, ensuring all points earned before the collision are preserved.

---

## Value Objects

### GridPosition

Discrete position in the city grid.

```kotlin
data class GridPosition(
    val x: Int,
    val y: Int
) {
    operator fun plus(other: GridPosition) =
        GridPosition(x + other.x, y + other.y)
    
    fun neighbors(): List<GridPosition> = listOf(
        GridPosition(x, y - 1), // North
        GridPosition(x, y + 1), // South
        GridPosition(x + 1, y), // East
        GridPosition(x - 1, y)  // West
    )
}
```

### Position

Continuous position for smooth vehicle movement.

```kotlin
data class Position(
    val x: Float,
    val y: Float
) {
    fun lerp(target: Position, t: Float): Position =
        Position(
            x = x + (target.x - x) * t,
            y = y + (target.y - y) * t
        )
    
    fun distanceTo(other: Position): Float =
        sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
    
    fun toGridPosition(): GridPosition =
        GridPosition(x.toInt(), y.toInt())
}
```

### Vector2D

2D vector for movement and velocity.

```kotlin
data class Vector2D(
    val x: Float,
    val y: Float
) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    
    fun magnitude(): Float = sqrt(x * x + y * y)
    fun normalized(): Vector2D {
        val mag = magnitude()
        return if (mag > 0) Vector2D(x / mag, y / mag) else this
    }
}
```

## Domain Events

Events that occur during gameplay.

```kotlin
sealed interface GameEvent {
    data class VehicleSpawned(val vehicle: Vehicle) : GameEvent
    data class VehicleArrived(val vehicle: Vehicle, val score: Int) : GameEvent
    data class CrossingPassed(val vehicle: Vehicle, val intersection: Intersection) : GameEvent
    data class CollisionOccurred(
        val vehicles: Set<Vehicle>,
        val intersection: Intersection,
        val blockingTime: Float
    ) : GameEvent
    data class IntersectionBlocked(
        val intersection: Intersection,
        val duration: Float,
        val collidedVehicleIds: Set<String>
    ) : GameEvent
    data class IntersectionUnblocked(
        val intersection: Intersection,
        val removedVehicleIds: Set<String>
    ) : GameEvent
    data class VehicleRemoved(
        val vehicle: Vehicle,
        val reason: RemovalReason,
        val finalScore: Int
    ) : GameEvent
    data class TrafficLightSwitched(val intersection: Intersection, val direction: Direction) : GameEvent
    data class ScoreAwarded(val points: Int, val reason: String) : GameEvent
}

enum class RemovalReason {
    Arrived,
    Collision
}
```

## Aggregates and Boundaries

### City Aggregate

**Root**: City  
**Entities**: Intersection, TrafficLight  
**Boundary**: All operations on intersections and traffic lights go through City

**Invariants Enforced**:
- Grid size constraints
- All positions have intersections
- Graph connectivity
- Intersection occupancy rules (max 1 vehicle per direction)

### Vehicle Aggregate

**Root**: Vehicle  
**Value Objects**: Route, Position  
**Boundary**: Vehicle owns its route and position

**Invariants Enforced**:
- Route validity
- Position on route
- Wait time tracking
- Score calculation including bonus and penalties

## Persistence Model (Optional - Future)

```kotlin
// For future save/load functionality
@Serializable
data class SavedGameState(
    val cityWidth: Int,
    val cityHeight: Int,
    val vehicles: List<SavedVehicle>,
    val totalScore: Int,
    val gameTime: Float,
    val vehiclesRouted: Int,
    val vehiclesRemoved: Int
)

@Serializable
data class SavedVehicle(
    val id: String,
    val positionX: Float,
    val positionY: Float,
    val routePath: List<GridPosition>,
    val currentRouteIndex: Int,
    val crossingsPassed: Int,
    val waitTime: Float,
    val isInCollision: Boolean
)
```

## Model Validation Rules

### City Validation
- ✅ Width and height between 10 and 20
- ✅ All grid positions populated
- ✅ Graph is connected
- ✅ Border intersections exist on all sides

### Intersection Validation
- ✅ Each direction has at most one occupying vehicle
- ✅ Blocked intersections have no occupancy
- ✅ Collision count <= 4
- ✅ Blocking time matches collision count formula

### Vehicle Validation
- ✅ Unique ID
- ✅ Position within city bounds
- ✅ Route starts and ends at border intersections
- ✅ Speed > 0
- ✅ crossingsPassed >= 0
- ✅ waitTime >= 0
- ✅ Crashed vehicles have isInCollision = true

### Route Validation
- ✅ Path has at least 2 intersections
- ✅ Path is connected (consecutive intersections adjacent)
- ✅ Start is a border intersection
- ✅ Destination is a border intersection
- ✅ totalDistance = path.size - 1

### GameState Validation
- ✅ All vehicle IDs unique
- ✅ Total score >= 0
- ✅ Game time >= 0
- ✅ All vehicles within city bounds
- ✅ vehiclesRouted >= 0
- ✅ vehiclesRemoved >= 0

---

## Service Layer Entities

While the above entities represent the domain model, the following service/manager classes coordinate game behavior:

### TrafficManager

Manages vehicle queues and following behavior to prevent collisions outside of intersections.

```kotlin
class TrafficManager {
    fun canVehicleEnterIntersection(
        vehicle: Vehicle,
        intersection: Intersection,
        fromDirection: Direction
    ): Boolean {
        return intersection.canVehicleEnter(fromDirection, vehicle.id)
    }

    fun updateVehicleFollowing(
        vehicles: List<Vehicle>,
        city: City
    ): List<Vehicle> {
        // For each road segment, identify vehicles and establish following relationships
        // Returns updated vehicle list with following behavior applied
    }
    
    fun formQueue(
        vehicles: List<Vehicle>,
        intersection: Intersection,
        direction: Direction
    ): List<Vehicle> {
        // Organizes vehicles from specific direction waiting at intersection
        // Only allows first vehicle to enter when intersection slot is free
        // Returns updated vehicle list
    }
    
    fun calculateSafeDistance(
        follower: Vehicle,
        leader: Vehicle
    ): Float {
        return follower.position.distanceTo(leader.position)
    }
    
    fun shouldSlowDown(distance: Float): Boolean {
        return distance < Vehicle.SAFE_FOLLOWING_DISTANCE * 2
    }
}
```

**Responsibilities**:
- Track which vehicles are on the same road segment
- Establish following relationships (vehicleAhead)
- Calculate safe distances and adjust speeds
- Form and manage queues at intersections per direction
- Enforce one-vehicle-per-direction intersection occupancy rule
- Ensure no collisions occur outside of intersections

**Design Note**: The directional queuing ensures that vehicles from the same direction wait behind each other, preventing more than one vehicle per direction from entering an intersection simultaneously.

---

### CollisionDetector

Detects and handles collision events at intersections.

```kotlin
class CollisionDetector {
    fun detectCollisions(
        intersection: Intersection,
        vehicles: List<Vehicle>
    ): CollisionResult {
        val vehiclesOnIntersection = vehicles.filter { vehicle ->
            vehicle.position.toGridPosition() == intersection.gridPosition &&
            vehicle.state == VehicleState.Moving
        }

        if (vehiclesOnIntersection.size >= 2) {
            return CollisionResult.Collision(vehiclesOnIntersection.toSet())
        }

        return CollisionResult.NoCollision
    }

    fun handleCollision(
        intersection: Intersection,
        collidingVehicles: Set<Vehicle>
    ): IntersectionUpdate {
        val vehicleIds = collidingVehicles.map { it.id }.toSet()
        val updatedIntersection = intersection.blockWithCollision(vehicleIds)
        
        return IntersectionUpdate(
            intersection = updatedIntersection,
            vehiclesToMark = vehicleIds,
            blockingTime = updatedIntersection.blockTimer ?: 0f
        )
    }
}

sealed interface CollisionResult {
    object NoCollision : CollisionResult
    data class Collision(val vehicles: Set<Vehicle>) : CollisionResult
}

data class IntersectionUpdate(
    val intersection: Intersection,
    val vehiclesToMark: Set<String>,
    val blockingTime: Float
)
```

**Responsibilities**:
- Detect when multiple vehicles occupy the same intersection
- Calculate blocking time based on collision count
- Mark vehicles as crashed
- Update intersection state

**Design Note**: The occupancy system naturally limits collisions to 4 vehicles maximum (one per direction), simplifying collision detection logic.

---

### GameOverDetector

Monitors the city for gridlock conditions and triggers game over when appropriate.

```kotlin
class GameOverDetector {
    companion object {
        const val GRIDLOCK_GRACE_PERIOD = 5f  // seconds
    }
    
    private var gridlockTimer: Float? = null
    
    fun checkGridlock(
        city: City,
        vehicles: List<Vehicle>
    ): GridlockStatus {
        val borderIntersections = city.getBorderIntersections()
        val allBlocked = borderIntersections.all { intersection ->
            isEntryPointBlocked(intersection, vehicles)
        }
        
        return when {
            allBlocked && gridlockTimer == null -> {
                gridlockTimer = GRIDLOCK_GRACE_PERIOD
                GridlockStatus.Warning(GRIDLOCK_GRACE_PERIOD)
            }
            allBlocked && gridlockTimer != null && gridlockTimer!! > 0 -> {
                GridlockStatus.Warning(gridlockTimer!!)
            }
            allBlocked && gridlockTimer != null && gridlockTimer!! <= 0 -> {
                GridlockStatus.GameOver
            }
            else -> {
                gridlockTimer = null
                GridlockStatus.Clear
            }
        }
    }
    
    fun updateGridlockTimer(deltaTime: Float) {
        gridlockTimer = gridlockTimer?.let { it - deltaTime }
    }
    
    private fun isEntryPointBlocked(
        intersection: Intersection,
        vehicles: List<Vehicle>
    ): Boolean {
        // Check if all directions of this border intersection are occupied or blocked
        return intersection.status == IntersectionStatus.Blocked ||
               intersection.occupancy.values.all { it != null }
    }
}

sealed interface GridlockStatus {
    object Clear : GridlockStatus
    data class Warning(val remainingTime: Float) : GridlockStatus
    object GameOver : GridlockStatus
}
```

**Responsibilities**:
- Monitor all city border entry points
- Detect when all entry points are blocked
- Manage grace period timer (5 seconds)
- Trigger game over condition
- Provide warning status to UI

**Design Note**: The 5-second grace period gives players a chance to recover from near-gridlock situations, creating dramatic "last-second save" moments while preventing premature game overs.

---

## Performance Considerations

### Memory Footprint

For 20×20 grid with 50 vehicles:
- City: ~400 intersections × 250 bytes = 100 KB (including occupancy maps)
- Vehicles: 50 × 180 bytes = 9 KB (including waitTime)
- Routes: 50 × (avg 20 intersections) × 8 bytes = 8 KB
- **Total**: ~120 KB (negligible)

### Immutability

All entities are immutable data classes, providing:
- Thread safety
- Easy testing
- Predictable state updates
- Efficient change detection for Compose

### Object Creation

Potential concern: Frequent copying of GameState
**Mitigation**: Structural sharing via persistent data structures (if needed)

---

**Data Model Status**: ✅ Complete (Updated v2.0)
**Next**: Architecture design and component contracts
