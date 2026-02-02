# Feature Specification: CitySemaphores - Traffic Light Simulation Game

**Feature Branch**: `main`  
**Created**: 2026-02-01  
**Status**: Draft  
**Input**: User description: "A 2D game where traffic lights in a city are controlled manually to keep traffic flowing without causing accidents"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Basic Traffic Light Control (Priority: P1)

As a player, I want to manually control traffic lights at intersections so that I can control traffic flow and prevent accidents.

**Why this priority**: This is the core functionality of the game. Without manual traffic light control, there is no game mechanic.

**Independent Test**: Can be fully tested by creating a single intersection with traffic lights, switching them via mouse click/touch, and observing whether vehicles respond accordingly (stop at red, go at green).

**Acceptance Scenarios**:

1. **Given** an intersection with a green light in the east-west direction, **When** the player clicks/taps the light, **Then** the light switches to red and vehicles stop
2. **Given** waiting vehicles in front of a red light, **When** the player switches the light to green, **Then** the vehicles continue moving
3. **Given** an active intersection, **When** the player hovers over the light (desktop) or touches it (touch), **Then** the light is highlighted/displayed as interactive

---

### User Story 2 - Vehicle Spawning and Routing (Priority: P1)

As a player, I want vehicles to appear at city borders and automatically take optimal routes through the city, so there is a continuous challenge.

**Why this priority**: Without vehicles, there is nothing to control. This is equally important with traffic light control for a functioning MVP.

**Independent Test**: Can be tested by creating a city grid with multiple intersections and observing whether vehicles spawn at edges, calculate the shortest path (Dijkstra), and follow it.

**Acceptance Scenarios**:

1. **Given** an initialized city grid, **When** the game starts, **Then** vehicles appear at random city borders
2. **Given** a spawned vehicle, **When** its route is calculated, **Then** it follows the shortest path via Dijkstra to the destination
3. **Given** a vehicle on a route, **When** it reaches a green light, **Then** it continues to the next intersection
4. **Given** a vehicle at its destination, **When** it reaches the last intersection, **Then** it disappears from the map

---

### User Story 3 - Collision Detection and Blocking (Priority: P1)

As a player, I want to receive immediate feedback when I make a mistake (accident), so I can learn from my errors.

**Why this priority**: This is the "fail state" of the game and essential for game design. Without consequences, traffic light control has no meaning.

**Independent Test**: Can be tested by intentionally causing vehicles to collide at an intersection and checking whether the intersection is blocked with additively increasing block time as more vehicles collide (maximum 4 vehicles), and whether it is visually marked. Additional vehicles should form queues before the intersection.

**Acceptance Scenarios**:

1. **Given** two vehicles approaching the same intersection from different directions, **When** both enter the intersection simultaneously with green lights in their respective directions, **Then** a collision is detected
2. **Given** a detected collision with 1 vehicle, **When** the accident occurs, **Then** the intersection is blocked for 7.5 seconds (base blocking time)
3. **Given** a detected collision with 2 vehicles, **When** the accident occurs, **Then** the intersection is blocked for 22.5 seconds (7.5 + 15)
4. **Given** an already blocked intersection with 2 collided vehicles, **When** a third vehicle collides at the same intersection, **Then** the blocking time is reset to 52.5 seconds (7.5 + 15 + 30) and starts counting from this new value
5. **Given** an already blocked intersection with 3 collided vehicles (blocking time 52.5s), **When** a fourth vehicle collides, **Then** the blocking time is reset to 112.5 seconds (7.5 + 15 + 30 + 60) and the collision counter is capped at 4
6. **Given** an intersection with 4 collided vehicles (maximum), **When** additional vehicles approach the blocked intersection, **Then** they wait in queue before the intersection and do not enter or add to the collision
7. **Given** a blocked intersection, **When** the blocking time is running, **Then** all lights are displayed as red and new vehicles cannot pass
8. **Given** a blocked intersection, **When** the blocking time expires, **Then** the intersection is released, collided vehicles are removed (player keeps earned points), and the collision counter is reset to 0

---

### User Story 4 - Scoring System (Priority: P2)

As a player, I want to collect points for successfully managed traffic so I can measure my performance and stay motivated.

**Why this priority**: Adds progression and motivation, but is not essential for basic functionality.

**Independent Test**: Can be tested by steering a vehicle through multiple intersections without accidents and checking the score (+1 per intersection, bonus = route distance minus wait time at destination).

