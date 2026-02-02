# Tasks: CitySemaphores - Traffic Light Simulation Game

**Feature**: CitySemaphores - Cross-Platform 2D Traffic Simulation Game  
**Input**: Design documents from `.specify/memory/`  
**Prerequisites**: plan.md, spec.md, data-model.md, research.md  
**Branch**: main  
**Generated**: 2026-02-01  
**Last Updated**: 2026-02-01 (Specification clarifications implemented)

**Tests**: Following TDD principles - tests written and verified to fail before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

---

## Recent Specification Changes (2026-02-02)

The following clarifications have been incorporated into this task list:

1. **Traffic Light System**: 4 fully independent traffic lights per intersection (North, South, East, West)
2. **Directional Occupancy**: Maximum 1 vehicle per direction on intersection, naturally limiting collisions to 4 vehicles max
3. **Blocking Time System**: ADDITIVE calculation (NOT exponential) with base 7.5s:
   - 1 vehicle: 7.5 seconds
   - 2 vehicles: 22.5 seconds (7.5+15)
   - 3 vehicles: 52.5 seconds (7.5+15+30)
   - 4 vehicles: 112.5 seconds (7.5+15+30+60)
   - Maximum 4 vehicles can collide at an intersection (one per direction)
4. **Scoring System**: Base score (crossings) + Distance bonus - Wait time penalty
   - Base: +1 per intersection passed
   - Bonus: Route distance (number of road segments)
   - Penalty: -1 per second of waiting time
   - Formula: score = crossings + max(0, distance - waitSeconds)
5. **Vehicle Removal**: Collided vehicles removed after intersection unblocks, player keeps earned points
6. **Vehicle Following Behavior**: Vehicles NEVER collide outside intersections - they maintain safe distance and form directional queues
7. **Game Over Condition**: Game ends when all city border entry points are blocked by traffic jams for 5 seconds (gridlock condition)
8. **Frame Rate**: 30 FPS minimum requirement, 60 FPS desirable (not mandatory)

**New Entities Added**:
- **TrafficManager**: Manages vehicle queues and following behavior
- **GameOverDetector**: Monitors gridlock condition with 5-second grace period
- **GameOverScreen**: UI for displaying final statistics

---

## Format: `- [ ] [ID] [P?] [Story] Description`

- **Checkbox**: `- [ ]` (markdown checkbox - REQUIRED)
- **[ID]**: Sequential task ID (T001, T002, etc.)
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4, US5)
- **Description**: Clear action with exact file path

---

## Phase 1: Setup (Project Initialization)

**Purpose**: Initialize Kotlin Multiplatform project with Compose Multiplatform and all platform targets

- [ ] T001 Initialize Kotlin Multiplatform project with Compose Multiplatform plugin in build.gradle.kts
- [ ] T002 Configure Gradle settings for multiplatform targets (JS IR, Android, JVM Desktop) in settings.gradle.kts
- [ ] T003 [P] Create project directory structure per plan.md (commonMain, commonTest, jsMain, androidMain, desktopMain)
- [ ] T004 [P] Add kotlinx.coroutines dependency to commonMain in composeApp/build.gradle.kts
- [ ] T005 [P] Add kotlinx.serialization plugin and dependency in build.gradle.kts
- [ ] T006 [P] Add Compose Multiplatform dependencies (runtime, foundation, material3) in composeApp/build.gradle.kts
- [ ] T007 [P] Configure kotlin.test for commonTest in composeApp/build.gradle.kts
- [ ] T008 Create basic package structure in commonMain: domain/, game/, ui/, viewmodel/, util/
- [ ] T009 [P] Setup .gitignore for Kotlin/Gradle project
- [ ] T010 [P] Configure Android manifest and MainActivity stub in androidMain/kotlin/com/citysemaphores/MainActivity.kt
- [ ] T011 [P] Create Web entry point main() in jsMain/kotlin/com/citysemaphores/Main.kt
- [ ] T012 [P] Create Desktop entry point main() in desktopMain/kotlin/com/citysemaphores/Main.kt
- [ ] T013 Verify build and "Hello World" runs on Web target with ./gradlew jsBrowserRun
- [ ] T014 Verify build works for Android target with ./gradlew :composeApp:assembleDebug
- [ ] T015 Verify build works for Desktop target with ./gradlew :composeApp:run

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story implementation

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T016 [P] Create base domain package structure: model/, graph/, collision/, scoring/ in commonMain
- [ ] T017 [P] Define Direction enum (North, South, East, West) with opposite() in commonMain/kotlin/com/citysemaphores/domain/model/Direction.kt
- [ ] T018 [P] Create GridPosition value object with neighbors() in commonMain/kotlin/com/citysemaphores/domain/model/GridPosition.kt
- [ ] T019 [P] Create Position value object with lerp() and distanceTo() in commonMain/kotlin/com/citysemaphores/domain/model/Position.kt
- [ ] T020 [P] Create Vector2D value object with operators in commonMain/kotlin/com/citysemaphores/domain/model/Vector2D.kt
- [ ] T021 [P] Define GameEvent sealed interface with all event types in commonMain/kotlin/com/citysemaphores/domain/model/GameEvent.kt
- [ ] T022 Create MVI base structure: GameUiState data class in commonMain/kotlin/com/citysemaphores/viewmodel/GameUiState.kt
- [ ] T023 Create GameIntent sealed interface in commonMain/kotlin/com/citysemaphores/viewmodel/GameIntent.kt
- [ ] T024 Create GameViewModel skeleton with StateFlow<GameUiState> in commonMain/kotlin/com/citysemaphores/viewmodel/GameViewModel.kt
- [ ] T025 [P] Setup Compose theme (Color.kt, Theme.kt, Typography.kt) in commonMain/kotlin/com/citysemaphores/ui/theme/

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Basic Traffic Light Control (Priority: P1) 🎯 MVP Core

**Goal**: Enable manual traffic light control at intersections so players can manage traffic flow

**Independent Test**: Create single intersection with traffic lights, click/tap to switch, verify state changes and visual feedback

