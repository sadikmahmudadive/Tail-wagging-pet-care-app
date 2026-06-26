# Tail Wagging - Pet Care App

Tail Wagging is a comprehensive Android application designed to help pet owners manage their pets' health, schedules, and daily needs. From tracking vaccinations to an AI-powered health scanner, this app is the perfect companion for every loving pet owner.

## Features

- **Pet Profiles:** Create and manage detailed profiles for all your pets including medical history and birthdays.
- **AI Health Scanner:** Analyze pet-related health concerns from photos using **Google Gemini 1.5 Flash**. Save results directly to your pet's medical records.
- **Offline Support:** View your pets, notifications, and scheduled events even without an internet connection thanks to Firebase Disk Persistence.
- **Actionable Notifications:** Get smart reminders for vet visits and tasks with direct actions like "Mark Done" or "Dismiss" from the notification bar.
- **Service Locator & Search:** Find nearby pet services (Veterinarians, Grooming, Boarding) with real-time search and category filtering.
- **Calendar & Reminders:** A complete scheduling system for pet care with automated local alarms.
- **Modern UI:** Immersive parallax headers, sticky action bars, and Material 3 design for a premium experience.

## Tech Stack

- **Language:** Java
- **UI Framework:** Android XML with Google Material Design 3
- **Database:** Firebase Realtime Database (with Offline Persistence)
- **Authentication:** Firebase Auth
- **Storage:** Firebase Storage & Cloudinary
- **AI Integration:** Google Gemini 1.5 Flash (via OkHttp)
- **Maps:** MapLibre SDK
- **Image Loading:** Glide (with disk caching)
- **Build System:** Gradle (Kotlin DSL)

## Getting Started

### Prerequisites

- Android Studio Koala or newer
- JDK 11+
- A Google Gemini API Key (obtained from [Google AI Studio](https://aistudio.google.com/))

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/sadikmahmudadive/Tail-wagging-pet-care-app.git
   ```

2. **Open in Android Studio:**
   Open the project folder in Android Studio and let Gradle sync.

3. **Configure API Keys:**
   Create a `local.properties` file in the root directory (if not already present) and add your API keys:
   ```properties
   GEMINI_API_KEY=YOUR_GEMINI_KEY
   OPENAI_API_KEY=YOUR_OPENAI_KEY
   ```
   *Note: These keys are kept local and won't be committed to Git for security.*

## Security & Privacy (Strict Rules)

To prevent API keys from being leaked and disabled by providers (OpenAI, Google), this project uses a strict security workflow:

1.  **BuildConfig Integration**: All API keys are injected at build time from `local.properties`. Never hardcode keys in Java files.
2.  **GitHub Actions**: A security scanner (`gitleaks`) runs on every push. If a secret is detected, the build will fail.
3.  **Pre-commit Hook**: A script is provided in `Support/pre-commit-hook.sh`. You should install it to prevent committing keys locally:
    ```bash
    cp Support/pre-commit-hook.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    ```

If your key is revoked, check your git history to ensure you haven't committed it in a previous version.

4. **Firebase Setup:**
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.tailwagging`.
   - Download the `google-services.json` and place it in the `app/` directory.

5. **Run the app:**
   Select your emulator or physical device and click the "Run" button.

## License

Copyright (C) 2026 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
