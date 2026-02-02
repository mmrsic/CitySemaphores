# Implementation Plan: CitySemaphores - Traffic Light Simulation Game

**Branch**: `main` | **Date**: 2026-02-01 | **Spec**: `.specify/memory/spec.md`

**Input**: Feature specification from `.specify/memory/spec.md`

## Summary

CitySemaphores is a visually impressive cross-platform 2D traffic simulation game where players manually control traffic lights to optimize traffic flow and prevent collisions. The game features:

- **Grid-based city** with rectangular intersections and 4 independent traffic lights per intersection (N, S, E, W)
- **Vehicle spawning** at city borders with optimal routing via Dijkstra's algorithm
- **Directional occupancy** limiting one vehicle per direction on intersections, naturally capping collisions at 4 vehicles maximum
- **Collision detection** with additively increasing intersection blocking time (1 vehicle = 7.5s, 2 vehicles = 22.5s, 3 vehicles = 52.5s, 4 vehicles = 112.5s)
- **Scoring system** awarding base points for crossings plus distance bonus reduced by waiting time, encouraging efficient traffic management
- **Vehicle removal** after collision unblocking, with player keeping earned points before collision
- **Visual excellence** with smooth animations (30 FPS minimum, 60 FPS desirable), particle effects, glow effects, and modern UI

**Technical Approach**: Kotlin Multiplatform with Compose Multiplatform for cross-platform support, targeting Browser (P1), Android (P2), Linux Desktop (P3), with optional macOS and iOS support. Shared game logic in commonMain with platform-specific rendering and input handling.

## Technical Context

**Language/Version**: Kotlin 1.9+  
**Primary Dependencies**: 
  - Compose Multiplatform (UI framework)
  - kotlinx.coroutines (asynchronous operations)
  - kotlinx.serialization (configuration/saves)
  - Koin (optional DI)

**Storage**: LocalStorage/SharedPreferences for game state persistence (optional)  
**Testing**: kotlin.test (common), JUnit 5 (JVM), Kotest (optional)  
**Target Platforms**: 
  - Browser/Web (Kotlin/JS) - Priority 1
  - Android 5.0+ (API 21) - Priority 2
  - Linux Desktop (JVM + Flatpak) - Priority 3
  - macOS (JVM + .dmg) - Priority 4 (optional)
  - iOS 14+ - Priority 4 (optional)

**Project Type**: Multiplatform (Browser + Android + Desktop)  
**Performance Goals**: 
  - 60 FPS for animations and visual effects
  - 30+ FPS minimum during gameplay with 10+ simultaneous vehicles
  - Dijkstra routing < 100ms for grids up to 20×20
  - Visual feedback < 100ms for all player actions

**Constraints**: 
  - 80%+ code sharing in commonMain
  - Web performance optimized for modern browsers
  - Mobile performance optimized for mid-range Android devices
  - Visual quality must not compromise frame rate

**Scale/Scope**: 
  - Grid size: 10×10 to 20×20 intersections
  - Simultaneous vehicles: 10-50
  - Single-player local game
  - No network/multiplayer features in MVP

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ Compliance Status

**I. Modular Component Design**: ✅ PASS
- Game divided into clear components: City, Vehicle, TrafficLight, CityGraph, etc.
- Event-driven architecture with loose coupling
- Each component independently testable

**II. Test-First Development**: ✅ PASS
- TDD workflow planned: Tests → Approval → Implementation
- Unit tests in commonTest
- Platform-specific integration tests

**III. Performance-First**: ✅ PASS
- 60 FPS animation target defined
- Performance budgets established (Dijkstra < 100ms)
- Efficient algorithms planned (no O(n²) collision checks)

**IV. Clear Game Mechanics**: ✅ PASS
- Simple control: Click/touch to switch lights
- Immediate visual feedback for all actions
- Clear consequences: Accidents → Blocking → Point loss

**V. Extensibility**: ✅ PASS
- Configurable grid system
- Adjustable parameters (spawn rate, speed, etc.)
- Modular scoring system

**VI. Platform-First Development**: ✅ PASS
- Browser (P1) → Android (P2) → Desktop (P3) → iOS (P4)
- 80%+ code sharing target in commonMain
- Platform-specific only for rendering/input

**VII. Visual Excellence**: ✅ PASS
- Modern graphics with vibrant colors planned
- 30 FPS minimum, 60 FPS desirable with interpolation
- Particle effects, glow effects, visual feedback
- Cohesive design language

### 🚨 Constitution Violations

**None identified.** All core principles are satisfied by the current design.

## Project Structure

### Documentation (this feature)