**Acceptance Scenarios**:

1. **Given** a vehicle passes an intersection without accident, **When** it leaves the intersection, **Then** the player receives +1 point (base score)
2. **Given** a vehicle with 5 passed intersections and route distance of 10 that did not wait, **When** it reaches its destination, **Then** the player receives base score (5) plus full distance bonus (10) for total of 15 points
3. **Given** a vehicle with 5 passed intersections, route distance of 10, and 3 seconds of waiting, **When** it reaches its destination, **Then** the player receives base score (5) plus reduced bonus (10-3=7) for total of 12 points
4. **Given** a vehicle with accumulated waiting time exceeding route distance, **When** it reaches its destination, **Then** the bonus is 0 but base score (crossings) is still awarded
5. **Given** a vehicle involved in a collision, **When** the intersection unblocks, **Then** the vehicle is removed and player keeps all points earned before collision (crossings passed)
6. **Given** a running game, **When** points are awarded, **Then** the current total score is displayed in the UI

---

### User Story 5 - Visual Representation and UI (Priority: P2)

As a player, I want to see a visually impressive, clear, and organized 2D top-down view of the city with modern graphics and smooth animations, so I can effectively manage traffic and enjoy the game experience.

**Why this priority**: Important for player engagement and retention. While basic functionality can be tested with rudimentary graphics, a visually impressive presentation significantly enhances the game experience and marketability.

**Independent Test**: Can be visually tested by inspecting the rendered city, traffic lights, vehicles, animations, visual effects, and UI elements for visual quality and appeal.

**Acceptance Scenarios**:

1. **Given** a started game, **When** the city is rendered, **Then** streets, intersections, and traffic lights are clearly visible with modern, polished graphics
2. **Given** a traffic light in "Red" state, **When** rendered, **Then** it is displayed with vibrant red color and optional glow/emission effect
3. **Given** a traffic light in "Green" state, **When** rendered, **Then** it is displayed with vibrant green color and optional glow/emission effect
4. **Given** a traffic light switching state, **When** the player toggles it, **Then** a smooth transition animation is displayed
5. **Given** vehicles on the map, **When** they move, **Then** their movement is smooth, fluid, and uses interpolation for natural motion
6. **Given** a collision occurs, **When** vehicles crash, **Then** a visually striking effect is displayed (particle effects, visual feedback)
7. **Given** an intersection is blocked, **When** the blocking occurs, **Then** visual indicators (warning symbols, color overlay, pulsing effects) clearly communicate the state
8. **Given** the active game, **When** the player plays, **Then** score and stats are displayed with clean, modern UI design
9. **Given** a vehicle reaches its destination, **When** it arrives, **Then** a satisfying visual celebration effect is shown (sparkles, fade-out, score popup)

---

### Edge Cases

- **What happens when a vehicle waits at a red light and the light is never switched to green?** → Vehicle waits indefinitely (player responsibility)
- **How does the system behave when multiple lights are clicked/switched simultaneously?** → Click events are queued and processed sequentially to prevent race conditions and ensure consistent state transitions
- **What happens when all 4 lights show green and vehicles from multiple directions enter?** → Collision detection activates; accidents can occur (intentional game design to increase difficulty)
- **What happens when all 4 lights show red simultaneously?** → All approaching vehicles stop; no movement until at least one light is switched to green
- **What happens when a new vehicle reaches an intersection during a blocking period?** → Vehicle waits in front of the intersection until the block is lifted
- **What happens when multiple collisions occur at the same intersection during an active blocking period?** → Each additional colliding vehicle additively increases the blocking time: 1 vehicle = 7.5s, 2 vehicles = 22.5s (7.5+15), 3 vehicles = 52.5s (7.5+15+30), 4 vehicles = 112.5s (7.5+15+30+60). **Maximum 4 vehicles can collide** (one per direction). The timer resets to the new additive value. Additional vehicles from same direction wait in queue before the intersection and do not add to the collision count.
- **How do vehicles behave when another vehicle is directly ahead on the same road segment?** → Vehicles automatically slow down and follow at a safe distance. **No collisions occur outside of intersections** - only at intersections when traffic lights are incorrectly managed.
- **What happens when a long queue forms due to a red light or blocked intersection?** → Vehicles stack up in a queue/traffic jam, maintaining safe distances. This creates strategic pressure on the player to manage traffic flow efficiently.
- **How does a vehicle determine which traffic light to check?** → Each vehicle checks only its specific directional traffic light based on its approach direction (e.g., a vehicle approaching from the North checks the North light)
- **Can a vehicle drive "backwards" or change its route?** → No, route is fixed at spawn and immutable
- **What happens when no free route to the destination exists?** → Should be avoided in grid design; alternative: vehicle is not spawned
- **How are crossing routes prioritized in Dijkstra?** → All edges have equal weight (1 unit), no prioritization
- **Can dangerous light combinations (e.g., N+S+E green, W red) be created?** → Yes, all combinations are allowed; player must manage risk

