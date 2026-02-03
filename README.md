# CitySemaphores - Traffic Light Simulation Game

> 🚦 **Current Status: Phase 3 Prototype - Traffic Light Control** 🚦  
> A working prototype demonstrating manual traffic light control at intersections.
> 
> **What's implemented:**
> - ✅ 5x5 City Grid with Intersections
> - ✅ 4 Independent Traffic Lights per Intersection (N, S, E, W)
> - ✅ Manual Traffic Light Control (Click to Toggle)
> - ✅ Visual Feedback (Hover Effects, Color Changes)
> - ✅ Intersection Blocking System (Ready for collision detection)
> - ✅ Game Controls (Start/Pause/Stop)
> - ✅ Statistics Panel
>
> **Try the prototype now:**
> ```bash
> # Desktop (Fastest)
> ./gradlew :composeApp:run
> 
> # Or use IntelliJ IDEA Run Configuration: "Desktop - Run"
> ```
> 
> **How to use:**
> 1. Click "Start" to initialize the game
> 2. Click on any traffic light (colored circles) to toggle Red ↔ Green
> 3. Observe hover effects and visual feedback
> 4. All 4 directions are independently controllable

---

A visually impressive cross-platform 2D traffic simulation game where the player manually controls traffic lights.
## Game Concept
- **City Layout**: Grid-based road network with rectangular intersections
- **Traffic Light Control**: Manual switching (Red/Green) via mouse click/touch
- **Vehicles**: Appear at city borders, follow optimal routes (Dijkstra)
- **Collisions**: Intersections are temporarily blocked in case of accidents
- **Scoring System**: +1 per intersection, doubled at destination
- **Visual Excellence**: Modern graphics, smooth animations, particle effects, and polished UI
## Technology
- **Language**: Kotlin (Kotlin Multiplatform)
- **UI Framework**: Compose Multiplatform (Jetpack Compose)
- **Architecture**: MVI Pattern with StateFlow
- **Graphics**: 60 FPS animations, visual effects, smooth interpolation
### Supported Platforms
1. ✅ **Browser (Web - JS/IR)** - Highest Priority
2. ✅ **Android APK** - Second Priority
3. ✅ **Desktop (JVM)** - Linux, macOS, Windows
4. 🔄 **Linux (Flatpak)** - Desirable
5. ⚪ **iOS** - Optional

> **Note**: WebAssembly (wasmJs) target has been temporarily removed to avoid compilation conflicts with the JS/IR target. It can be re-added later with a separate entry point if needed.
## Visual Features
- **Smooth Animations**: 60 FPS transitions and movements
- **Particle Effects**: Collision effects, celebration sparkles
- **Glow Effects**: Enhanced traffic light visibility
- **Visual Feedback**: Immediate response to all player actions
- **Modern Design**: Clean lines, vibrant colors, cohesive color scheme

## Getting Started

### Prerequisites
- JDK 11 or higher
- For Android: Android SDK (automatically downloaded by Gradle)
- For Web: Node.js (automatically managed by Kotlin/JS Gradle plugin)

### Running the Application

> **💡 IntelliJ IDEA Users**: Pre-configured run configurations are available in the project!  
> Look for these configurations in the run menu (top right):
> - **Web - JS Browser Development** / **Web - JS Browser Production**
> - **Android - Build Debug APK** / **Android - Install Debug**
> - **Desktop - Run** / **Desktop - Package Distribution**
> - **Tests - All** / **Tests - JS** / **Tests - Desktop** / **Tests - Android Unit**
> - **Clean Build**

#### 🌐 Web (Browser) - JS Target
Run in development mode with live reload:
```bash
./gradlew jsBrowserDevelopmentRun
```

Build for production:
```bash
./gradlew jsBrowserProductionWebpack
```
Output files will be in `composeApp/build/dist/js/productionExecutable/`

Open `index.html` in your browser from the build output directory.

#### 🤖 Android
Build debug APK:
```bash
./gradlew :composeApp:assembleDebug
```
The APK will be located at: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

Install directly to connected device:
```bash
./gradlew :composeApp:installDebug
```

Run on connected device/emulator:
```bash
./gradlew :composeApp:installDebug
adb shell am start -n com.citysemaphores/.MainActivity
```

#### 🖥️ Desktop (JVM)
Run on desktop:
```bash
./gradlew :composeApp:run
```

Create distributable package:
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```
- **Linux**: Creates `.deb` package in `composeApp/build/compose/binaries/main/deb/`
- **macOS**: Creates `.dmg` package in `composeApp/build/compose/binaries/main/dmg/`
- **Windows**: Creates `.msi` installer in `composeApp/build/compose/binaries/main/msi/`

#### 🧪 Running Tests
Run all tests:
```bash
./gradlew test
```

Run tests for specific platform:
```bash
./gradlew jsTest           # JavaScript tests
./gradlew desktopTest      # Desktop tests
./gradlew testDebugUnitTest # Android unit tests
```

### Clean Build
If you encounter build issues:
```bash
./gradlew clean
./gradlew build
```

## Documentation
The complete specification can be found in:
- **Feature Spec**: `.specify/memory/spec.md`
- **Constitution**: `.specify/memory/constitution.md`
## Status
**Current**: Specification phase completed with visual excellence requirements
**Next Steps**: Planning, Tasks, Implementation
