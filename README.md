# Golden Hour Planner for Photographers

A modern Android application built using Kotlin, Material 3, Room, Fused Location Provider, Firebase Authentication (Google Sign-In), and the Sunrise-Sunset REST API.

## Features

- **Google Sign-In (Firebase Auth)**: Authenticate with Google credentials or sign out anytime.
- **Location Auto-Detection**: Uses Google Fused Location Provider (`getCurrentLocation` with `PRIORITY_BALANCED_POWER_ACCURACY`) to detect device coordinates.
- **Live Sunrise-Sunset Calculations**: Fetches accurate solar times for today or any selected date from the Sunrise-Sunset API (`https://api.sunrise-sunset.org/json`). Converts UTC ISO-8601 timestamps to local time.
- **Golden Hours**: Calculates the exact Morning Golden Hour (1st hour of sunlight) and Evening Golden Hour (last hour of sunlight).
- **Material Date Picker**: Pick any past or future date using `MaterialDatePicker` to reload solar times and plans.
- **Real-Time Countdown**: Displays a live ticking countdown to the next solar event (Sunrise or Sunset) that updates continuously.
- **Photo-shoot Planner (Room Database)**: Save, edit, and delete photo-shoot plans with date, location name, and custom equipment notes stored locally using Room.

## Architecture & Stack

- **Language**: Kotlin 2.0+
- **UI Architecture**: MVVM with LiveData, ViewBinding, and Material Design 3 components
- **Database**: Android Jetpack Room with Kotlin Coroutines & Flow
- **Networking**: OkHttp3 & Gson for REST API parsing
- **Location**: Google Play Services Location (`play-services-location`)
- **Authentication**: Firebase Authentication (`firebase-auth`) & Google Sign-In (`play-services-auth`)

## Setup & Execution Instructions

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd practical
   ```

2. **Open in Android Studio**:
   Open Android Studio Ladybug (or newer) and select **Open** -> Navigate to the project root directory.

3. **Firebase Setup (Optional)**:
   Place your `google-services.json` file inside the `app/` directory if you wish to link your custom Firebase project.

4. **Build & Run**:
   - Connect an Android device or start an Android Virtual Device (AVD) running Android 7.0+ (API level 24+).
   - Run the `:app` module using `Shift + F10` or click **Run > Run 'app'**.

5. **Run Unit Tests**:
   Execute the following command in terminal or via Gradle tool window:
   ```bash
   ./gradlew testDebugUnitTest
   ```

## License

Copyright (c) 2025 Golden Hour Planner. All rights reserved.