### Tests for User Story 1

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T026 [P] [US1] Unit test for TrafficLightState enum in commonTest/kotlin/com/citysemaphores/domain/model/TrafficLightTest.kt
- [ ] T027 [P] [US1] Unit test for TrafficLight toggle() logic in commonTest/kotlin/com/citysemaphores/domain/model/TrafficLightTest.kt
- [ ] T028 [P] [US1] Unit test for Intersection canVehiclePass() in commonTest/kotlin/com/citysemaphores/domain/model/IntersectionTest.kt
- [ ] T029 [US1] Integration test for traffic light switching via GameViewModel in commonTest/kotlin/com/citysemaphores/integration/TrafficLightSwitchingTest.kt

### Implementation for User Story 1

- [ ] T030 [P] [US1] Create TrafficLightState enum (Red, Green) in commonMain/kotlin/com/citysemaphores/domain/model/TrafficLightState.kt
- [ ] T031 [P] [US1] Create TrafficLight data class with toggle() in commonMain/kotlin/com/citysemaphores/domain/model/TrafficLight.kt
- [ ] T032 [US1] Create Intersection data class with trafficLights map and canVehiclePass() in commonMain/kotlin/com/citysemaphores/domain/model/Intersection.kt
- [ ] T033 [US1] Implement SwitchTrafficLight intent handler in GameViewModel in commonMain/kotlin/com/citysemaphores/viewmodel/GameViewModel.kt
- [ ] T034 [P] [US1] Create IntersectionView composable with traffic light rendering in commonMain/kotlin/com/citysemaphores/ui/components/IntersectionView.kt
- [ ] T035 [US1] Add click/touch handling to traffic lights in IntersectionView.kt
- [ ] T036 [P] [US1] Add visual highlight for interactive traffic lights (hover/touch feedback) in IntersectionView.kt
- [ ] T037 [US1] Verify all US1 tests pass and traffic lights switch correctly

**Checkpoint**: Traffic light control is fully functional and independently testable

---

## Phase 4: User Story 2 - Vehicle Spawning and Routing (Priority: P1) 🎯 MVP Core

**Goal**: Vehicles spawn at borders and follow optimal routes calculated by Dijkstra's algorithm

**Independent Test**: Spawn vehicles at city edges, verify Dijkstra calculates shortest path, vehicles follow routes to destination

### Tests for User Story 2

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T038 [P] [US2] Unit test for CityGraph adjacency list creation in commonTest/kotlin/com/citysemaphores/domain/graph/CityGraphTest.kt
- [ ] T039 [P] [US2] Unit test for DijkstraRouter.findShortestPath() with known grid in commonTest/kotlin/com/citysemaphores/domain/graph/DijkstraRouterTest.kt
- [ ] T040 [P] [US2] Unit test for DijkstraRouter performance (<100ms for 20×20) in commonTest/kotlin/com/citysemaphores/domain/graph/DijkstraRouterTest.kt
- [ ] T041 [P] [US2] Unit test for Route.advance() and isAtDestination() in commonTest/kotlin/com/citysemaphores/domain/model/RouteTest.kt
- [ ] T042 [P] [US2] Unit test for Vehicle.move() position interpolation in commonTest/kotlin/com/citysemaphores/domain/model/VehicleTest.kt
- [ ] T043 [P] [US2] Unit test for VehicleSpawner spawn logic in commonTest/kotlin/com/citysemaphores/game/VehicleSpawnerTest.kt
- [ ] T044 [US2] Integration test for vehicle spawning and routing flow in commonTest/kotlin/com/citysemaphores/integration/VehicleSpawningTest.kt

### Implementation for User Story 2

- [ ] T045 [P] [US2] Create Route data class with path navigation in commonMain/kotlin/com/citysemaphores/domain/model/Route.kt
- [ ] T046 [P] [US2] Create VehicleState enum (Moving, Waiting, Arrived, Crashed) in commonMain/kotlin/com/citysemaphores/domain/model/VehicleState.kt
- [ ] T047 [US2] Create Vehicle data class with position, route, movement logic in commonMain/kotlin/com/citysemaphores/domain/model/Vehicle.kt
- [ ] T048 [P] [US2] Create Edge data class in commonMain/kotlin/com/citysemaphores/domain/graph/Edge.kt
- [ ] T049 [US2] Create CityGraph data class with adjacency list in commonMain/kotlin/com/citysemaphores/domain/graph/CityGraph.kt
- [ ] T050 [US2] Implement DijkstraRouter.findShortestPath() algorithm in commonMain/kotlin/com/citysemaphores/domain/graph/DijkstraRouter.kt
- [ ] T051 [US2] Create City data class with intersection map and graph in commonMain/kotlin/com/citysemaphores/domain/model/City.kt
- [ ] T052 [US2] Implement VehicleSpawner with spawn timing and route calculation in commonMain/kotlin/com/citysemaphores/game/VehicleSpawner.kt
- [ ] T053 [US2] Create GameTimer for spawn interval tracking in commonMain/kotlin/com/citysemaphores/game/GameTimer.kt
- [ ] T054 [P] [US2] Create VehicleView composable with basic rendering in commonMain/kotlin/com/citysemaphores/ui/components/VehicleView.kt
- [ ] T055 [P] [US2] Create CityGridView composable for rendering city layout in commonMain/kotlin/com/citysemaphores/ui/components/CityGridView.kt
- [ ] T056 [US2] Integrate VehicleSpawner into GameEngine in commonMain/kotlin/com/citysemaphores/game/GameEngine.kt
- [ ] T057 [US2] Add vehicle movement update logic in GameEngine.kt
- [ ] T058 [US2] Handle vehicle arrival at destination (remove from game) in GameEngine.kt
- [ ] T059 [US2] Verify all US2 tests pass and vehicles spawn/route/move correctly

**Checkpoint**: Vehicle spawning and routing system is fully functional

---

## Phase 5: User Story 3 - Collision Detection and Blocking (Priority: P1) 🎯 MVP Core