```text
.specify/memory/
├── spec.md              # Feature specification (exists)
├── constitution.md      # Development principles (exists)
├── plan.md              # This file
├── research.md          # Technology research and evaluation
├── data-model.md        # Domain model and data structures
├── architecture.md      # System architecture and patterns
└── contracts/           # Component interfaces and contracts
    ├── game-loop.md
    ├── routing.md
    ├── collision.md
    ├── visual-effects.md
    └── state-management.md
```

### Source Code (repository root)

```text
CitySemaphores/
├── composeApp/                    # Compose Multiplatform application
│   ├── src/
│   │   ├── commonMain/           # Shared code (80%+ target)
│   │   │   ├── kotlin/
│   │   │   │   └── com/citysemaphores/
│   │   │   │       ├── domain/   # Business logic & models
│   │   │   │       │   ├── model/
│   │   │   │       │   │   ├── City.kt
│   │   │   │       │   │   ├── Intersection.kt
│   │   │   │       │   │   ├── TrafficLight.kt
│   │   │   │       │   │   ├── Vehicle.kt
│   │   │   │       │   │   ├── Route.kt
│   │   │   │       │   │   └── GameState.kt
│   │   │   │       │   ├── graph/
│   │   │   │       │   │   ├── CityGraph.kt
│   │   │   │       │   │   └── DijkstraRouter.kt
│   │   │   │       │   ├── collision/
│   │   │   │       │   │   └── CollisionDetector.kt
│   │   │   │       │   └── scoring/
│   │   │   │       │       └── ScoreCalculator.kt
│   │   │   │       ├── game/     # Game loop & logic
│   │   │   │       │   ├── GameEngine.kt
│   │   │   │       │   ├── VehicleSpawner.kt
│   │   │   │       │   └── GameTimer.kt
│   │   │   │       ├── ui/       # Compose UI (shared)
│   │   │   │       │   ├── screens/
│   │   │   │       │   │   ├── GameScreen.kt
│   │   │   │       │   │   └── MenuScreen.kt
│   │   │   │       │   ├── components/
│   │   │   │       │   │   ├── CityGridView.kt
│   │   │   │       │   │   ├── IntersectionView.kt
│   │   │   │       │   │   ├── VehicleView.kt
│   │   │   │       │   │   ├── ScoreDisplay.kt
│   │   │   │       │   │   └── VisualEffects.kt
│   │   │   │       │   └── theme/
│   │   │   │       │       ├── Color.kt
│   │   │   │       │       ├── Theme.kt
│   │   │   │       │       └── Typography.kt
│   │   │   │       ├── viewmodel/  # State management
│   │   │   │       │   ├── GameViewModel.kt
│   │   │   │       │   └── GameUiState.kt
│   │   │   │       └── util/
│   │   │   │           ├── Animation.kt
│   │   │   │           └── MathUtils.kt
│   │   │   └── resources/
│   │   ├── commonTest/           # Shared tests
│   │   │   └── kotlin/
│   │   │       └── com/citysemaphores/
│   │   │           ├── domain/
│   │   │           │   ├── DijkstraRouterTest.kt
│   │   │           │   ├── CollisionDetectorTest.kt
│   │   │           │   └── ScoreCalculatorTest.kt
│   │   │           ├── game/
│   │   │           │   └── GameEngineTest.kt
│   │   │           └── integration/
│   │   │               └── GameFlowTest.kt
│   │   ├── jsMain/               # Web-specific (P1)
│   │   │   └── kotlin/
│   │   │       └── com/citysemaphores/
│   │   │           ├── platform/
│   │   │           │   ├── CanvasRenderer.kt
│   │   │           │   └── WebInput.kt
│   │   │           └── Main.kt
│   │   ├── androidMain/          # Android-specific (P2)
│   │   │   └── kotlin/
│   │   │       └── com/citysemaphores/
│   │   │           ├── platform/
│   │   │           │   └── AndroidPlatform.kt
│   │   │           └── MainActivity.kt
│   │   ├── desktopMain/          # Desktop-specific (P3)
│   │   │   └── kotlin/
│   │   │       └── com/citysemaphores/
│   │   │           ├── platform/
│   │   │           │   └── DesktopPlatform.kt
│   │   │           └── Main.kt
│   │   └── iosMain/              # iOS-specific (P4, optional)
│   │       └── kotlin/
│   │           └── com/citysemaphores/
│   │               └── platform/
│   │                   └── IOSPlatform.kt
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
└── .gitignore
```

**Structure Decision**: 

**Multiplatform structure** selected because:
1. Game targets multiple platforms (Browser, Android, Desktop)
2. Significant shared logic (game engine, routing, collision, scoring)
3. Platform-specific needs limited to rendering and input
4. Compose Multiplatform supports declarative UI across all targets

