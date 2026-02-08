# Duetduetku - Finance Tracker

A smart finance tracker app powered by AI (Gemini) and Voice Recognition.

## Features
- **Voice Note Input**: Record transactions naturally (e.g., "Makan bakso 15rb").
- **AI Analysis**: Automatically extracts merchant, amount, and date from voice or text using Gemini AI.
- **Scan Receipt**: Scan physical receipts to auto-fill transaction details.
- **Budgeting**: Set and track monthly budgets.
- **Privacy First**: Built with offline-first architecture (Room Database).

## Setup Instructions

### 1. Requirements
- Android Studio Ladybug or newer.
- Android SDK 35.
- Firebase Account.
- Google AI Studio Account.

### 2. Secrets Configuration
This project uses sensitive keys that are **ignored** by git. You must create them manually.

#### `local.properties`
1. Duplicate `local.properties.example`.
2. Rename it to `local.properties`.
3. Add your Gemini API Key:
   ```properties
   GEMINI_API_KEY=AIzaSy...
   ```

#### `google-services.json`
1. Create a project in [Firebase Console](https://console.firebase.google.com/).
2. Add an Android App with package name: `com.duetduetku.app`.
3. Download `google-services.json`.
4. Place it in the `app/` directory (`app/google-services.json`).

### 3. Build for Debug
- Run `./gradlew assembleDebug` or build directly from Android Studio.

## Production Release

### 1. Pre-requisites
- Ensure `google-services.json` is in `app/`.
- Ensure `local.properties` has your Gemini API Key.

### 2. Generate Signed APK/Bundle
1. Open Android Studio.
2. Go to **Build** > **Generate Signed Bundle / APK**.
3. Select **Android App Bundle** (best for Play Store) or **APK** (for manual install).
4. Click **Next**.
5. **Key Store Path**: Click "Create new" if you don't have one. Save it safely!
6. Fill in the "Key store password", "Key alias", and "Key password".
7. Click **Next**, select **Release**, and click **Create**.
8. Upload the `.aab` file to Google Play Console!