**Goal**: Detect collisions at intersections, provide immediate feedback, block intersections temporarily

**Independent Test**: Force two vehicles into same intersection simultaneously, verify collision detection and blocking behavior

### Tests for User Story 3

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T060 [P] [US3] Unit test for CollisionDetector.detectCollision() basic case in commonTest/kotlin/com/citysemaphores/domain/collision/CollisionDetectorTest.kt
- [ ] T061 [P] [US3] Unit test for CollisionDetector with no collision in commonTest/kotlin/com/citysemaphores/domain/collision/CollisionDetectorTest.kt
- [ ] T062 [P] [US3] Unit test for Intersection.blockWithCollision() with ADDITIVE blocking times (1 vehicle=7.5s, 2 vehicles=22.5s, 3 vehicles=52.5s, 4 vehicles=112.5s) in commonTest/kotlin/com/citysemaphores/domain/model/IntersectionTest.kt
- [ ] T063 [P] [US3] Unit test for 4-vehicle collision cap (additional vehicles wait in queue) in commonTest/kotlin/com/citysemaphores/domain/model/IntersectionTest.kt
- [ ] T064 [US3] Integration test for collision → additive blocking → unblocking flow in commonTest/kotlin/com/citysemaphores/integration/CollisionFlowTest.kt
- [ ] T065 [P] [US3] Unit test for TrafficManager.updateVehicleFollowing() safe distance maintenance in commonTest/kotlin/com/citysemaphores/game/TrafficManagerTest.kt
- [ ] T066 [P] [US3] Unit test for TrafficManager.formQueue() at blocked intersections in commonTest/kotlin/com/citysemaphores/game/TrafficManagerTest.kt

### Implementation for User Story 3

- [ ] T067 [US3] Add blockWithCollision(collidingVehicleIds: Set<String>) method with ADDITIVE blocking calculation to Intersection data class in commonMain/kotlin/com/citysemaphores/domain/model/Intersection.kt
- [ ] T068 [US3] Add directional occupancy map (Map<Direction, String?>) to Intersection for tracking one vehicle per direction in commonMain/kotlin/com/citysemaphores/domain/model/Intersection.kt
- [ ] T069 [US3] Add collidedVehicles: Set<String> to Intersection to track collided vehicle IDs and implement updateBlockTimer() in commonMain/kotlin/com/citysemaphores/domain/model/Intersection.kt
- [ ] T070 [US3] Add canVehicleEnter(from: Direction, vehicleId: String), enterIntersection(), leaveIntersection() methods to Intersection in commonMain/kotlin/com/citysemaphores/domain/model/Intersection.kt
- [ ] T071 [US3] Update Vehicle to include waitTime: Float and isInCollision: Boolean properties in commonMain/kotlin/com/citysemaphores/domain/model/Vehicle.kt
- [ ] T072 [US3] Add waitAtIntersection(deltaTime: Float) and calculateScore() methods to Vehicle in commonMain/kotlin/com/citysemaphores/domain/model/Vehicle.kt
- [ ] T073 [US3] Add totalDistance property to Route for bonus score calculation in commonMain/kotlin/com/citysemaphores/domain/model/Route.kt
- [ ] T074 [US3] Implement CollisionDetector.detectCollisions() with spatial check for multiple vehicles in commonMain/kotlin/com/citysemaphores/domain/collision/CollisionDetector.kt
- [ ] T075 [US3] Implement CollisionDetector.handleCollision() to update intersection and mark vehicles in commonMain/kotlin/com/citysemaphores/domain/collision/CollisionDetector.kt
- [ ] T076 [US3] Implement TrafficManager.canVehicleEnterIntersection() checking occupancy in commonMain/kotlin/com/citysemaphores/game/TrafficManager.kt
- [ ] T077 [US3] Implement TrafficManager.updateVehicleFollowing() for safe distance maintenance in commonMain/kotlin/com/citysemaphores/game/TrafficManager.kt
- [ ] T078 [US3] Implement TrafficManager.formQueue() for directional intersection queue management in commonMain/kotlin/com/citysemaphores/game/TrafficManager.kt
- [ ] T079 [US3] Integrate TrafficManager and CollisionDetector into GameEngine update loop in commonMain/kotlin/com/citysemaphores/game/GameEngine.kt
- [ ] T080 [US3] Handle collision events with additive blocking and vehicle removal after unblocking in GameEngine.kt
- [ ] T081 [US3] Implement intersection blocking timer updates and vehicle removal on unblock in GameEngine.kt
- [ ] T082 [P] [US3] Add blocked intersection visual indicators in IntersectionView.kt (warning symbols, color overlay, pulsing effects)
- [ ] T083 [P] [US3] Update traffic light rendering to show all red during blocking in IntersectionView.kt
- [ ] T084 [P] [US3] Add visual queue indicators for waiting vehicles in VehicleView.kt
- [ ] T083 [US3] Verify all US3 tests pass and collision detection with additive blocking works correctly

**Checkpoint**: Collision detection and intersection blocking fully functional - MVP CORE COMPLETE

---

## Phase 6: User Story 4 - Scoring System (Priority: P2)

**Goal**: Award points for successfully managed traffic to measure player performance

**Independent Test**: Drive vehicle through multiple intersections, verify +1 per intersection (base), verify distance bonus minus wait time at destination

### Tests for User Story 4

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T085 [P] [US4] Unit test for Vehicle.calculateScore() with no waiting time in commonTest/kotlin/com/citysemaphores/domain/model/VehicleTest.kt
- [ ] T086 [P] [US4] Unit test for Vehicle.calculateScore() with waiting time penalty in commonTest/kotlin/com/citysemaphores/domain/model/VehicleTest.kt
- [ ] T087 [P] [US4] Unit test for Vehicle.calculateScore() with wait time exceeding distance in commonTest/kotlin/com/citysemaphores/domain/model/VehicleTest.kt
- [ ] T088 [P] [US4] Unit test for Vehicle.passCrossing() incrementing counter in commonTest/kotlin/com/citysemaphores/domain/model/VehicleTest.kt
- [ ] T089 [P] [US4] Unit test for Vehicle.waitAtIntersection() accumulating wait time in commonTest/kotlin/com/citysemaphores/domain/model/VehicleTest.kt
- [ ] T090 [US4] Integration test for scoring with bonus and penalty across multiple vehicles in commonTest/kotlin/com/citysemaphores/integration/ScoringTest.kt

