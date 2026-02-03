# CitySemaphores - Phase 3 Prototype Guide

## 🚦 Was ist implementiert?

Der aktuelle Prototyp demonstriert die **manuelle Ampelsteuerung** (Phase 3: User Story 1).

### Funktionen:

1. **5×5 Stadt-Grid**
   - 25 Kreuzungen mit jeweils 4 Ampeln
   - Jede Richtung (Nord, Süd, Ost, West) hat eine unabhängige Ampel

2. **Interaktive Ampelsteuerung**
   - ✅ Klick auf Ampel → Umschalten zwischen Rot und Grün
   - ✅ Hover-Effekt → Ampel wird größer
   - ✅ Visuelles Feedback → Weiße Markierung bei Hover
   - ✅ Shadow-Effekt bei Interaktion

3. **Spielsteuerung**
   - Start-Button → Initialisiert das Spiel
   - Pause-Button → (Vorbereitet für Simulation)
   - Stop-Button → Beendet das Spiel
   - Score-Anzeige → (Bereit für Fahrzeuglogik)

4. **Statistik-Panel**
   - Fahrzeuge gespawnt
   - Fahrzeuge abgeschlossen
   - Kollisionen
   - Gesamte Kreuzungen passiert
   - Spielzeit

5. **Blockierungs-System**
   - ⚠️ Vorbereitet für Kollisions-Erkennung (Phase 5)
   - Timer für Sperrzeit
   - Visuelle Warnung (rote Färbung)
   - Alle Ampeln werden rot bei Blockierung

## 🎮 Wie Sie den Prototyp starten:

### Option 1: Desktop (Empfohlen - am schnellsten)

**Mit IntelliJ IDEA:**
- Öffnen Sie die Run-Konfiguration: **"Desktop - Run Prototype"**
- Klicken Sie auf den grünen Play-Button

**Mit Terminal:**
```bash
./gradlew :composeApp:run
```

### Option 2: Web Browser (Development Mode)

**Mit IntelliJ IDEA:**
- Öffnen Sie die Run-Konfiguration: **"Web - Browser Development"**
- Klicken Sie auf den grünen Play-Button
- Der Browser öffnet sich automatisch auf `http://localhost:8080`

**Mit Terminal:**
```bash
./gradlew :composeApp:jsBrowserDevelopmentRun
```

### Option 3: Android

**Voraussetzungen:**
- Android-Gerät oder Emulator verbunden
- USB-Debugging aktiviert

**Mit Terminal:**
```bash
./gradlew :composeApp:installDebug
```

## 🎨 Benutzeroberfläche

```
┌────────────────────────────────────────────┐
│  City Semaphores          Score: 0         │
│  [Start] [Pause] [Stop]                    │
└────────────────────────────────────────────┘

┌────────────────────────────────────────────┐
│                                            │
│         🚦 🚦 🚦 🚦 🚦                     │
│         🚦 🚦 🚦 🚦 🚦                     │
│         🚦 🚦 🚦 🚦 🚦    5×5 Grid        │
│         🚦 🚦 🚦 🚦 🚦                     │
│         🚦 🚦 🚦 🚦 🚦                     │
│                                            │
│    Klicken Sie auf Ampeln zum Umschalten  │
└────────────────────────────────────────────┘

┌────────────────────────────────────────────┐
│  Statistics                                │
│  Vehicles: 0  Completed: 0  Collisions: 0 │
│  Game Time: 0.0s                           │
└────────────────────────────────────────────┘
```

## 🧪 Tests ausführen

### Desktop Tests (JVM)
```bash
./gradlew :composeApp:desktopTest
```
oder Run-Konfiguration: **"Tests - Desktop Unit Tests"**

### JS Tests (Node.js)
```bash
./gradlew :composeApp:jsNodeTest
```
oder Run-Konfiguration: **"Tests - JS Node Tests"**

### Alle Tests
```bash
./gradlew test
```

## 📊 Test-Abdeckung (Phase 3)

- ✅ **TrafficLightTest**: 6 Unit Tests
  - State toggle (Red ↔ Green)
  - canPass() Logik
  - setGreen/setRed Funktionen

- ✅ **IntersectionTest**: 15 Unit Tests
  - Initialisierung
  - canVehiclePass() mit Ampelstatus
  - Directional Occupancy (1 Fahrzeug pro Richtung)
  - Blockierung mit ADDITIVER Sperrzeit (7.5s, 22.5s, 52.5s, 112.5s)
  - Timer-Updates und Entsperrung

- ✅ **TrafficLightSwitchingTest**: 4 Integration Tests
  - ViewModel Intent-Handling
  - Ampel-Toggle über UI
  - Mehrfache Ampeln gleichzeitig setzen

**Gesamt: 25 Tests** (alle bestehen ✅)

## 🔧 Technische Details

### Architektur
- **MVI Pattern**: GameViewModel → GameUiState → GameScreen
- **Immutable State**: Alle Zustandsänderungen über copy()
- **StateFlow**: Reaktive UI-Updates
- **Compose Multiplatform**: 100% geteilter UI-Code

### Domain Model
- `Direction` - Enum für Himmelsrichtungen
- `GridPosition` - Diskrete Grid-Koordinaten
- `TrafficLightState` - RED | GREEN
- `TrafficLight` - Single directional light
- `Intersection` - 4 lights + blocking logic

### UI Components
- `IntersectionView` - Kreuzung mit 4 Ampeln
- `GameScreen` - Haupt-Spielansicht
- `CitySemaphoresTheme` - Light/Dark Theme

## 🚀 Was kommt als Nächstes?

Die nächsten Phasen sind bereits in `tasks.md` definiert:

- **Phase 4**: Vehicle Spawning & Routing (Dijkstra)
- **Phase 5**: Collision Detection & Blocking
- **Phase 6**: Scoring System
- **Phase 7**: Game Over Condition (Gridlock)
- **Phase 8**: Visual Effects & Polish

## 🐛 Bekannte Einschränkungen

- Noch keine Fahrzeuge (kommt in Phase 4)
- Noch keine Kollisionserkennung (kommt in Phase 5)
- Score-System nicht aktiv (kommt in Phase 6)
- Pause-Button hat noch keine Funktion (braucht Game Loop)

## 📝 Hinweise

- **Performance**: 60 FPS auf Desktop, ~30-60 FPS im Browser
- **Browser-Kompatibilität**: Chrome, Firefox, Safari, Edge (moderne Versionen)
- **Touch-Support**: Funktioniert auf Touch-Screens (Android, iOS später)
- **Responsive**: Grid passt sich Fenster-Größe an

---

**Viel Spaß beim Testen! 🎮**
