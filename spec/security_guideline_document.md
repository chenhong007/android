# TimeSpent App: Step-by-Step Implementation Plan

## 1. Project Setup & Architecture

### 1.1. Initialize the Android Project

*   Create a new Android Studio project targeting API 33+ with Kotlin and Jetpack Compose.

*   Configure Gradle:

    *   Enable `kotlin-kapt` for Room.
    *   Add `compose`, `lifecycle`, `navigation`, `room`, `mpandroidchart`, and `sqlcipher` dependencies.
    *   Use a lockfile (`gradle.lockfile`) for deterministic builds.

### 1.2. Define Clean Architecture

*   Layers:

    *   **Data Layer**: Room entities, DAOs, SQLCipher integration, file I/O for backup/restore.
    *   **Domain Layer**: Use cases for usage-tracking, statistics computation, reminders.
    *   **Presentation Layer**: ViewModels with LiveData/StateFlow, Jetpack Compose UI.

*   Benefits:

    *   Separation of concerns, testability, maintainability.

### 1.3. Security & Privacy by Design

*   Threat Modeling: Identify attackers, assets (usage data), and threats (data exfiltration).
*   Integrate security checks into CI (static analysis, dependency scanning).
*   Use Android Keystore for key management—never hardcode encryption keys.
*   Apply the principle of least privilege for permissions and database access.

## 2. Permission Handling & Onboarding Flow

### 2.1. Onboarding Screens

*   Screen 1: Explain digital-wellbeing benefits.
*   Screen 2: Request **Usage Access** (`UsageStatsManager`).
*   Screen 3: Request **Notification Listener** (`NotificationListenerService`).
*   Use Android’s `ActivityResultLauncher` APIs for permission flows.
*   Always provide rationale dialogs and a fallback path if permission is denied.

### 2.2. Fail-Secure Permission Checks

*   At app launch and before each core operation, verify permissions; if missing, redirect to settings.
*   Log events locally (no remote analytics) for audit and debugging.

## 3. Data Collection Module

### 3.1. Usage Tracking Service

*   Implement a `ForegroundService` bound to the app’s lifetime when tracking is active.

*   Use `UsageStatsManager.queryEvents()` with a sliding window for precise, second-level data.

*   Session Recognition:

    *   Detect `moveToForeground` / `moveToBackground` events.
    *   Exclude lock-screen time by filtering events with `Configuration.USER_TYPE_KEYGUARD`.

### 3.2. Notification Monitoring

*   Extend `NotificationListenerService`:

    *   Record notification posts and removals.
    *   Throttle writes to DB (batch every few seconds) to reduce I/O.

### 3.3. Performance Modes

*   **Standard**: Poll every 5 seconds, batch writes every minute.
*   **High-Precision**: Poll every second, batch writes every 10 seconds.
*   **Energy-Saving**: Poll every 15–30 seconds, batch writes every 2 minutes.
*   Allow user to switch modes; persist mode in encrypted SharedPreferences.

## 4. Secure Local Storage

### 4.1. Encrypted Room Database

*   Integrate [SQLCipher for Android](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/) via Room.

*   Generate a random database key at first launch; store it securely in the Android Keystore.

*   Define entities:

    *   `AppUsageEvent(id, packageName, startTime, endTime, mode)`
    *   `NotificationEvent(id, packageName, timestamp, type)`
    *   `UnlockEvent(id, timestamp)`

*   DAOs use `@Query`, `@Insert(onConflict = IGNORE)`, `@Transaction` with parameterized statements.

### 4.2. Backup & Restore

*   Export database file (`.db`, `.db-wal`, `.db-shm`) into a ZIP with a timestamped filename.
*   Encrypt the ZIP with the same database key.
*   Import: prompt user for file, verify signature (via HMAC-SHA256), decrypt, replace local DB.
*   Secure defaults: store backups in scoped storage, require user action to share.

## 5. Business Logic & Use Cases

### 5.1. Aggregation & Filtering

*   Use Coroutines + Flow in domain layer for:

    *   Daily/weekly/monthly summaries.
    *   Custom date-range queries.

*   Offload heavy computations to `Dispatchers.Default`.

*   Validate all query parameters (start < end, date formats).

### 5.2. Reminder Scheduling

*   Use `WorkManager` for Do-Not-Disturb and usage reminders.

*   Persist constraints and triggers in Room; on boot and at config change, reschedule.

*   Allow users to configure:

    *   Thresholds (e.g., 2 hours screen-on).
    *   Quiet hours (Do-Not-Disturb windows).

## 6. UI/UX & Data Visualization

### 6.1. Theming & Accessibility

*   Use Material3 Compose theming with `darkTheme = isSystemInDarkTheme()`.
*   Provide high-contrast palette switch and dynamic font scaling based on `LocalDensity`.
*   Add `contentDescription` for all icons and interactive elements.

### 6.2. Charts Integration

*   MPAndroidChart in Compose:

    *   BarChart (daily summaries).
    *   CalendarHeatmap (custom Composable with `Canvas`).
    *   LineChart (usage trends).

*   Features:

    *   Zoom/pan, tooltips on tap, animations on data load.

*   Dynamically update charts via LiveData/StateFlow; debounce rapid updates.

## 7. Localization & Settings

### 7.1. Multilingual Support

*   Strings in `res/values/strings.xml` and `res/values-zh/strings.xml`.
*   Use `stringResource()` in Compose.
*   Format dates and numbers via `java.time` and `Locale`.

### 7.2. Settings Screen

*   Toggle themes, performance mode, reminder thresholds.
*   “Export Data” and “Delete All Data” actions with confirmation dialogs.
*   On Delete: securely wipe DB, keystore key, and SharedPreferences.

## 8. Testing & Quality Assurance

### 8.1. Unit & Instrumentation Tests

*   Domain logic: JUnit + MockK.
*   DAO tests: Room in-memory tests with SQLCipher dependency.
*   UI tests: Compose testing library, Espresso for permission flows.

### 8.2. Security & Performance Testing

*   Static Analysis: integrate Detekt, Android Lint, and SCA (e.g., OWASP Dependency-Check).
*   Dynamic Analysis: check for database leaks, unencrypted temp files.
*   Battery Profiling: use Android Studio Energy Profiler; ensure daily drain <1%.

## 9. Release & Maintenance

### 9.1. Release Build Configuration

*   Disable debug logging, remove `android:debuggable=true`.
*   ProGuard/R8 rules to obfuscate code but keep room entities.
*   Enforce `minifyEnabled`, `shrinkResources`.

### 9.2. Ongoing Operations

*   Monitor Google Play Console for crashes (only anonymous stack traces, no PII).
*   Schedule regular dependency updates and re-run SCA.
*   Provide GDPR-style “Privacy” info in About screen: data collected, storage methods, user rights.

**By following this phased plan, we’ll ensure TimeSpent is performant, privacy-centric, and secure by design.**