### Implementation for User Story 4

- [ ] T091 [US4] Vehicle already has crossingsPassed, waitTime, and calculateScore() from Phase 5 - verify implementation
- [ ] T092 [US4] Route already has totalDistance property from Phase 5 - verify implementation
- [ ] T093 [US4] Add vehiclesRemoved counter to GameState in commonMain/kotlin/com/citysemaphores/domain/model/GameState.kt
- [ ] T094 [US4] Update GameState.removeVehicle() to calculate and add final score in commonMain/kotlin/com/citysemaphores/domain/model/GameState.kt
- [ ] T095 [US4] Update GameState.vehicleReachedDestination() to calculate final score with bonus/penalty in commonMain/kotlin/com/citysemaphores/domain/model/GameState.kt
- [ ] T096 [US4] Integrate wait time tracking into GameEngine vehicle update loop in commonMain/kotlin/com/citysemaphores/game/GameEngine.kt
- [ ] T097 [US4] Implement destination arrival scoring with calculateScore() in GameEngine.kt
- [ ] T098 [US4] Implement collision vehicle removal with score calculation in GameEngine.kt
- [ ] T099 [US4] Add score to GameUiState in commonMain/kotlin/com/citysemaphores/viewmodel/GameUiState.kt
- [ ] T100 [P] [US4] Create ScoreDisplay composable showing total score and stats in commonMain/kotlin/com/citysemaphores/ui/components/ScoreDisplay.kt
- [ ] T101 [US4] Integrate ScoreDisplay into GameScreen in commonMain/kotlin/com/citysemaphores/ui/screens/GameScreen.kt
- [ ] T102 [US4] Verify all US4 tests pass and scoring with bonus/penalty works correctly

**Checkpoint**: Scoring system fully functional

---

## Phase 7: User Story 6 - Game Over Condition (Priority: P2)

**Goal**: Detect gridlock condition when all city border entry points are blocked and trigger game over after 5-second grace period

**Independent Test**: Create traffic jams blocking all entry points, verify gridlock detection after 5-second grace period, verify game over screen displays statistics

### Tests for User Story 6

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T087 [P] [US6] Unit test for GameOverDetector.checkGridlock() detecting clear state in commonTest/kotlin/com/citysemaphores/game/GameOverDetectorTest.kt
- [ ] T088 [P] [US6] Unit test for GameOverDetector.checkGridlock() detecting warning state in commonTest/kotlin/com/citysemaphores/game/GameOverDetectorTest.kt
- [ ] T089 [P] [US6] Unit test for GameOverDetector grace period timer countdown (5 seconds) in commonTest/kotlin/com/citysemaphores/game/GameOverDetectorTest.kt
- [ ] T090 [P] [US6] Unit test for GameOverDetector triggering game over after grace period expires in commonTest/kotlin/com/citysemaphores/game/GameOverDetectorTest.kt
- [ ] T091 [P] [US6] Unit test for GameOverDetector resetting timer when gridlock clears in commonTest/kotlin/com/citysemaphores/game/GameOverDetectorTest.kt
- [ ] T092 [US6] Integration test for full gridlock → warning → game over flow in commonTest/kotlin/com/citysemaphores/integration/GameOverFlowTest.kt

### Implementation for User Story 6

- [ ] T093 [P] [US6] Create GridlockStatus sealed interface (Clear, Warning, GameOver) in commonMain/kotlin/com/citysemaphores/domain/model/GridlockStatus.kt
- [ ] T094 [US6] Add isGameOver and gridlockStatus to GameState data class in commonMain/kotlin/com/citysemaphores/domain/model/GameState.kt
- [ ] T095 [US6] Create GameOverDetector with checkGridlock() and grace period logic in commonMain/kotlin/com/citysemaphores/game/GameOverDetector.kt
- [ ] T096 [US6] Implement GameOverDetector.isEntryPointBlocked() helper method in commonMain/kotlin/com/citysemaphores/game/GameOverDetector.kt
- [ ] T097 [US6] Implement GameOverDetector.updateGridlockTimer() for grace period countdown in commonMain/kotlin/com/citysemaphores/game/GameOverDetector.kt
- [ ] T098 [US6] Integrate GameOverDetector into GameEngine update loop in commonMain/kotlin/com/citysemaphores/game/GameEngine.kt
- [ ] T099 [US6] Add gridlock status to GameUiState in commonMain/kotlin/com/citysemaphores/viewmodel/GameUiState.kt
- [ ] T100 [P] [US6] Create GameOverScreen composable with statistics display in commonMain/kotlin/com/citysemaphores/ui/screens/GameOverScreen.kt
- [ ] T101 [P] [US6] Add restart button to GameOverScreen in commonMain/kotlin/com/citysemaphores/ui/screens/GameOverScreen.kt
- [ ] T102 [P] [US6] Add return to menu button to GameOverScreen in commonMain/kotlin/com/citysemaphores/ui/screens/GameOverScreen.kt
- [ ] T103 [P] [US6] Add gridlock warning visual indicator to GameScreen (timer countdown, warning message) in commonMain/kotlin/com/citysemaphores/ui/screens/GameScreen.kt
- [ ] T104 [US6] Integrate GameOverScreen navigation into GameViewModel in commonMain/kotlin/com/citysemaphores/viewmodel/GameViewModel.kt
- [ ] T105 [US6] Verify all US6 tests pass and game over detection works correctly

**Checkpoint**: Game over condition and UI fully functional

---

## Phase 8: User Story 5 - Visual Representation and UI (Priority: P2)

**Goal**: Deliver visually impressive 2D top-down view with modern graphics, smooth animations, particle effects, and polished UI

