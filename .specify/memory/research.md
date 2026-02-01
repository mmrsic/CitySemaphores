# Technology Research: CitySemaphores

**Date**: 2026-02-01  
**Status**: Complete  
**Decision**: Kotlin Multiplatform + Compose Multiplatform

## Overview

This document evaluates technology choices for CitySemaphores, a cross-platform 2D traffic simulation game targeting Browser (P1), Android (P2), and Desktop (P3).

## Requirements Summary

### Functional Requirements
- Cross-platform support (Web, Android, Desktop)
- 2D rendering with smooth animations (60 FPS target)
- Game loop with real-time updates
- Visual effects (particles, glow, transitions)
- Touch and mouse input support

### Non-Functional Requirements
- 80%+ code sharing across platforms
- Modern, maintainable codebase
- Strong type safety
- Good performance on mid-range devices
- Active community and tooling support

## Technology Evaluation

### Option 1: Kotlin Multiplatform + Compose Multiplatform ✅ SELECTED

**Pros:**
- ✅ Native performance on all platforms
- ✅ Single codebase for UI and logic
- ✅ Modern declarative UI (Compose)
- ✅ Strong type safety with Kotlin
- ✅ Excellent IDE support (IntelliJ/Android Studio)
- ✅ Active development and community
- ✅ Null-safety built into language
- ✅ Coroutines for async operations
- ✅ Mature Android support
- ✅ Growing web/desktop support

**Cons:**
- ⚠️ Compose for Web still maturing
- ⚠️ iOS support in beta (acceptable for P4)
- ⚠️ Learning curve for Compose
- ⚠️ Web bundle size larger than pure JS

**Verdict**: Best fit for requirements. Modern, type-safe, cross-platform with good performance.

---

### Option 2: LibGDX (Kotlin/Java)

**Pros:**
- ✅ Proven game framework
- ✅ Excellent 2D rendering performance
- ✅ Cross-platform (Desktop, Web, Android, iOS)
- ✅ Mature ecosystem
- ✅ Good documentation

**Cons:**
- ❌ Older UI patterns (not declarative)
- ❌ More boilerplate code
- ❌ Less modern than Compose
- ❌ Scene2D UI system dated
- ❌ Manual memory management concerns

**Verdict**: Good alternative but less modern than Compose Multiplatform.

---

### Option 3: Unity (C#)

**Pros:**
- ✅ Powerful game engine
- ✅ Excellent visual editor
- ✅ Strong 2D/3D support
- ✅ Asset store
- ✅ Cross-platform export

**Cons:**
- ❌ Overkill for 2D grid game
- ❌ Large runtime size
- ❌ C# not Kotlin
- ❌ Complex build pipeline
- ❌ Licensing costs for revenue
- ❌ Not aligned with existing tech choice

**Verdict**: Too heavy for requirements. Not suitable.

---

### Option 4: Web-Only (TypeScript + React/Svelte)

**Pros:**
- ✅ Fast web development
- ✅ Small bundle size
- ✅ Excellent browser support
- ✅ Rich ecosystem

**Cons:**
- ❌ Web-only, no native Android/Desktop
- ❌ TypeScript not Kotlin
- ❌ Multiple codebases for platforms
- ❌ Web performance limitations

**Verdict**: Doesn't meet multiplatform requirement.

---

### Option 5: Flutter (Dart)

**Pros:**
- ✅ Cross-platform support
- ✅ Good performance
- ✅ Declarative UI
- ✅ Growing community

**Cons:**
- ❌ Dart not Kotlin
- ❌ Web support still maturing
- ❌ Desktop support newer
- ❌ Not aligned with existing tech choice

**Verdict**: Good alternative but Dart is less familiar.

## Selected Technology: Kotlin Multiplatform + Compose Multiplatform

### Justification

1. **Meets All Requirements**: Cross-platform, modern UI, good performance
2. **Code Sharing**: 80%+ target achievable with commonMain
3. **Type Safety**: Kotlin's null-safety and type system reduce bugs
4. **Modern Patterns**: Declarative UI, coroutines, flow
5. **Tooling**: Excellent IDE support with IntelliJ IDEA
6. **Community**: Active Kotlin community, growing Compose Multiplatform adoption
7. **Future-Proof**: JetBrains committed to Compose Multiplatform evolution

### Technology Stack Details

#### Core Technologies

**Kotlin 1.9+**
- Modern language with null-safety
- Coroutines for async operations
- Data classes for domain models
- Extension functions for utilities
- Sealed classes for state management

**Compose Multiplatform**
- Declarative UI across platforms
- Built-in animation support
- Material Design 3 components
- Custom Canvas for advanced rendering
- Recomposition optimization

**kotlinx.coroutines 1.7+**
- Game loop implementation
- Async Dijkstra routing
- Event stream processing
- Platform-agnostic async

**kotlinx.serialization 1.6+**
- Game state serialization
- Configuration files
- Save/load functionality (future)

#### Platform-Specific

**Web (Kotlin/JS IR)**
- Canvas API for rendering
- DOM manipulation if needed
- Browser APIs (localStorage, etc.)
- WebGL future consideration

**Android**
- Jetpack Compose
- Material Design 3
- Android SDK APIs
- Hardware acceleration