## Clarifications

### Session 2026-02-01

- Q: How many independent traffic lights should exist at each intersection? → A: C (4 fully independent lights - North, South, East, West can each be Red or Green independently)
- Q: When a vehicle approaches an intersection with 4 independent lights, which light(s) determine if it can proceed? → A: B (Vehicle checks its specific directional light)
- Q: If multiple vehicles collide at same intersection during blocking period, how is blocking time calculated? → A: CUSTOM - Blocking time increases additively with each colliding vehicle using base time 7.5s: 1 vehicle = 7.5s, 2 vehicles = 22.5s (7.5+15), 3 vehicles = 52.5s (7.5+15+30), 4 vehicles = 112.5s (7.5+15+30+60). Timer resets to new value when additional collision occurs. **MAXIMUM 4 vehicles can collide at an intersection** - additional vehicles wait in queue before the intersection.
- Q: What is the target frame rate for visual smoothness and animation quality? → A: B (30 FPS minimum requirement, 60 FPS desirable if achievable without significant extra effort)
- Q: When multiple traffic lights are clicked/switched simultaneously, how should the system process them? → A: B (Clicks are queued and processed sequentially)
- Q: How do vehicles behave outside of intersections when another vehicle is ahead? → A: CUSTOM - Vehicles NEVER collide outside of intersections. They form queues/traffic jams by following the vehicle ahead at a safe distance.
- Q: What is the game over condition? → A: CUSTOM - The game ends when no new vehicles can be spawned because all city border entry points are blocked by traffic jams (gridlock condition). A grace period of 5 seconds is provided before triggering Game Over.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST create a grid-based city with rectangular intersections
- **FR-002**: System MUST provide 4 fully independent traffic lights (North, South, East, West) at each intersection, each with two states (Red/Green)
- **FR-003**: System MUST enable independent control of all 4 directional traffic lights at each intersection, with no automatic synchronization between lights
- **FR-004**: Player MUST be able to manually switch each of the 4 directional traffic lights via mouse click (Desktop/Web) or touch (Mobile)
- **FR-004a**: System MUST queue player input events (traffic light switches) and process them sequentially to prevent race conditions and ensure consistent state transitions
- **FR-005**: System MUST spawn vehicles at city borders
- **FR-006**: System MUST calculate a route for each vehicle at spawn using Dijkstra's algorithm
- **FR-007**: Vehicles MUST check their specific directional traffic light (based on approach direction) and stop at red lights
- **FR-008**: Vehicles MUST continue/start moving at green lights
- **FR-008a**: Vehicles MUST form queues when waiting at red lights or blocked intersections, maintaining safe distances between each other
- **FR-008b**: Vehicles MUST follow other vehicles on the same road segment at a safe distance and automatically adjust speed to prevent collisions outside of intersections
- **FR-009**: System MUST detect collisions between vehicles at intersections
- **FR-009a**: System MUST track the number of vehicles that collide at each blocked intersection (maximum 4 vehicles) and calculate blocking time additively: 1 vehicle = 7.5s, 2 vehicles = 22.5s (7.5+15), 3 vehicles = 52.5s (7.5+15+30), 4 vehicles = 112.5s (7.5+15+30+60)
- **FR-009b**: System MUST reset the blocking timer to the new additive value when an additional vehicle collides at an already blocked intersection (up to the 4-vehicle maximum)
- **FR-009c**: System MUST prevent collisions outside of intersections by implementing vehicle following behavior with safe distance maintenance
- **FR-010**: System MUST block the affected intersection with additively increasing duration based on collision count: 1 vehicle = 7.5s, 2 vehicles = 22.5s, 3 vehicles = 52.5s, 4 vehicles = 112.5s. Maximum blocking time is 112.5 seconds (4 vehicles cap).
- **FR-011**: System MUST visually mark blocked intersections
- **FR-012**: System MUST award +1 point when a vehicle passes an intersection without accident
- **FR-013**: System MUST calculate bonus points for vehicles reaching destination based on route distance traveled minus accumulated waiting time (bonus = max(0, distance - waitSeconds)), added to base score (crossings passed)
- **FR-013a**: System MUST track waiting time for each vehicle and reduce bonus points by 1 for each second of waiting
- **FR-013b**: Vehicles involved in collisions MUST be removed after intersection unblocks, with player keeping all points earned before collision
- **FR-014**: System MUST display the total score in the UI
- **FR-015**: System MUST render a 2D top-down view of the city
- **FR-016**: System MUST display traffic light states in correct colors (Red/Green)
- **FR-017**: System MUST provide smooth transition animations when traffic lights change state
- **FR-018**: System MUST use interpolation for vehicle movement to ensure fluid, natural motion
- **FR-019**: System MUST display visually striking effects for collisions (particle effects, visual feedback)
- **FR-020**: System MUST provide visual indicators for blocked intersections (warning symbols, color overlays, or pulsing effects)
- **FR-021**: System MUST display celebration effects when vehicles reach their destination (sparkles, fade-out, score popup)
- **FR-022**: System MUST render traffic lights with glow/emission effects for enhanced visibility
- **FR-023**: System MUST use modern, polished graphics with clean lines and vibrant colors
- **FR-024**: System MUST provide a visually cohesive color scheme and design language across all elements