**Independent Test**: Visual inspection of rendered city, animations, effects, and UI for quality and appeal

### Tests for User Story 5

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T106 [P] [US5] Unit test for lerp interpolation function in commonTest/kotlin/com/citysemaphores/util/MathUtilsTest.kt
- [ ] T107 [P] [US5] Unit test for ParticleEffect.update() lifecycle in commonTest/kotlin/com/citysemaphores/ui/effects/ParticleEffectTest.kt
- [ ] T108 [US5] Performance test for 30 FPS minimum (60 FPS desirable) with 50 vehicles in commonTest/kotlin/com/citysemaphores/integration/PerformanceTest.kt

### Implementation for User Story 5

- [ ] T109 [P] [US5] Define color palette with vibrant colors in commonMain/kotlin/com/citysemaphores/ui/theme/Color.kt
- [ ] T110 [P] [US5] Create Material3 theme configuration in commonMain/kotlin/com/citysemaphores/ui/theme/Theme.kt
- [ ] T111 [P] [US5] Create lerp() and interpolation utilities in commonMain/kotlin/com/citysemaphores/util/MathUtils.kt
- [ ] T112 [P] [US5] Create AnimatedPosition data class in commonMain/kotlin/com/citysemaphores/util/Animation.kt
- [ ] T113 [US5] Implement smooth vehicle movement with interpolation in VehicleView.kt using animateFloatAsState
- [ ] T114 [US5] Add traffic light transition animations in IntersectionView.kt using AnimatedContent
- [ ] T115 [P] [US5] Create Particle data class in commonMain/kotlin/com/citysemaphores/ui/effects/Particle.kt
- [ ] T116 [US5] Implement ParticleEffect interface in commonMain/kotlin/com/citysemaphores/ui/effects/ParticleEffect.kt
- [ ] T117 [US5] Create CollisionParticles implementation with emit/update/render in commonMain/kotlin/com/citysemaphores/ui/effects/CollisionParticles.kt
- [ ] T118 [US5] Create CelebrationParticles implementation in commonMain/kotlin/com/citysemaphores/ui/effects/CelebrationParticles.kt
- [ ] T119 [US5] Create VisualEffectsManager to coordinate all effects in commonMain/kotlin/com/citysemaphores/ui/effects/VisualEffectsManager.kt
- [ ] T120 [US5] Integrate VisualEffectsManager into GameEngine in commonMain/kotlin/com/citysemaphores/game/GameEngine.kt
- [ ] T121 [US5] Add glow effects to traffic lights using Canvas drawCircle with blur in IntersectionView.kt
- [ ] T122 [P] [US5] Create VisualEffects composable wrapper in commonMain/kotlin/com/citysemaphores/ui/components/VisualEffects.kt
- [ ] T123 [US5] Trigger collision effects when accidents occur in GameEngine.kt
- [ ] T124 [US5] Trigger celebration effects on vehicle destination arrival in GameEngine.kt
- [ ] T125 [P] [US5] Add score popup animation for destination arrival in ScoreDisplay.kt
- [ ] T126 [US5] Polish city grid rendering with clean lines and modern style in CityGridView.kt
- [ ] T127 [US5] Create GameScreen main layout with all components in commonMain/kotlin/com/citysemaphores/ui/screens/GameScreen.kt
- [ ] T128 [P] [US5] Create MenuScreen with game start options in commonMain/kotlin/com/citysemaphores/ui/screens/MenuScreen.kt
- [ ] T129 [US5] Verify 30 FPS minimum (60 FPS desirable) on Desktop and Web with performance profiling
- [ ] T130 [US5] Verify all US5 tests pass and visual quality meets requirements

**Checkpoint**: Visual polish and effects complete - game is visually impressive

---

## Phase 9: Game Loop Integration (Foundation Enhancement)

**Goal**: Implement robust game loop with proper timing, state updates, and event propagation

**Independent Test**: Verify game loop runs at consistent frame rate with accurate deltaTime calculations

### Tests for Game Loop

> **TDD: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T131 [P] Unit test for GameEngine.update() with mock deltaTime in commonTest/kotlin/com/citysemaphores/game/GameEngineTest.kt
- [ ] T132 [P] Unit test for GameEngine event emission in commonTest/kotlin/com/citysemaphores/game/GameEngineTest.kt
- [ ] T133 Integration test for complete game loop cycle in commonTest/kotlin/com/citysemaphores/integration/GameLoopTest.kt

### Implementation for Game Loop

- [ ] T134 Create GameState data class with all game state in commonMain/kotlin/com/citysemaphores/domain/model/GameState.kt
- [ ] T135 Implement GameEngine with update/render cycle in commonMain/kotlin/com/citysemaphores/game/GameEngine.kt
- [ ] T136 Add StateFlow<GameEvent> to GameEngine for event propagation in GameEngine.kt
- [ ] T137 Integrate GameEngine into GameViewModel in commonMain/kotlin/com/citysemaphores/viewmodel/GameViewModel.kt
- [ ] T138 Implement game loop timing with LaunchedEffect in GameScreen.kt
- [ ] T139 Add pause/resume functionality to GameEngine in GameEngine.kt
- [ ] T140 Add pause/resume intents to GameViewModel in GameViewModel.kt
- [ ] T141 Verify all game loop tests pass and loop runs smoothly at 30+ FPS

**Checkpoint**: Game loop fully integrated and functional

---

## Phase 10: Platform Support - Web (Priority: P1)

**Goal**: Optimize and deploy for Browser/Web platform

- [ ] T142 [P] Create HTML entry point in jsMain/resources/index.html
- [ ] T143 [P] Configure Webpack for production build in composeApp/build.gradle.kts jsMain section
- [ ] T144 [P] Implement Canvas-based rendering optimization in jsMain/kotlin/com/citysemaphores/platform/CanvasRenderer.kt
- [ ] T145 [P] Implement web input handling (mouse, keyboard) in jsMain/kotlin/com/citysemaphores/platform/WebInput.kt
- [ ] T146 Optimize Web bundle size with code splitting
- [ ] T147 Test on Chrome, Firefox, Safari for compatibility
- [ ] T148 Verify 30+ FPS performance on web target (60 FPS desirable)
- [ ] T149 Deploy web build for testing with ./gradlew jsBrowserDistribution

