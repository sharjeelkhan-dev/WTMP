# WTMP Project Walkthrough

WTMP (Who Touched My Phone) is a security-focused Android application designed to monitor and log unauthorized access attempts to your device. Built with a futuristic **Cyber/Neon** aesthetic, it combines modern Android development practices with robust background monitoring.

## 1. Architecture
The project follows **Clean Architecture** principles and the **MVVM** design pattern, ensuring scalability and testability.

*   **UI Layer**: Built entirely with **Jetpack Compose**, featuring a custom Material 3 Cyber theme.
*   **Domain Layer**: Contains business logic and repository interfaces (e.g., `SecurityRepository`).
*   **Data Layer**:
    *   **Room Database**: Persists security events and metadata locally.
    *   **DataStore**: Manages user preferences and protection states.
    *   **Hilt**: Handles Dependency Injection across all layers.
*   **Service Layer**: A **Foreground Service** manages the lifecycle of device monitoring, ensuring the app remains active even when in the background.

## 2. UI/UX
The application features a unique **Cyberpunk/Glassmorphism** design language.

*   **Material 3 Cyber Theme**: Uses neon accents (Cyan, Green, Red) against a deep dark background (`CyberBackground`).
*   **Glassmorphism Cards**: Translucent UI components (`GlassCard`) that provide a layered, futuristic depth.
*   **Pager-based Onboarding**: A smooth 3-step introduction to the app using `HorizontalPager`.
*   **Animated Dashboard**:
    *   Pulsing "Protection Status" indicator.
    *   Animated circular progress bars for Security Scoring.
    *   Interactive activity charts in the Stats screen.

## 3. Core Features
*   **Foreground Service Monitoring**: Actively listens for `ACTION_USER_PRESENT` (Device Unlock) events.
*   **CameraX Evidence Capture**: Automatically triggers a silent front-camera capture when an unlock is detected.
*   **Stats Dashboard**: Visualizes protection metrics, including session duration, intrusion prevention counts, and activity trends.
*   **Biometric Protection**: Optional app-level locking using the Android Biometric API (configurable in Settings).

## 4. Security & Privacy
WTMP is designed with a **Privacy-First** approach:

*   **Local Storage**: All captured photos and logs are stored in the app's internal, private directory. No cloud syncing is performed.
*   **Privacy Center**: A dedicated section explaining data handling and providing tools for global data deletion.
*   **Permission Handling**: Transparent request flow for Camera and Notification permissions required for core functionality.

## 5. Screens Implemented
*   **Splash Screen**: Futuristic logo animation.
*   **Onboarding**: Feature walkthrough for new users.
*   **Dashboard**: Main control center for toggling protection and viewing summary stats.
*   **History**: A searchable, detailed list of all recorded security events.
*   **Event Details**: Deep-dive into a specific event, including the captured intruder photo.
*   **Stats**: Comprehensive analytics of device security over time.
*   **Settings**: Configuration for biometric lock, sensitivity, alarm, and vibration.
*   **Privacy Center**: Transparency portal for user data rights.

## 6. How to Test the Protection
To verify the implementation:
1.  Open the app and complete the **Onboarding**.
2.  On the **Dashboard**, tap **"Enable Protection"**.
3.  Grant the necessary **Camera** and **Notification** permissions when prompted.
4.  Lock your phone manually.
5.  Unlock your phone (simulate a normal usage session).
6.  Return to the WTMP app and check the **History** tab. You should see a new entry with your photo captured during the unlock.

## 7. Future Enhancements
*   **Audible Alarm**: Trigger a loud siren if multiple failed unlock attempts are detected.
*   **Encrypted Backups**: Option for users to export an encrypted archive of their security history.
*   **Geofencing**: Automatically enable/disable protection based on trusted locations (e.g., Home).

---
*Developed with ❤️ and Security in mind.*