### Key Entities

- **City**: Represents the entire road network, manages all intersections and the graph network for routing
- **Intersection**: A single intersection with grid position, 4 independent traffic lights (N/S/E/W), status (normal/blocked), blocking timer, directional occupancy map (max 1 vehicle per direction), set of collided vehicle IDs (capped at 4), base blocking time (configurable 5-10s, default 7.5s)
- **TrafficLight**: Fully independent directional traffic light control with state (Red/Green), direction (North/South/East/West), switching method. Each light operates independently with no automatic synchronization
- **Vehicle**: Vehicle with current position, route (list of Intersections), movement speed, point counter for passed intersections, accumulated wait time, approach direction for determining which directional light to check, following behavior (maintains safe distance from vehicle ahead), collision flag
- **Route**: Path description as list of Intersections from start to destination, current index in the route, total distance (number of road segments) for bonus calculation
- **CityGraph**: Graph representation of the city with adjacency list, Dijkstra implementation, edge weights
- **VehicleSpawner**: Manages spawn logic, selects start points and destinations, creates vehicles with calculated route
- **CollisionDetector**: Detects collisions at intersections only, triggers intersection blocking, manages blocking timers with additive duration calculation. Tracks collided vehicle IDs per intersection (max 4) and calculates blocking time as: 1 vehicle = 7.5s, 2 vehicles = 22.5s (7.5+15), 3 vehicles = 52.5s (7.5+15+30), 4 vehicles = 112.5s (7.5+15+30+60). Clears collided vehicles set when blocking expires and removes vehicles from game.
- **TrafficManager**: Manages vehicle queues and following behavior. Ensures vehicles maintain safe distances on road segments. Prevents collisions outside of intersections. Coordinates directional queue formation at red lights and blocked intersections. Enforces one-vehicle-per-direction intersection occupancy rule.
- **ScoreManager**: Manages total score, processes scoring events (intersection passed, destination reached)
- **VisualEffectsManager**: Manages particle effects, animations, and visual feedback for game events (collisions, celebrations, transitions)
- **AnimationController**: Controls and coordinates smooth transitions and interpolations for all animated elements

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Player can identify and switch a traffic light within 5 seconds
- **SC-002**: System continuously spawns vehicles (e.g., one new vehicle every 3-5 seconds)
- **SC-003**: Dijkstra route calculation completes in under 100ms for a grid up to 20x20 intersections
- **SC-004**: Collision detection works in 100% of test cases without false positives/negatives
- **SC-005**: Scoring is correct and traceable (manually verifiable through observation)
- **SC-006**: Game runs smoothly at minimum 30 FPS with 10+ simultaneous vehicles
- **SC-007**: A test player can understand what to do without instructions (intuitive controls)
- **SC-008**: Intersection blocking after accident is communicated visually and clearly (recognizable within 2 seconds)
- **SC-009**: Visual effects and animations enhance game feel and receive positive feedback from test players (80%+ approval rating)
- **SC-010**: All animations run at consistent 60 FPS without stuttering or frame drops on target platforms
- **SC-011**: Color scheme is visually cohesive and pleasing, with good contrast for accessibility
- **SC-012**: Game Over condition is triggered reliably when all entry points are blocked for the grace period
- **SC-013**: Player understands why the game ended (gridlock is clearly communicated visually and textually)