**Checkpoint**: Web platform fully functional and optimized

---

## Phase 11: Platform Support - Android (Priority: P2)

**Goal**: Optimize and deploy for Android platform

- [ ] T150 [P] Configure Android SDK versions in composeApp/build.gradle.kts (minSdk 21, targetSdk 34)
- [ ] T151 [P] Implement AndroidManifest.xml with permissions and activity in androidMain/AndroidManifest.xml
- [ ] T152 [P] Create MainActivity with Compose integration in androidMain/kotlin/com/citysemaphores/MainActivity.kt
- [ ] T153 [P] Implement touch input handling for Android in androidMain/kotlin/com/citysemaphores/platform/AndroidPlatform.kt
- [ ] T154 Configure hardware acceleration in AndroidManifest.xml
- [ ] T155 Test on Android emulator (Pixel 5, API 30)
- [ ] T156 Test on physical mid-range Android device
- [ ] T157 Verify 30 FPS minimum (60 FPS desirable) on mid-range devices
- [ ] T158 Build release APK with ./gradlew :composeApp:assembleRelease
- [ ] T159 Configure app signing for release in gradle.properties

**Checkpoint**: Android platform fully functional and optimized

---

## Phase 12: Platform Support - Desktop Linux (Priority: P3)

**Goal**: Build and package for Linux Desktop platform

- [ ] T160 [P] Create Desktop main() entry point in desktopMain/kotlin/com/citysemaphores/Main.kt
- [ ] T161 [P] Configure Desktop window settings in Main.kt
- [ ] T162 [P] Implement desktop input handling (mouse, keyboard) in desktopMain/kotlin/com/citysemaphores/platform/DesktopPlatform.kt
- [ ] T163 Test on Linux with ./gradlew :composeApp:run
- [ ] T164 Verify 30 FPS minimum (60 FPS desirable) on Linux Desktop
- [ ] T165 Create Flatpak manifest for Linux distribution
- [ ] T166 Build distributable package with ./gradlew :composeApp:createDistributable
- [ ] T167 Test Flatpak installation and execution

**Checkpoint**: Linux Desktop platform fully functional

---

## Phase 13: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements affecting multiple user stories

- [ ] T168 [P] Add comprehensive KDoc comments to all public APIs in domain/ and game/ packages
- [ ] T169 [P] Create README.md with project overview, build instructions, and platform support
- [ ] T170 [P] Add LICENSE file (specify license type)
- [ ] T171 Add game configuration file support (grid size, spawn rate, etc.) using kotlinx.serialization
- [ ] T172 [P] Implement error handling and logging across all components
- [ ] T173 [P] Add accessibility features (keyboard navigation, screen reader support)
- [ ] T174 Code cleanup and refactoring for readability
- [ ] T175 Performance profiling across all platforms with optimization as needed
- [ ] T176 [P] Add unit tests for edge cases (empty grid, single intersection, etc.)
- [ ] T177 Security review for web platform (CSP, XSS prevention)
- [ ] T178 [P] Create user documentation in docs/user-guide.md
- [ ] T179 [P] Create developer documentation in docs/developer-guide.md
- [ ] T180 Run all tests across all platforms and verify 100% pass rate
- [ ] T181 Final validation against all acceptance criteria from spec.md

---

## Dependencies & Execution Order

### Phase Dependencies

1. **Setup (Phase 1)**: No dependencies - can start immediately
2. **Foundational (Phase 2)**: Depends on Setup completion - **BLOCKS all user stories**
3. **User Stories (Phase 3-8)**: All depend on Foundational phase completion
   - US1, US2, US3 (P1 priority) form the **MVP core** and should be completed first
   - US4, US6 (P2 priority) add scoring and game over detection
   - US5 (P2 priority) adds visual polish and effects
   - Game Loop (Phase 9) integrates all components and should come after core user stories
   - User stories CAN proceed in parallel if staffed
4. **Platform Support (Phase 10-12)**: Depends on all P1 user stories (US1-US3) being complete
   - Web (P1) should be prioritized
   - Android (P2) and Desktop (P3) can proceed in parallel after Web
5. **Polish (Phase 13)**: Depends on all desired user stories and platforms being complete

### User Story Dependencies

- **US1 (P1 - Traffic Lights)**: Foundation only - No dependencies on other stories
- **US2 (P1 - Vehicles)**: Foundation only - No dependencies on other stories  
- **US3 (P1 - Collisions)**: Requires US1 (intersections) and US2 (vehicles) models but can test independently
- **US4 (P2 - Scoring)**: Requires US2 (vehicles) and US3 (crossings) but independently testable
- **US6 (P2 - Game Over)**: Requires US2 (vehicles for gridlock detection) but independently testable
- **US5 (P2 - Visual Polish)**: Enhances all previous stories but independently testable
- **Game Loop (Phase 9)**: Integrates US1-US6, should come after MVP core (US1-US3)

### Critical Path (Sequential MVP)

For single developer or sequential development:

1. Complete **Phase 1: Setup** (T001-T015)
2. Complete **Phase 2: Foundational** (T016-T025) ← CRITICAL BLOCKER
3. Complete **Phase 3: US1** (T026-T037) ← MVP Component 1
4. Complete **Phase 4: US2** (T038-T059) ← MVP Component 2
5. Complete **Phase 5: US3** (T060-T083) ← MVP Component 3  
6. Complete **Phase 9: Game Loop** (T131-T141) ← MVP Integration
7. Complete **Phase 10: Web Platform** (T142-T149) ← MVP Deployment
8. **STOP**: You now have a functional MVP!
9. Continue with US4 (Scoring), US6 (Game Over), US5 (Visual Polish), additional platforms as desired

### Parallel Opportunities

#### Within Setup Phase
All tasks marked [P] in Phase 1 can run in parallel (T003-T012)