The `composeApp/` module contains all source code organized by target:
- **commonMain**: 80%+ of code (game logic, domain models, shared UI)
- **jsMain**: Web-specific optimizations (Canvas rendering, browser APIs)
- **androidMain**: Android platform integration (Activity, Material Design)
- **desktopMain**: Desktop platform integration (JVM, native menus)
- **iosMain**: iOS platform integration (optional, future)

## Complexity Tracking

> **No constitution violations identified - this section intentionally left empty.**

The design adheres to all constitution principles without requiring exceptions or complexity justifications.

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────┐
│         Compose UI Layer (Platform)             │
│  WebCanvas │ Android │ Desktop │ iOS (opt)      │
└─────────────────────────────────────────────────┘
                      ↑
                      │ UI State (StateFlow)
                      │
┌─────────────────────────────────────────────────┐
│              GameViewModel (MVI)                │
│         - GameUiState (immutable)               │
│         - User Intents                          │
│         - StateFlow<GameUiState>                │
└─────────────────────────────────────────────────┘
                      ↑
                      │ Events & Commands
                      │
┌─────────────────────────────────────────────────┐
│              GameEngine (Core Logic)            │
│  - Game Loop (Update → Render cycle)            │
│  - VehicleSpawner                               │
│  - CollisionDetector                            │
│  - ScoreCalculator                              │
└─────────────────────────────────────────────────┘
                      ↑
                      │ Domain Operations
                      │
┌─────────────────────────────────────────────────┐
│            Domain Models (Common)               │
│  City │ Intersection │ Vehicle │ TrafficLight   │
│  Route │ CityGraph │ GameState                  │
└─────────────────────────────────────────────────┘
```

### Key Architectural Patterns

1. **MVI (Model-View-Intent)**: Unidirectional data flow
   - View sends Intents to ViewModel
   - ViewModel updates immutable State
   - View reacts to State changes

2. **Event-Driven**: Loose coupling via Kotlin Flow
   - CollisionEvent, ScoreEvent, SpawnEvent
   - Components communicate through event streams

3. **Repository Pattern**: Data abstraction (for future persistence)
   - GameStateRepository (optional for save/load)

4. **Strategy Pattern**: Interchangeable algorithms
   - RoutingStrategy (Dijkstra default, A* future)

5. **Component Pattern**: Game entities as composable objects
   - Vehicle, Intersection, TrafficLight as data classes

## Technology Stack

### Core Technologies

| Component | Technology | Version | Justification |
|-----------|-----------|---------|---------------|
| Language | Kotlin | 1.9+ | Multiplatform support, null-safety, coroutines |
| UI Framework | Compose Multiplatform | Latest stable | Declarative UI, cross-platform, modern |
| Build System | Gradle | 8.x | Standard for Kotlin/JVM projects |
| Coroutines | kotlinx.coroutines | 1.7+ | Async operations, game loop |
| Serialization | kotlinx.serialization | 1.6+ | Config, save data (optional) |
| Testing | kotlin.test | Built-in | Cross-platform testing |
| DI | Koin (optional) | 3.5+ | Simple, KMP-compatible |

### Platform-Specific Technologies

| Platform | Additional Tech | Purpose |
|----------|----------------|---------|
| Web/JS | Kotlin/JS IR, Canvas API | High-performance rendering |
| Android | Jetpack Compose, Material 3 | Native Android UI |
| Desktop | Compose Desktop, JVM | Native desktop experience |
| iOS (opt) | Compose iOS (Beta) | Native iOS UI |

## Performance Strategy

### Frame Rate Budget

**60 FPS Target** = 16.67ms per frame

| Operation | Budget | Strategy |
|-----------|--------|----------|
| Input Processing | 1ms | Immediate event handling |
| Game Logic Update | 5ms | Efficient algorithms, minimal allocations |
| Collision Detection | 2ms | Spatial partitioning, early exit |
| Dijkstra Routing | 10ms (async) | Background coroutine, cached routes |
| Rendering | 8ms | Compose recomposition optimization |
| Visual Effects | 1ms | Lightweight particle systems |

### Optimization Strategies

1. **Spatial Partitioning**: Grid-based collision detection (O(1) per vehicle)
2. **Object Pooling**: Reuse vehicle instances to reduce GC pressure
3. **Dirty Flag Pattern**: Only update changed intersections
4. **Route Caching**: Dijkstra results cached for common paths
5. **Recomposition Optimization**: Stable keys, immutable parameters
6. **Platform-Specific**: Canvas optimization (Web), hardware acceleration (Android)

## Visual Effects Implementation

### Animation System

```kotlin
// Smooth interpolation for vehicles
data class AnimatedPosition(
    val current: Point,
    val target: Point,
    val progress: Float // 0.0 to 1.0
)