## Technical Context

### Development Environment
- **Language**: Kotlin (Kotlin Multiplatform)
- **Project Type**: Cross-Platform 2D Game
- **Graphics Framework**: Compose Multiplatform (Jetpack Compose for UI)
- **Build System**: Gradle with Kotlin Multiplatform Plugin

### Target Platforms (Prioritized)
1. **Browser (Web/JS)** - Highest Priority
   - Kotlin/JS with Compose for Web
   - WebGL or Canvas for Rendering
2. **Android APK** - Second Priority
   - Jetpack Compose for Android
   - Minimum SDK: Android 5.0 (API 21)
3. **Linux Desktop (Flatpak)** - Desirable
   - Compose for Desktop (JVM)
   - Flatpak Distribution
4. **macOS (.dmg)** - Optional
   - Compose for Desktop (JVM)
   - Native macOS Bundle
5. **iOS** - Optional
   - Compose Multiplatform iOS (Beta)
   - Minimum iOS 14+

### Architecture Considerations
- **Game Loop**: Standard game loop (Input → Update → Render) with Compose integration
- **Input Handling**: Sequential event queue processing for traffic light switches to prevent race conditions and ensure deterministic state transitions
- **Event System**: Kotlin Flow/StateFlow for reactive events (collisions, scoring, intersection passage)
- **State Management**: 
  - MVI (Model-View-Intent) Pattern with Compose
  - Immutable State with data classes
  - StateFlow for Observable State
- **Shared Code**: 
  - Common Module for game logic (Dijkstra, collision, scoring)
  - Platform-specific modules for UI rendering and input

### Performance Requirements
- Grid Size: Initial 10x10 to 20x20 intersections
- Simultaneous Vehicles: 10-50
- Frame Rate: 30 FPS minimum requirement (60 FPS desirable if achievable without significant extra development effort)
- Dijkstra Calculation: < 100ms per route
- Web Performance: Functional on modern browsers (Chrome, Firefox, Safari)
- Mobile Performance: Optimized for mid-range Android devices

### Technology Stack
- **Core**: Kotlin 1.9+, Kotlin Multiplatform
- **UI**: Compose Multiplatform (Jetpack Compose)
- **Testing**: kotlin.test (common), JUnit (JVM), Kotest (optional)
- **Coroutines**: kotlinx.coroutines for asynchronous operations
- **Serialization**: kotlinx.serialization for configuration/saves
- **DI**: Koin or manual DI (KISS principle)

---

## Clarifications *(optional)*

### Session 2026-02-01

- Q: How many independent traffic lights should exist at each intersection? → A: C (4 fully independent lights - North, South, East, West can each be Red or Green independently)
- Q: When a vehicle approaches an intersection with 4 independent lights, which light(s) determine if it can proceed? → A: B (Vehicle checks its specific directional light)
- Q: If multiple vehicles collide at same intersection during blocking period, how is blocking time calculated? → A: CUSTOM - Blocking time increases additively with each colliding vehicle using base time 7.5s: 1 vehicle = 7.5s, 2 vehicles = 22.5s (7.5+15), 3 vehicles = 52.5s (7.5+15+30), 4 vehicles = 112.5s (7.5+15+30+60). Timer resets to new value when additional collision occurs. **MAXIMUM 4 vehicles can collide at an intersection** - additional vehicles wait in queue before the intersection.
- Q: What is the target frame rate for visual smoothness and animation quality? → A: B (30 FPS minimum requirement, 60 FPS desirable if achievable without significant extra effort)
- Q: When multiple traffic lights are clicked/switched simultaneously, how should the system process them? → A: B (Clicks are queued and processed sequentially)
- Q: How do vehicles behave outside of intersections when another vehicle is ahead? → A: CUSTOM - Vehicles NEVER collide outside of intersections. They form queues/traffic jams by following the vehicle ahead at a safe distance.