**Desktop (JVM)**
- Compose Desktop
- Skia rendering engine
- File system access
- Native menu integration

## Performance Considerations

### Web Performance

**Challenge**: JavaScript runtime performance
**Mitigation**:
- Kotlin/JS IR compiler optimizations
- Canvas rendering instead of DOM
- RequestAnimationFrame for game loop
- Minimize allocations in hot paths

**Target**: 30 FPS minimum, 60 FPS desirable

### Android Performance

**Challenge**: Mid-range device performance
**Mitigation**:
- Hardware acceleration enabled
- Efficient Compose recomposition
- Object pooling for vehicles
- Spatial partitioning for collisions

**Target**: 60 FPS on mid-range devices

### Desktop Performance

**Challenge**: None expected
**Mitigation**: N/A
**Target**: 60 FPS easily achievable

## Rendering Strategy

### Compose Canvas vs. Custom OpenGL

**Decision**: Compose Canvas (native)

**Rationale**:
- Sufficient for 2D grid rendering
- Simpler implementation
- Cross-platform without platform-specific code
- Good performance for game requirements
- Can optimize later if needed

### Animation Implementation

**Approach**: Compose Animation APIs + Custom Interpolation

```kotlin
// Smooth position transitions
val animatedPosition by animateFloatAsState(
    targetValue = targetPosition,
    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
)

// Custom interpolation for vehicles
fun lerp(start: Float, end: Float, t: Float): Float = 
    start + (end - start) * t
```

### Particle Effects

**Approach**: Custom lightweight particle system

```kotlin
data class Particle(
    var position: Point,
    var velocity: Vector,
    var lifetime: Float,
    var color: Color
)

class ParticleSystem {
    private val particles = mutableListOf<Particle>()
    
    fun emit(position: Point, count: Int) { /* ... */ }
    fun update(deltaTime: Float) { /* ... */ }
    fun render(canvas: Canvas) { /* ... */ }
}
```

## State Management

### MVI Pattern

**Choice**: MVI (Model-View-Intent) over MVVM

**Rationale**:
- Unidirectional data flow
- Predictable state updates
- Easy to test
- Good fit for game state
- Immutable state with data classes

**Implementation**:

```kotlin
data class GameUiState(
    val city: City,
    val vehicles: List<Vehicle>,
    val score: Int,
    val isPlaying: Boolean
)

sealed interface GameIntent {
    data class SwitchTrafficLight(val intersection: Intersection) : GameIntent
    object PauseGame : GameIntent
    object ResumeGame : GameIntent
}

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState.initial())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    fun handleIntent(intent: GameIntent) { /* ... */ }
}
```

## Testing Strategy

### Unit Testing

**Framework**: kotlin.test (multiplatform)

**Coverage Target**: 70%+ for core logic

**Focus Areas**:
- Dijkstra routing algorithm
- Collision detection
- Scoring calculation
- Vehicle movement
- Traffic light state management

### Integration Testing

**Framework**: kotlin.test + platform tests

**Focus Areas**:
- Game flow (spawn → route → move → collision)
- UI state synchronization
- Event propagation

### Platform Testing

**Web**: Browser testing (Chrome, Firefox, Safari)
**Android**: Emulator + physical device
**Desktop**: Linux, macOS builds

## Build Configuration

### Gradle Setup

```kotlin
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "citysemaphores.js"
            }
        }
        binaries.executable()
    }
    
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    jvm("desktop") {
        jvmToolchain(17)
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val jsMain by getting
        val androidMain by getting
        val desktopMain by getting
    }
}
```

## Alternative Approaches Considered

### For Visual Effects

**Option A**: Use platform-specific APIs (WebGL, OpenGL)
- **Rejected**: Too complex, breaks code sharing goal

**Option B**: Use existing particle library
- **Rejected**: Few KMP-compatible libraries available

**Option C**: Custom lightweight system ✅ SELECTED
- **Reason**: Full control, optimized, multiplatform

### For Routing

**Option A**: A* algorithm
- **Considered**: More optimal for complex routing
- **Deferred**: Dijkstra sufficient for grid-based city, can upgrade later

**Option B**: Precomputed routes
- **Rejected**: Less flexible, large memory footprint for 20×20 grid

**Option C**: Dijkstra ✅ SELECTED
- **Reason**: Simple, proven, fast enough (<100ms target)

## Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| Compose Web performance | Early profiling, Canvas optimization, fallback strategies |
| Bundle size for Web | Tree-shaking, code splitting, lazy loading |
| Cross-platform bugs | Extensive testing, CI/CD for all platforms |
| Animation performance | Object pooling, efficient recomposition, profiling |
| Platform API differences | Abstract platform layer, expect/actual pattern |

## Conclusion

**Kotlin Multiplatform + Compose Multiplatform** is the optimal choice for CitySemaphores, balancing:
- Modern development experience
- Cross-platform requirements
- Performance goals
- Code maintainability
- Future extensibility

The stack is mature enough for production use, with active development ensuring long-term viability.

---

**Research Status**: ✅ Complete  
**Decision**: ✅ Approved for implementation  
**Next**: Proceed with Phase 1 - Core Game Logic
