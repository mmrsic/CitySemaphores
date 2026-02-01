# Data Model: CitySemaphores

**Date**: 2026-02-01  
**Status**: Complete  
**Version**: 1.0

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
                          │  - score        │
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
    val collisionVehicleCount: Int = 0,
    val baseBlockingTime: Float = 7.5f  // Configurable: 5-10 seconds
) {
    fun isBorder(): Boolean =
        gridPosition.x == 0 || gridPosition.y == 0 ||
        gridPosition.x == maxX || gridPosition.y == maxY
    
    fun canVehiclePass(from: Direction): Boolean =
        status == IntersectionStatus.Normal &&
        trafficLights[from]?.state == TrafficLightState.Green
    
    fun blockWithCollision(): Intersection {
        val newCount = min(collisionVehicleCount + 1, MAX_COLLISION_VEHICLES)
        val exponentialTime = baseBlockingTime.pow(newCount - 1)
        return copy(
            status = IntersectionStatus.Blocked, 
            blockTimer = exponentialTime,
            collisionVehicleCount = newCount
        )
    }
    
    fun canAcceptCollision(): Boolean =
        collisionVehicleCount < MAX_COLLISION_VEHICLES
    
    fun block(duration: Float): Intersection =
        copy(status = IntersectionStatus.Blocked, blockTimer = duration)
    
    fun updateBlockTimer(deltaTime: Float): Intersection =
        if (blockTimer != null && blockTimer > 0) {
            val newTimer = blockTimer - deltaTime
            if (newTimer <= 0) {
                copy(status = IntersectionStatus.Normal, blockTimer = null)
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
- `collisionVehicleCount`: Number of vehicles that have collided at this intersection during current blocking period

**Invariants**:
- ID must be unique within the city
- Grid position must be within city bounds
- Must have 4 independent traffic lights (North, South, East, West)
- blockTimer is null when status is Normal
- blockTimer > 0 when status is Blocked
- collisionVehicleCount >= 0 and <= 4 (MAX_COLLISION_VEHICLES)
- Blocking time is calculated additively:
  - 2 vehicles: 20 seconds
  - 3 vehicles: 50 seconds (20 + 30)
  - 4 vehicles: 100 seconds (20 + 30 + 50)
- When collisionVehicleCount reaches 4, additional vehicles wait in queue before intersection

**Design Note**: The additive blocking time system provides escalating but predictable penalties. Each additional collision significantly increases blocking time, but remains manageable compared to exponential growth. The 4-vehicle cap creates a maximum penalty of 100 seconds while additional vehicles form queues, creating strategic gameplay pressure without excessive punishment.

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
    val state: VehicleState = VehicleState.Moving
) {
    fun move(deltaTime: Float): Vehicle {
        // Calculate new position based on route and speed
        // ...
    }
    
    fun waitAtIntersection(): Vehicle =
        copy(state = VehicleState.Waiting)
    
    fun continueMoving(): Vehicle =
        copy(state = VehicleState.Moving)
    
    fun passCrossing(): Vehicle =
        copy(crossingsPassed = crossingsPassed + 1)
    
    fun calculateScore(): Int =
        crossingsPassed * 2 // Doubled at destination
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

**Invariants**:
- ID must be unique
- Position must be on or between valid intersections
- Route must be non-empty
- Speed must be positive
- currentSpeed must be >= 0 and <= speed
- crossingsPassed >= 0
- If vehicleAhead is not null, then vehicle is in Following state
- queuePosition >= 0
- Vehicles never collide outside of intersections (enforced by following behavior)

**Design Note**: The following behavior ensures that vehicles automatically maintain safe distances on road segments, preventing collisions outside of intersections. This makes intersections the only location where player mistakes (incorrect traffic light management) can cause accidents.

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

**Invariants**:
- Path must have at least 2 intersections (start and destination)
- Path must be connected (consecutive intersections are adjacent)
- Current index must be within valid range [0, path.size-1]

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
    val vehiclesRouted: Int = 0,  // Total vehicles that reached destination
    val gridlockStatus: GridlockStatus = GridlockStatus.Clear
) {
    fun addVehicle(vehicle: Vehicle): GameState =
        copy(vehicles = vehicles + vehicle)
    
    fun removeVehicle(vehicleId: String): GameState =
        copy(vehicles = vehicles.filter { it.id != vehicleId })
    
    fun updateVehicle(vehicleId: String, update: (Vehicle) -> Vehicle): GameState =
        copy(vehicles = vehicles.map { if (it.id == vehicleId) update(it) else it })
    
    fun addScore(points: Int): GameState =
        copy(totalScore = totalScore + points)
    
    fun updateTime(deltaTime: Float): GameState =
        copy(gameTime = gameTime + deltaTime)
    
    fun vehicleReachedDestination(): GameState =
        copy(vehiclesRouted = vehiclesRouted + 1)
    
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
- `gridlockStatus`: Current gridlock detection status (Clear/Warning/GameOver)
        copy(totalScore = totalScore + points)
    
    fun updateTime(deltaTime: Float): GameState =
        copy(gameTime = gameTime + deltaTime)
}
```

**Properties**:
- `city`: The city layout (immutable during gameplay)
- `vehicles`: All active vehicles
- `totalScore`: Accumulated score
- `gameTime`: Elapsed time in seconds
- `isPlaying`: Game paused/running status
- `lastSpawnTime`: Time of last vehicle spawn

**Invariants**:
- All vehicles have unique IDs
- Total score >= 0
- Game time >= 0
- All vehicle positions are within city bounds

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
    data class CollisionOccurred(val vehicle1: Vehicle, val vehicle2: Vehicle, val intersection: Intersection) : GameEvent
    data class IntersectionBlocked(val intersection: Intersection, val duration: Float) : GameEvent
    data class IntersectionUnblocked(val intersection: Intersection) : GameEvent
    data class TrafficLightSwitched(val intersection: Intersection, val direction: Direction) : GameEvent
    data class ScoreAwarded(val points: Int, val reason: String) : GameEvent
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

### Vehicle Aggregate

**Root**: Vehicle  
**Value Objects**: Route, Position  
**Boundary**: Vehicle owns its route and position

**Invariants Enforced**:
- Route validity
- Position on route
- Score calculation

## Persistence Model (Optional - Future)

```kotlin
// For future save/load functionality
@Serializable
data class SavedGameState(
    val cityWidth: Int,
    val cityHeight: Int,
    val vehicles: List<SavedVehicle>,
    val totalScore: Int,
    val gameTime: Float
)

@Serializable
data class SavedVehicle(
    val id: String,
    val positionX: Float,
    val positionY: Float,
    val routePath: List<GridPosition>,
    val currentRouteIndex: Int,
    val crossingsPassed: Int
)
```

## Model Validation Rules

### City Validation
- ✅ Width and height between 10 and 20
- ✅ All grid positions populated
- ✅ Graph is connected
- ✅ Border intersections exist on all sides

### Vehicle Validation
- ✅ Unique ID
- ✅ Position within city bounds
- ✅ Route starts and ends at border intersections
- ✅ Speed > 0
- ✅ crossingsPassed >= 0

### Route Validation
- ✅ Path has at least 2 intersections
- ✅ Path is connected (consecutive intersections adjacent)
- ✅ Start is a border intersection
- ✅ Destination is a border intersection

### GameState Validation
- ✅ All vehicle IDs unique
- ✅ Total score >= 0
- ✅ Game time >= 0
- ✅ All vehicles within city bounds

---

## Service Layer Entities

While the above entities represent the domain model, the following service/manager classes coordinate game behavior:

### TrafficManager

Manages vehicle queues and following behavior to prevent collisions outside of intersections.

```kotlin
class TrafficManager {
    fun updateVehicleFollowing(
        vehicles: List<Vehicle>,
        city: City
    ): List<Vehicle> {
        // For each road segment, identify vehicles and establish following relationships
        // Returns updated vehicle list with following behavior applied
    }
    
    fun formQueue(
        vehicles: List<Vehicle>,
        intersection: Intersection
    ): List<Vehicle> {
        // Organizes vehicles waiting at an intersection into a queue
        // Assigns queue positions
        // Returns updated vehicle list
    }
    
    fun calculateSafeDistance(
        follower: Vehicle,
        leader: Vehicle
    ): Float {
        // Returns current distance between vehicles
    }
    
    fun shouldSlowDown(distance: Float): Boolean {
        // Determines if follower should reduce speed
        return distance < Vehicle.SAFE_FOLLOWING_DISTANCE * 2
    }
}
```

**Responsibilities**:
- Track which vehicles are on the same road segment
- Establish following relationships (vehicleAhead)
- Calculate safe distances and adjust speeds
- Form and manage queues at intersections
- Ensure no collisions occur outside of intersections

**Design Note**: This manager is crucial for the gameplay mechanic that "vehicles never collide outside of intersections." It creates strategic pressure through traffic jams rather than additional collision penalties.

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
        // Check if vehicles are queued at this border intersection
        // preventing new spawns
        return vehicles.any { vehicle ->
            vehicle.position.toGridPosition() == intersection.gridPosition &&
            vehicle.state == VehicleState.Waiting
        }
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
- City: ~400 intersections × 200 bytes = 80 KB
- Vehicles: 50 × 150 bytes = 7.5 KB
- Routes: 50 × (avg 20 intersections) × 8 bytes = 8 KB
- **Total**: ~100 KB (negligible)

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

**Data Model Status**: ✅ Complete  
**Next**: Architecture design and component contracts
