# Tail Wagging - Pet Care App

Tail Wagging is a comprehensive Android application designed to help pet owners manage their pets' health, schedules, and daily needs. From tracking vaccinations to an AI-powered health scanner, this app is the perfect companion for every loving pet owner.

## Features

- **Pet Profiles:** Create and manage detailed profiles for all your pets.
- **Calendar & Reminders:** Schedule vet visits, medication, grooming, and more with automated notifications.
- **AI Health Scanner:** Use the power of Google Gemini AI to analyze pet-related health concerns from photos.
- **Service Locator:** Find nearby pet services and veterinary clinics.
- **Notification Center:** Keep track of all reminders and important alerts in one place.
- **User Profiles:** Manage your account and pet owner details.

## Tech Stack

- **Language:** Java
- **UI Framework:** Android XML with Google Material Design
- **Database:** Firebase Realtime Database
- **Authentication:** Firebase Auth
- **Storage:** Firebase Storage & Cloudinary
- **AI Integration:** Google Gemini 1.5 Flash (via OkHttp)
- **Maps:** MapLibre SDK
- **Image Loading:** Glide
- **Build System:** Gradle (Kotlin DSL)

## Getting Started

### Prerequisites

- Android Studio Koala or newer
- JDK 11+
- A Google Gemini API Key (for the AI features)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/sadikmahmudadive/Tail-wagging-pet-care-app.git
   ```

2. **Open in Android Studio:**
   Open the project folder in Android Studio and let Gradle sync.

3. **Configure API Keys:**
   Create a `local.properties` file in the root directory (if not already present) and add your Gemini API key:
   ```properties
   GEMINI_API_KEY=YOUR_ACTUAL_API_KEY_HERE
   ```

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
