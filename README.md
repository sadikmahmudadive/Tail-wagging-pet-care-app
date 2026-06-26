# Tail Wagging - Pet Care App

Tail Wagging is a comprehensive Android application designed to help pet owners manage their pets' health, schedules, and daily needs. From tracking vaccinations to an AI-powered health scanner, this app is the perfect companion for every loving pet owner.

## Features

- **Pet Profiles:** Create and manage detailed profiles for all your pets.
- **Calendar & Reminders:** Schedule vet visits, medication, grooming, and more with automated notifications.
- **AI Health Scanner:** Analyze pet-related health concerns from photos using OpenAI ChatGPT Vision.
- **Service Locator:** Find nearby pet services and veterinary clinics.
- **Notification Center:** Keep track of all reminders and important alerts in one place.
- **User Profiles:** Manage your account and pet owner details.

## Tech Stack

- **Language:** Java
- **UI Framework:** Android XML with Google Material Design
- **Database:** Firebase Realtime Database
- **Authentication:** Firebase Auth
- **Storage:** Firebase Storage & Cloudinary
- **AI Integration:** OpenAI ChatGPT Vision (gpt-4o-mini via OkHttp)
- **Maps:** MapLibre SDK
- **Image Loading:** Glide
- **Build System:** Gradle (Kotlin DSL)

## Getting Started

### Prerequisites

- Android Studio Koala or newer
- JDK 11+
- An OpenAI API Key (for the AI features)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/sadikmahmudadive/Tail-wagging-pet-care-app.git
   ```

2. **Open in Android Studio:**
   Open the project folder in Android Studio and let Gradle sync.

3. **Configure API Keys:**
   Create a `local.properties` file in the root directory (if not already present) and add your OpenAI API key:
   ```properties
   OPENAI_API_KEY=YOUR_ACTUAL_API_KEY_HERE
   ```

4. **Firebase Setup:**
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.tailwagging`.
   - Download the `google-services.json` and place it in the `app/` directory.

5. **Run the app:**
   Select your emulator or physical device and click the "Run" button.

## Firebase Security Rules

This project includes baseline secure Firebase rules. They require authentication for all reads/writes and add light validation for AI medical history entries stored in Realtime Database.

Files:
- `database.rules.json` (Firebase Realtime Database)
- `firestore.rules` (Cloud Firestore)
- `storage.rules` (Cloud Storage)

Deploying rules (via Firebase CLI):
1. Install and login: `npm i -g firebase-tools` then `firebase login`
2. Initialize (once): `firebase init` and select Database, Firestore, and Storage. Point to the rule files above when prompted.
3. Deploy:
   - Realtime Database: `firebase deploy --only database`
   - Firestore: `firebase deploy --only firestore:rules`
   - Storage: `firebase deploy --only storage`

Notes:
- Current app writes AI results to the Realtime Database path `service_records/{recordId}`. Rules validate field presence and types. For stricter access control (per-user ownership), include a `userId` field on each record and extend the rules to enforce `request.auth.uid == newData.child('userId').val()`.

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
