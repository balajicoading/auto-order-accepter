# AutoAccept Android App

A native Android app (Kotlin + Views) that automatically reads order popups using OCR and performs gestures based on distance thresholds.

## Build Instructions

1. Open this project in Android Studio
2. Wait for Gradle sync to complete
3. Build and run on a device with Android 7.0+ (API 26+)

## Setup

1. Launch the app
2. Enable the AutoAccept service in Accessibility Settings (the app will prompt you)
3. For Android 7-12: Grant screen capture permission when enabling the service
4. For Android 13+: Grant notification permission
5. Toggle "Enable Auto-Accept" in the app

## How It Works

- The app uses an AccessibilityService to capture screenshots
- ML Kit OCR reads "Pickup distance" and "Drop distance" from the screen
- If total distance <= threshold (default 2.0 km): performs a swipe gesture
- If total distance > threshold: performs a tap gesture to close

## Configuration

All settings are configurable in the main screen:
- Swipe coordinates (start/end X/Y)
- Swipe duration (ms)
- Close tap coordinates (X/Y)
- Distance threshold (km)

Test buttons allow you to verify gestures work correctly.

## Technical Details

- Min SDK: 26 (Android 7.0)
- Target SDK: 34 (Android 14)
- Uses Google ML Kit Text Recognition (on-device)
- AccessibilityService for gesture dispatch
- Foreground service for background operation
- All settings persist to SharedPreferences

## Project Structure

```
app/src/main/java/com/autoaccept/app/
├── MainActivity.kt - Main UI and settings
├── OrderAccessibilityService.kt - Accessibility service with OCR loop
├── KeepAliveService.kt - Foreground service
├── OcrUtils.kt - ML Kit OCR processing
└── Prefs.kt - SharedPreferences helper
```
