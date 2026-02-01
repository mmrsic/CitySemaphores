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

**Independent Test**: Can be tested by intentionally causing two vehicles to collide at an intersection and checking whether the intersection is blocked and visually marked.

**Acceptance Scenarios**:

1. **Given** two vehicles approaching the same intersection from different directions, **When** both enter the intersection simultaneously with a green light, **Then** a collision is detected
2. **Given** a detected collision, **When** the accident occurs, **Then** the intersection is blocked for 5-10 seconds
3. **Given** a blocked intersection, **When** the blocking time is running, **Then** all lights are displayed as red and new vehicles cannot pass
4. **Given** a blocked intersection, **When** the blocking time expires, **Then** the intersection is released and usable again

---

### User Story 4 - Scoring System (Priority: P2)

As a player, I want to collect points for successfully managed traffic so I can measure my performance and stay motivated.

**Why this priority**: Adds progression and motivation, but is not essential for basic functionality.

**Independent Test**: Can be tested by steering a vehicle through multiple intersections without accidents and checking the score (+1 per intersection, doubling at destination).

**Acceptance Scenarios**:

1. **Given** a vehicle passes an intersection without accident, **When** it leaves the intersection, **Then** the player receives +1 point
2. **Given** a vehicle with 5 passed intersections, **When** it reaches its destination, **Then** the 5 points are doubled (additional +5, total +10 for this vehicle)
3. **Given** a running game, **When** points are awarded, **Then** the current total score is displayed in the UI
4. **Given** an accident at an intersection, **When** affected vehicles collide, **Then** they receive no points for this intersection

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
- **How does the system behave when multiple lights are switched simultaneously?** → Each light is switched independently, no synchronization required
- **What happens when a new vehicle reaches an intersection during a blocking period?** → Vehicle waits in front of the intersection until the block is lifted
- **Can a vehicle drive "backwards" or change its route?** → No, route is fixed at spawn and immutable
- **What happens when no free route to the destination exists?** → Should be avoided in grid design; alternative: vehicle is not spawned
- **How are crossing routes prioritized in Dijkstra?** → All edges have equal weight (1 unit), no prioritization

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST create a grid-based city with rectangular intersections
- **FR-002**: System MUST provide a traffic light system with two states (Red/Green) at each intersection
- **FR-003**: System MUST enable separate traffic light control for horizontal and vertical directions
- **FR-004**: Player MUST be able to manually switch traffic lights via mouse click (Desktop/Web) or touch (Mobile)
- **FR-005**: System MUST spawn vehicles at city borders
- **FR-006**: System MUST calculate a route for each vehicle at spawn using Dijkstra's algorithm
- **FR-007**: Vehicles MUST stop at red lights
- **FR-008**: Vehicles MUST continue/start moving at green lights
- **FR-009**: System MUST detect collisions between two vehicles at intersections
- **FR-010**: System MUST block the affected intersection for 5-10 seconds (configurable) upon collision
- **FR-011**: System MUST visually mark blocked intersections
- **FR-012**: System MUST award +1 point when a vehicle passes an intersection without accident
- **FR-013**: System MUST double the accumulated points of a vehicle when it reaches its destination
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
- **Intersection**: A single intersection with grid position, traffic light system, status (normal/blocked), blocking timer
- **TrafficLight**: Traffic light control with state (Red/Green), direction (horizontal/vertical), switching method
- **Vehicle**: Vehicle with current position, route (list of Intersections), movement speed, point counter for passed intersections
- **Route**: Path description as list of Intersections from start to destination, current index in the route
- **CityGraph**: Graph representation of the city with adjacency list, Dijkstra implementation, edge weights
- **VehicleSpawner**: Manages spawn logic, selects start points and destinations, creates vehicles with calculated route
- **CollisionManager**: Detects collisions, triggers intersection blocking, manages blocking timers
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
- Frame Rate: 30+ FPS (60 FPS desirable for Desktop/Mobile)
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