fun lerp(start: Point, end: Point, t: Float): Point
```

### Particle System

```kotlin
interface ParticleEffect {
    fun emit(position: Point, count: Int)
    fun update(deltaTime: Float)
    fun render(canvas: Canvas)
}

class CollisionParticles : ParticleEffect
class CelebrationParticles : ParticleEffect
```

### Visual Effects Manager

```kotlin
class VisualEffectsManager {
    fun playCollisionEffect(position: Point)
    fun playCelebrationEffect(position: Point, score: Int)
    fun playTransitionEffect(light: TrafficLight)
    fun update(deltaTime: Float)
}
```

## Development Phases

### Phase 0: Project Setup & Research ✓
- ✅ Kotlin Multiplatform project structure
- ✅ Compose Multiplatform dependencies
- ✅ Build configuration for all targets
- ✅ Basic "Hello World" running on Web + Android

### Phase 1: Core Game Logic (Week 1-2)
- [ ] Domain models (City, Intersection, Vehicle, etc.)
- [ ] CityGraph with Dijkstra implementation
- [ ] GameEngine with game loop
- [ ] VehicleSpawner
- [ ] CollisionDetector
- [ ] ScoreCalculator
- [ ] Unit tests for all core logic

### Phase 2: Basic UI (Week 2-3)
- [ ] GameViewModel with MVI pattern
- [ ] CityGridView rendering
- [ ] IntersectionView with traffic lights
- [ ] VehicleView with basic rendering
- [ ] Input handling (click/touch)
- [ ] ScoreDisplay UI
- [ ] GameOverScreen with statistics
- [ ] Test on Web (P1)

### Phase 3: Visual Polish (Week 3-4)
- [ ] Animation system with interpolation
- [ ] Particle system for effects
- [ ] Glow effects for traffic lights
- [ ] Celebration effects
- [ ] Collision visual feedback
- [ ] Blocked intersection indicators
- [ ] Theme and color palette
- [ ] 60 FPS optimization

### Phase 4: Platform Support (Week 4-5)
- [ ] Android build and testing
- [ ] Android-specific optimizations
- [ ] Desktop (Linux) build
- [ ] Flatpak packaging
- [ ] Performance profiling all platforms

### Phase 5: Polish & Testing (Week 5-6)
- [ ] User testing and feedback
- [ ] Bug fixes
- [ ] Performance tuning
- [ ] Accessibility improvements
- [ ] Documentation
- [ ] Release preparation

## Risk Assessment

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Compose Multiplatform web performance | High | Medium | Canvas fallback, profiling early |
| Particle effects slow on web | Medium | Medium | Simplified effects for web, platform detect |
| Dijkstra performance with large grids | Medium | Low | Limit grid size, async routing, caching |
| Cross-platform input differences | Low | Medium | Abstract input layer, early testing |
| iOS Compose stability (beta) | High | High | Mark iOS as optional, focus on P1-P3 |

## Success Metrics

### Technical Metrics
- ✅ 60 FPS on Desktop/Android, 30+ FPS on Web
- ✅ <100ms Dijkstra routing for 20×20 grid
- ✅ 80%+ code sharing in commonMain
- ✅ Zero crashes in 1-hour play session
- ✅ Build time <2 minutes for incremental

### Quality Metrics
- ✅ 70%+ test coverage for core logic
- ✅ All tests passing on JVM and JS
- ✅ Zero compiler warnings
- ✅ KDoc for all public APIs

### User Experience Metrics
- ✅ 80%+ positive feedback on visuals
- ✅ Players understand controls without tutorial
- ✅ Visual feedback <100ms for all actions
- ✅ Smooth gameplay with no stuttering

## Next Steps

1. **Immediate**: Begin Phase 1 implementation
   - Set up Kotlin Multiplatform project
   - Implement domain models
   - Implement Dijkstra routing
   - Write unit tests

2. **This Week**: Complete core game logic
   - Game engine and loop
   - Collision detection
   - Scoring system
   - Test coverage >70%

3. **Next Week**: Basic UI implementation
   - MVI architecture
   - City grid rendering
   - Traffic light controls
   - Deploy to web for testing

4. **Documentation**: Create supporting docs
   - `research.md`: Technology evaluation
   - `data-model.md`: Domain model details
   - `architecture.md`: System architecture
   - `contracts/`: Component interfaces

---

**Plan Status**: ✅ Complete and ready for implementation  
**Constitution Compliance**: ✅ All principles satisfied  
**Next Command**: `/speckit.tasks` to generate actionable task list
