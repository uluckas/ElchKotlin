# Elch App - Compose Multiplatform

This project is a Kotlin Multiplatform Mobile (KMM) application built with Compose Multiplatform, targeting Android, iOS, and Desktop. It is a migrated version of the original ElchKotlin Android app.

## Project Structure

- `src/commonMain/kotlin`: Contains common Kotlin code, including shared business logic (`CommonAlarmTimer.kt`), UI (`App.kt`, `TimerViewModel.kt`), and expect declarations.
- `src/commonMain/composeResources/`: Contains common resources like images (`drawable/elch.jpg`) and sounds (`raw/ring.wav`) used across platforms.
- `src/androidMain/`: Contains Android-specific code, including the main Activity (`AndroidApp.kt`), `AndroidManifest.xml`, platform-specific `actual` implementations (e.g., for alarms, settings), and Android resources.
- `src/iosMain/`: Contains iOS-specific code, including the main UI entry point (`Main.ios.kt`), `AppDelegate.swift`, `Info.plist`, `project.yaml` (for Xcodegen), and iOS-specific `actual` implementations.
- `src/desktopMain/`: Contains Desktop JVM-specific code, including the `main()` function (`Main.kt`) and Desktop-specific `actual` implementations.
- `src/commonTest/`: Contains unit tests for the common logic.

## Features

- Timer functionality with start, pause, and reset.
- Audible alarm when the timer expires (platform-specific implementation).
- Visual display of the timer.
- Basic UI built with Compose Multiplatform.

## Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher (for Gradle and Android).
- **Android Studio**: For Android development (latest stable version recommended).
- **Xcode**: For iOS development (latest stable version recommended, requires macOS).
- **Kotlin Multiplatform Mobile plugin** in Android Studio.
- **CocoaPods**: For iOS, if any KMP libraries require it (not explicitly used by base setup here, but good to have).
- **Xcodegen**: For generating the iOS Xcode project from `project.yaml`. Install via Homebrew: `brew install xcodegen`.

## Building and Running

**General:**
- This project uses Gradle for building. Ensure you have a compatible JDK installed.
- The Gradle wrapper (`./gradlew`) is included. Make it executable (`chmod +x gradlew`) if it isn't.
- **Resource Files**:
    - Place `elch.jpg` in `src/commonMain/composeResources/drawable/`.
    - Place `ring.wav` in `src/commonMain/composeResources/raw/`.
    (These files were part of the original repository but could not be automatically restored by the AI assistant).

**Android:**
1. Open the project in Android Studio.
2. Let Gradle sync.
3. Select the `androidApp` run configuration (it might be named `android`, `app`, or similar based on KMP template conventions; the current root `build.gradle.kts` structure implies a single module where Android is a target, so Android Studio might show targets directly).
4. Run on an emulator or physical device.
Command line: `./gradlew assembleDebug` (or `assembleRelease`)

**iOS:**
1. Ensure you have Xcode and Xcodegen installed.
2. Navigate to the project root in your terminal.
3. Run `xcodegen` to generate the `ElchAppKMPiOS.xcodeproj` file.
4. Open `ElchAppKMPiOS.xcodeproj` in Xcode.
5. Select a simulator or connect a device.
6. Build and run from Xcode.
Command line (building the KMP framework for Xcode): `./gradlew packForXcode` (or similar task provided by KMP plugin).

**Desktop:**
1. You can run the desktop application using the `./gradlew run` command or by finding the `run` task in your IDE's Gradle panel (often under `desktop > Tasks > application > run`).
2. To build a distributable package: `./gradlew packageDistributionForCurrentOS` (or specific tasks like `packageDmg`, `packageMsi`, `packageDeb`).

## Crashlytics (Legacy Fabric - Android)

The project includes an attempt to integrate the legacy Fabric Crashlytics SDK for Android:
- The API key is included in `AndroidManifest.xml`.
- `crashlytics.properties` file is in the project root. It contains the `apiKey`.
- **Action Required**: If you have the original `apiSecret` from your Fabric account, add it to `crashlytics.properties`. Without it, Crashlytics reporting might not function fully.
- **Note**: Fabric is deprecated. For long-term stability, migrating to Firebase Crashlytics is recommended.

## Known Issues / Limitations of AI Migration

- **Gradle Verification**: Due to limitations in the AI assistant's execution environment, running Gradle commands (like `./gradlew tasks` or `./gradlew --version` or actual builds) was not possible. The Gradle setup is based on common KMP templates but could not be dynamically verified.
- **File Deletion**: The AI assistant faced issues with reliably deleting old directories from the original project. Some remnants might exist.
- **Binary File Handling**: The AI assistant could not directly write the `elch.jpg` and `ring.wav` files due to tool limitations handling binary data. These need to be added manually as specified above.
