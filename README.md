# AI Privacy Browser

An Android mobile browser application that focuses on AI-driven capabilities combined with strict privacy standards. This repository provides the complete foundation of the project built with the MVVM architecture and Repository pattern.

## Project Details
* **App Name:** AI Privacy Browser
* **Package:** `com.aibrowser.app`
* **Language:** Kotlin
* **Min SDK:** 26 (Android 8.0)
* **Target SDK:** 34 (Android 14)
* **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)
* **Architecture:** MVVM with Repository Pattern
* **Design Philosophy:** Material Design 3, View Binding, Kotlin Coroutines, Clean Code

---

## Architecture & Folder Structure

The app is modularly structured under `app/src/main/java/com/aibrowser/app/`:

* **`data/`**: Manages repositories, local database, and remote API/network integration.
* **`domain/`**: Contains core domain models and business logic.
* **`ui/`**: Host for UI components including Activities, Fragments, and ViewModels.
* **`util/`**: Helper methods, extensions, and utility classes.
* **`di/`**: Houses manual dependency injection components.

---

## Tech Stack & Required Dependencies

The foundation comes pre-configured with industry-standard Android libraries:

* **AndroidX Core KTX, AppCompat, Material Components (Material 3)** for high-quality Material 3 DayNight UI implementation.
* **AndroidX Lifecycle (ViewModel, LiveData)** for robust lifecycle management and architectural separation.
* **AndroidX Navigation, Fragment KTX, Activity KTX, Constraint Layout** for responsive and simplified navigation flow.
* **Kotlin Coroutines (core, android)** for asynchronous operations and non-blocking multi-threading.
* **Retrofit 2, OkHttp 4, Gson** for seamless network management and JSON serialization.
* **Google Generative AI SDK (Gemini)** (`com.google.ai.client.generativeai:generativeai`) to support on-device or cloud-based AI actions.
* **Firebase (BoM, Auth, Realtime Database, Analytics)** to offer backend service integrations, secure authentication, and telemetry.
* **Google Mobile Ads (AdMob)** for monetization.
* **Glide & Timber** for efficient image loading/caching and clean structured logging respectively.

---

## Declared Android Permissions

The `AndroidManifest.xml` is configured with the following permissions:
* `android.permission.INTERNET` - Essential for web browsing and online API calls.
* `android.permission.ACCESS_NETWORK_STATE` - Inspect network status.
* `android.permission.ACCESS_COARSE_LOCATION` - Location-based search features.
* `android.permission.CAMERA` (optional, future use) - QR scanning and image capturing.
* `android.permission.RECORD_AUDIO` (optional, future use) - Voice search features.
* `android.permission.QUERY_ALL_PACKAGES` - Package queries.
* `android.permission.FOREGROUND_SERVICE` - Support background processes.

---

## Setup & Build Instructions

Follow these instructions to build and run the project locally.

### Prerequisites
* Java Development Kit (JDK) 17 is required.
* Android SDK with Build Tools 34.

### Local Compilation

To build the project and assemble the debug APK, run the following command from the root directory:

```bash
./gradlew assembleDebug
```

The compiled APK will be located under:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Project Roadmap

Find the current and future phases of development in our official roadmap link:
[AI Privacy Browser Project Roadmap](https://github.com/com.aibrowser.app/roadmap)