#### Within Foundational Phase
All tasks marked [P] in Phase 2 can run in parallel (T016-T021, T025)

#### Across User Stories (if team capacity allows)
Once Phase 2 completes:
- **Team A**: US1 Traffic Lights (T026-T037)
- **Team B**: US2 Vehicles (T038-T059)
- **Team C**: Start US4 Scoring models (T077-T079)

After MVP core (US1-US3-Game Loop):
- **Team A**: US4 Scoring (T073-T086)
- **Team B**: US6 Game Over (T087-T105)
- **Team C**: US5 Visual Polish (T106-T130)
- **Team D**: Web Platform (T142-T149)

#### Within Each User Story
- All test tasks marked [P] can run in parallel
- All model creation tasks marked [P] can run in parallel
- All independent UI component tasks marked [P] can run in parallel

### Parallel Example: User Story 2 (Vehicles)

```bash
# Phase: Write all tests in parallel (TDD - ensure they FAIL first)
Parallel Task T038: Unit test for CityGraph adjacency list
Parallel Task T039: Unit test for DijkstraRouter.findShortestPath()
Parallel Task T040: Unit test for DijkstraRouter performance
Parallel Task T041: Unit test for Route.advance()
Parallel Task T042: Unit test for Vehicle.move()
Parallel Task T043: Unit test for VehicleSpawner

# Phase: Create all models in parallel
Parallel Task T045: Create Route data class
Parallel Task T046: Create VehicleState enum
Parallel Task T048: Create Edge data class

# Sequential: Core implementations
Task T047: Create Vehicle data class (needs T045, T046)
Task T049: Create CityGraph (needs T048)
Task T050: Implement DijkstraRouter (needs T049)
Task T052: Implement VehicleSpawner (needs T047, T050)

# Parallel: Independent UI components
Parallel Task T054: Create VehicleView composable
Parallel Task T055: Create CityGridView composable

# Sequential: Integration
Task T056-T059: Integrate into GameEngine and verify tests
```

---

## Implementation Strategy

### Option 1: MVP First (Recommended for Solo Developer)

**Goal**: Fastest path to working game

1. ✅ Complete **Phase 1: Setup** (T001-T015)
2. ✅ Complete **Phase 2: Foundational** (T016-T025) ← CRITICAL
3. ✅ Complete **Phase 3: US1 - Traffic Lights** (T026-T037)
4. ✅ Complete **Phase 4: US2 - Vehicles** (T038-T059)
5. ✅ Complete **Phase 5: US3 - Collisions** (T060-T083)
6. ✅ Complete **Phase 9: Game Loop** (T131-T141)
7. ✅ Complete **Phase 10: Web Platform** (T142-T149)
8. **STOP and VALIDATE**: You have a working MVP on Web!
9. Demo, gather feedback, iterate

**Result**: Playable game in ~2-3 weeks

### Option 2: Incremental Delivery

**Goal**: Deploy value continuously

1. Foundation (Phase 1-2)
2. US1 → Test independently → Internal demo
3. US2 → Test independently → Internal demo (vehicles move!)
4. US3 → Test independently → Internal demo (collisions work!)
5. Game Loop → Integrate → Internal demo
6. Web Platform → **PUBLIC RELEASE v0.1** (MVP!)
7. US4 → Test independently → Release v0.2 (with scoring)
8. US6 → Test independently → Release v0.3 (with game over detection)
9. US5 → Test independently → Release v0.4 (visual polish)
10. Android Platform → Release v1.0 (mobile launch)

**Result**: Continuous value delivery with multiple releases

### Option 3: Parallel Team Strategy

**Goal**: Maximize throughput with team

**Week 1**: All team together
- Complete Phase 1: Setup
- Complete Phase 2: Foundational ← EVERYONE MUST FINISH THIS

**Week 2**: Split into parallel tracks
- **Developer A**: US1 Traffic Lights (T026-T037)
- **Developer B**: US2 Vehicles (T038-T059)  
- **Developer C**: US4 Scoring models/tests (T073-T079)

**Week 3**: Integration and polish
- **Developer A**: US3 Collisions (T060-T083)
- **Developer B**: Game Loop Integration (T131-T141)
- **Developer C**: US6 Game Over (T087-T105)

**Week 4**: Platforms and completion
- **All**: Code review, integration testing
- **Developer A**: Web Platform (T142-T149)
- **Developer B**: Android Platform (T150-T159)
- **Developer C**: US5 Visual Polish (T106-T130)

**Result**: Full-featured multi-platform game in 4 weeks

---

## Test-Driven Development (TDD) Workflow

**CRITICAL**: Following TDD principles as requested

### For Each User Story:

1. **RED Phase**: Write all tests FIRST
   - Write unit tests for models/logic
   - Write integration tests for user journeys
   - Run tests → **VERIFY THEY FAIL** (no implementation yet)
   - Commit: "Add failing tests for US[X]"

2. **GREEN Phase**: Implement to pass tests
   - Implement minimum code to make tests pass
   - Run tests frequently
   - When all tests pass → Commit: "Implement US[X]"

3. **REFACTOR Phase**: Clean up implementation
   - Improve code quality without changing behavior
   - Tests must still pass
   - Commit: "Refactor US[X]"

### Example TDD Cycle for US1 (Traffic Lights):

```bash
# RED: Write failing tests
Task T026: Write test for TrafficLightState enum
Task T027: Write test for TrafficLight.toggle()
Task T028: Write test for Intersection.canVehiclePass()
Task T029: Write integration test for switching
→ Run tests → ALL FAIL ✅ (expected!)
→ Commit: "Add failing tests for US1 traffic light control"

# GREEN: Implement to pass
Task T030: Create TrafficLightState enum
Task T031: Create TrafficLight with toggle()
Task T032: Create Intersection with canVehiclePass()
Task T033: Implement SwitchTrafficLight intent
→ Run tests → ALL PASS ✅
→ Commit: "Implement US1 traffic light control"

# REFACTOR: Improve code
Task T034-T037: Add UI components and polish
→ Run tests → ALL STILL PASS ✅
→ Commit: "Add UI for US1 traffic light control"
```

