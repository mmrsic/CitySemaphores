# CitySemaphores Constitution

## Core Principles

### I. Modular Component Design
The game is divided into clearly separated, reusable components:
- Each component (City, Vehicle, TrafficLight, etc.) has a clearly defined responsibility
- Components are independently testable
- Loose coupling through event system for communication between components

### II. Test-First Development (NON-NEGOTIABLE)
Test-Driven Development is mandatory:
- Unit tests for every core component (graph algorithms, collision detection, scoring system)
- Integration tests for component interaction (Vehicle + TrafficLight + Collision)
- Tests must be written and approved by the user before implementation begins
- Red-Green-Refactor cycle is strictly enforced

### III. Performance-First
The game must run smoothly:
- Minimum 30 FPS with 10+ simultaneous vehicles
- 60 FPS desirable if achievable without significant extra development effort
- Dijkstra route calculation < 100ms for grid up to 20x20
- Efficient collision detection (no O(n²) comparisons)
- Optimized rendering pipeline

### IV. Clear Game Mechanics
The game design follows the principle "Easy to Learn, Hard to Master":
- Intuitive controls (mouse click/touch to switch)
- Immediate visual feedback for all actions
- Clear consequences (accidents → blocking → point loss)
- Gradual difficulty increase through increasing traffic

### V. Extensibility
The system is prepared for future features:
- Configurable grid system (adjustable size)
- Adjustable parameters (spawn rate, blocking time, speed)
- Modular scoring system for additional metrics
- Foundation for different difficulty levels

### VI. Platform-First Development
Platform support by priority:
- **Browser (P1)**: Web performance and browser compatibility have highest priority
- **Android (P2)**: Native Android experience with Material Design
- **Desktop (P3)**: Linux Flatpak, then macOS
- **iOS (P4)**: Optional, if resources available
- **Shared Logic**: Maximum code sharing in commonMain (80%+ goal)
- **Platform-Specific**: Only UI rendering and input handling platform-specific

### VII. Visual Excellence
The game must be visually impressive and engaging:
- **Modern Graphics**: Use contemporary design language with clean lines, vibrant colors, and polished visuals
- **Smooth Animations**: All transitions and movements must be fluid with interpolation (60 FPS target)
- **Visual Feedback**: Every player action and game event must have clear, immediate visual response
- **Effects & Polish**: Particle effects, glow effects, celebrations, and visual indicators enhance game feel
- **Cohesive Design**: Consistent color scheme, typography, and design language across all elements
- **Accessibility**: Good color contrast and visual clarity for all players
- **Performance Balance**: Visual quality must not compromise frame rate or responsiveness

## Technical Standards

### Kotlin Multiplatform Best Practices
- **Kotlin Idioms**: Use Kotlin-specific features (data classes, sealed classes, extension functions)
- **Null-Safety**: Strict use of null-safety, no !! except with justification
- **Immutability**: Prefer `val` over `var`, immutable data structures
- **Coroutines**: Asynchronous operations with suspend functions and Flow
- **Common Code First**: Maximum code sharing in commonMain, platform-specific only when necessary
- **Clean Code**: Meaningful names, short functions, Single Responsibility Principle

### Compose Multiplatform Patterns
- **MVI/MVVM**: Model-View-Intent or Model-View-ViewModel with StateFlow
- **Composables**: Small, reusable Composable Functions
- **State Management**: Immutable State with `remember`, `mutableStateOf`, StateFlow
- **Side Effects**: Correct use of LaunchedEffect, DisposableEffect, SideEffect
- **Performance**: Minimize recomposition through stable parameters and keys

### Architecture Patterns
- **Event-Driven**: Loose coupling through Kotlin Flow for events (CollisionEvent, ScoreEvent, etc.)
- **Strategy Pattern**: Interchangeable algorithms (e.g., different routing strategies)
- **Repository Pattern**: Abstract data access for platform independence
- **Use Cases**: Encapsulate business logic in reusable use cases

### Code Quality Gates
- No compiler warnings
- Code Coverage: Minimum 70% for core logic (commonTest)
- KDoc for all public APIs
- Consistent formatting (Kotlin Official Style Guide)
- Platform-specific code must be documented and justified

### Platform-Specific Guidelines
- **Web (Kotlin/JS)**: Performance optimization for browsers, efficient Canvas rendering
- **Android**: Material Design 3 Guidelines, Jetpack Compose Best Practices
- **Desktop**: Native look & feel, keyboard shortcuts
- **iOS (optional)**: iOS Human Interface Guidelines, native navigation

### Visual Design Guidelines
- **Animation Timing**: Use appropriate easing functions (ease-in-out for transitions)
- **Color Palette**: Define consistent primary, secondary, and accent colors
- **Visual Hierarchy**: Clear distinction between interactive and non-interactive elements
- **Feedback Timing**: Immediate response (<50ms), effects visible within 100ms
- **Particle Systems**: Lightweight, performant particle effects for celebrations and collisions
- **Glow/Emission**: Use sparingly for emphasis (traffic lights, active elements)
- **Interpolation**: Linear interpolation for movement, smooth curves for UI transitions

## Development Workflow

### Feature Implementation
1. **Specification**: Document feature in spec.md
2. **Design**: Define class design and contracts (commonMain first)
3. **Write Tests**: Create unit tests (commonTest) and platform tests
4. **Review Tests**: User approves tests
5. **Implementation**: Write code until tests are green (commonMain → platform-specific)
6. **Refactoring**: Optimize code, remove duplicates
7. **Validation**: Performance tests on all target platforms, manual gameplay tests

### Multiplatform Development
- **Start with Common**: Implement logic first in commonMain
- **expect/actual**: Use expect/actual only when platform-specific APIs are required
- **Test on Web First**: Browser is the main target platform, test there first
- **Android Second**: After web validation, test on Android
- **Desktop Last**: Validate desktop targets (Linux/macOS) at the end

### Quality Assurance
- Every commit must compile on all configured platforms
- Tests must run on JVM and JS
- Manual tests on web browsers (Chrome, Firefox, Safari)
- Manual tests on Android emulator or physical device
- Performance profiling with Browser DevTools and Android Profiler

### Documentation
- README.md with setup instructions for all platforms
- Inline comments (KDoc) for complex algorithms (Dijkstra, collision)
- Platform-specific documentation for expect/actual implementations
- Architecture Decision Records (ADR) for important design decisions

## Governance

This constitution takes precedence over all other practices:
- All implementations must follow these principles
- Deviations must be documented and justified
- Complexity must be justified by concrete benefits
- When in doubt: Choose the simplest solution (KISS principle)

**Version**: 2.1.0 | **Ratified**: 2026-02-01 | **Last Amended**: 2026-02-01
