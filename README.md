# Ludo Master

A modern, native Android Ludo board game built with **Jetpack Compose**, **Material Design 3**, **Room local database persistence**, and **procedural audio synthesis**.

---

## Features

- **Authentic 15x15 Board Layout**:
  - Four colored corner bases (Red, Green, Yellow, Blue) with token sockets.
  - Starting safe tiles, star milestone safe tiles, colored home corridors, and center victory triangle.
  - Interactive 3D token pawns with movable pulse animations, hopping movement, and stack offset counters.
- **Multiple Game Modes**:
  - **Play vs Computer**: Single-player matches against smart AI bots with difficulty levels (Easy, Medium, Master).
  - **Pass & Play (Local Multiplayer)**: 2, 3, or 4 players on a single device.
  - **Golden Rush**: Fast-paced mode where the first token to reach home wins!
  - **Classic Mode**: Traditional rules requiring all 4 tokens home.
- **Authentic Ludo Rules**:
  - Roll a **6** to bring locked tokens onto the starting track.
  - Extra bonus turns on rolling a **6**, capturing opponents, or reaching home.
  - 3-consecutive-six penalty safeguard.
- **Audio & Haptics**:
  - Procedural sound synthesis (AudioTrack) for dice rolls, token hops, capturing impacts, safe star chimes, and victory fanfare.
  - Tactile haptic vibration for rolls and collisions.
- **Lifetime Statistics & History (Room DB)**:
  - Tracks total matches, wins, win rates, captures made, and sixes rolled.
  - Match history log with date, duration, winner, and user rank.
- **Customization & Guidance**:
  - 4 Board Themes: *Classic Heritage*, *Royal Blue*, *Forest Jade*, and *Midnight Obsidian*.
  - Interactive Illustrated Rulebook modal.

---

## Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material Design 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **State Management:** Kotlin Coroutines & StateFlow
- **Local Persistence:** Room Database with KSP (Kotlin Symbol Processing)
- **Audio:** Real-time procedural PCM audio via Android AudioTrack
- **Minimum SDK:** API 26 (Android 8.0+)
- **Target SDK:** API 35 (Android 15)

---

## What to Push to GitHub

When publishing this repository to GitHub, your existing .gitignore already ensures build artifacts and temporary files are excluded.

### Files to Commit and Push:
1. **Source Code**:
   - `app/src/main/java/` (All Kotlin files, UI screens, viewmodels, models, audio engine, database)
   - `app/src/test/java/` and `app/src/androidTest/java/` (Unit tests and instrumentation tests)
2. **Resources & Assets**:
   - `app/src/main/res/` (Layouts, strings, colors, drawables including the logo img_ludo_logo.jpg, launcher icons)
   - `app/src/main/AndroidManifest.xml`
3. **Build & Configuration Files**:
   - `build.gradle.kts` (Project-level)
   - `app/build.gradle.kts` (App-level)
   - `settings.gradle.kts`
   - `gradle/libs.versions.toml` (Version Catalog)
   - `gradle/` wrapper scripts (gradlew, gradlew.bat, gradle/wrapper/gradle-wrapper.properties)
   - `metadata.json`
   - `README.md` and `.gitignore`

### Files to Never Push (Already Handled by .gitignore):
- `build/` and `app/build/` (Gradle build outputs and APKs)
- `.gradle/` and `.kotlin/` (Local Gradle cache)
- `local.properties` (Contains local Android SDK path)
- `.idea/` (Android Studio workspace and cache files)
- `.env` or sensitive API secrets / keystores (debug.keystore)

---

## How to Push to GitHub

### Option A: From AI Studio Directly
In AI Studio, you can export your project to GitHub directly using the **Export / Push to GitHub** option in the top-right settings menu.

### Option B: Using Git via Terminal

1. **Initialize Git (if not already done):**
   ```bash
   git init
   ```

2. **Add all project files:**
   ```bash
   git add .
   ```

3. **Commit your changes:**
   ```bash
   git commit -m "feat: initial release of Ludo Master Android app"
   ```

4. **Link your GitHub repository:**
   ```bash
   git remote add origin https://github.com/<your-username>/<your-repo-name>.git
   git branch -M main
   ```

5. **Push to GitHub:**
   ```bash
   git push -u origin main
   ```

---

## Running in Android Studio

1. Clone or download the repository.
2. Open Android Studio and select **Open**.
3. Choose the project folder and allow Gradle to sync.
4. Select a virtual device (emulator) or a physical Android device.
5. Click **Run** to build and launch **Ludo Master**!