### Test Coverage Goals

- **Unit Tests**: 70%+ coverage for domain logic (graph, collision, scoring)
- **Integration Tests**: All user story acceptance scenarios covered
- **Performance Tests**: Verify FPS targets and Dijkstra performance
- **Platform Tests**: Smoke tests on each platform (Web, Android, Desktop)

---

## Effort Estimates

### By Phase
- **Phase 1 (Setup)**: 4-6 hours
- **Phase 2 (Foundational)**: 6-8 hours
- **Phase 3 (US1 - Traffic Lights)**: 8-10 hours
- **Phase 4 (US2 - Vehicles)**: 16-20 hours (most complex: Dijkstra, routing)
- **Phase 5 (US3 - Collisions + TrafficManager)**: 12-16 hours (includes vehicle following behavior)
- **Phase 6 (US4 - Scoring)**: 4-6 hours
- **Phase 7 (US6 - Game Over)**: 6-8 hours (gridlock detection, grace period, UI)
- **Phase 8 (US5 - Visual Polish)**: 12-16 hours (animations, particles, effects)
- **Phase 9 (Game Loop)**: 6-8 hours
- **Phase 10 (Web Platform)**: 6-8 hours
- **Phase 11 (Android Platform)**: 8-10 hours
- **Phase 12 (Desktop Platform)**: 4-6 hours
- **Phase 13 (Polish)**: 8-12 hours

**MVP Total (Phase 1-5 + 9-10)**: ~70-90 hours (1.5-2.5 weeks solo, 4-5 days team of 3)  
**Full Feature (All phases)**: ~120-150 hours (3-4 weeks solo, 1.5-2 weeks team of 3)

### By Task Type
- Test task: 15-30 minutes each
- Model/Value Object: 30-60 minutes each
- Algorithm (Dijkstra): 3-4 hours
- UI Component: 1-2 hours each
- Integration: 2-4 hours each
- Platform setup: 2-3 hours each

---

## Success Criteria Validation

After completion, verify these measurable outcomes from spec.md:

- [ ] **SC-001**: Player can identify and switch traffic light within 5 seconds ✓
- [ ] **SC-002**: System spawns vehicles continuously (one every 3-5 seconds) ✓
- [ ] **SC-003**: Dijkstra completes in <100ms for 20×20 grid ✓ (T040 test)
- [ ] **SC-004**: Collision detection 100% accurate ✓ (T060-T064 tests)
- [ ] **SC-005**: Scoring correct and traceable ✓ (T073-T076 tests)
- [ ] **SC-006**: 30 FPS minimum with 10+ vehicles (60 FPS desirable) ✓ (T108, T129, T148 tests)
- [ ] **SC-007**: Intuitive controls without tutorial ✓ (user testing)
- [ ] **SC-008**: Intersection blocking recognizable within 2 seconds ✓ (T080 visual indicators)
- [ ] **SC-009**: 80%+ positive feedback on visuals ✓ (user testing phase)
- [ ] **SC-010**: Smooth animations at 30+ FPS (60 FPS desirable) without stuttering ✓ (T108, T129 tests)
- [ ] **SC-011**: Cohesive color scheme with good contrast ✓ (T109-T110 theme)
- [ ] **SC-012**: Game Over condition triggers reliably when all entry points blocked for 5 seconds ✓ (T087-T092 tests)
- [ ] **SC-013**: Player understands why game ended (gridlock clearly communicated) ✓ (T100-T103 UI)

---

## Notes

- **[P] marker**: Different files, can be worked on simultaneously, no blocking dependencies
- **[Story] label**: Maps task to specific user story for traceability and independent testing
- **TDD Critical**: Tests MUST be written first and verified to fail before implementation
- **MVP = US1 + US2 + US3 + Game Loop + Web Platform**: Prioritize these for fastest time to playable game
- **Enhanced MVP = MVP + US4 (Scoring) + US6 (Game Over)**: Adds full game experience with win/lose conditions
- **Additive Blocking Times**: 1 vehicle = 7.5s, 2 vehicles = 22.5s (7.5+15), 3 vehicles = 52.5s (7.5+15+30), 4 vehicles = 112.5s (7.5+15+30+60) - NOT exponential
- **Directional Occupancy**: Max 1 vehicle per direction on intersection, naturally limiting collisions to 4 vehicles
- **Scoring System**: Base (crossings) + Bonus (distance) - Penalty (wait time seconds)
- **Vehicle Removal**: Collided vehicles removed after unblock, player keeps earned points
- **Vehicle Following**: Vehicles NEVER collide outside intersections - they maintain safe distance and form queues
- **Frame Rate Targets**: 30 FPS minimum requirement, 60 FPS desirable (not mandatory)
- **Commit strategy**: Commit after each task or logical group (e.g., all tests for a story, then all implementations)
- **Checkpoints**: Stop at phase checkpoints to validate story independence and completeness
- **File paths**: All paths shown are based on plan.md structure - adjust as needed
- **Performance**: Profile early and often, especially for Web platform (T148, T108, T129)

---

**Total Tasks**: 181 (increased from 162 to reflect US6 and TrafficManager additions)  
**MVP Tasks**: ~80 tasks (Phase 1-5, 9-10)  
**Enhanced MVP Tasks**: ~105 tasks (includes US4 and US6)  
**Test Tasks**: 40 (following TDD principles)  
**Parallel Opportunities**: ~55 tasks marked [P]  
**Independent Stories**: 6 user stories (US1-US6), each independently testable  
**Suggested MVP Scope**: US1 + US2 + US3 + Game Loop + Web Platform  
**Suggested Enhanced MVP**: MVP + US4 (Scoring) + US6 (Game Over)

**Generation Status**: ✅ Complete  
**Ready for Implementation**: ✅ Yes  
**Next Command**: Start with T001 or use parallel execution strategy

---

*Generated by `/speckit.tasks` on 2026-02-01 for CitySemaphores feature*
