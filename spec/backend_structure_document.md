# Backend Structure Document

## 1. Backend Architecture

Overall, the app uses a single-device, offline-first architecture. There is no remote server or cloud component. All backend logic lives on the user’s Android device, following these design patterns and frameworks:

*   MVVM (Model-View-ViewModel) pattern:

    *   ViewModels expose data to the UI via LiveData or Flow.
    *   Repositories mediate between ViewModels and the local database.
    *   DAOs (Data Access Objects) handle low-level database operations.

*   Android Jetpack:

    *   Room for structured local persistence.
    *   WorkManager for scheduling background tasks (e.g., periodic backup reminders).
    *   Navigation component for screen transitions.

*   Kotlin coroutines for asynchronous database and I/O operations.

How this supports key goals:

*   Scalability: As data grows, Room efficiently manages indexing and query performance. Repositories and DAOs can be extended with new tables or queries without disrupting existing logic.
*   Maintainability: Clear separation of concerns (UI vs. business logic vs. data) makes it easy to locate and update code. MVVM and Jetpack conventions are familiar to Android developers.
*   Performance: Local, encrypted SQLite (via SQLCipher) provides low-latency reads and writes. Coroutines prevent UI blocking. WorkManager ensures background tasks respect battery constraints.

## 2. Database Management

### Technologies Used

*   SQL database (SQLite) via **Room**
*   Encryption enabled through **SQLCipher**

### Data Structure and Access

*   Data is organized into tables (entities) representing usage stats, unlock events, notifications, reminder settings, and backup records.
*   Room generates type-safe DAOs for CRUD operations.
*   Queries return Kotlin Flow or LiveData streams to keep the UI updated in real time.
*   All data at rest is encrypted by SQLCipher to ensure privacy.
*   Data export to CSV, PDF, or JSON occurs by reading from Room tables and writing to local files.

### Data Management Practices

*   Indexes on timestamp fields to speed up queries by date or time range.
*   Transactions for batch inserts (e.g., bulk import of usage samples).
*   Error handling around database operations to catch and log exceptions without crashing the app.
*   Manual and automated cleanup routines (triggered by user or WorkManager) to compact the database if it grows beyond a threshold.

## 3. Database Schema

Below is a human-readable overview of each table followed by the corresponding SQL schema.

### Human-Readable Schema

1.  **UsageStats**

    *   id (auto-increment)
    *   packageName (app identifier)
    *   startTimestamp (ms)
    *   endTimestamp (ms)
    *   activeDuration (seconds)
    *   mode (Standard, High Accuracy, Energy Saving)

2.  **UnlockEvents**

    *   id
    *   eventTimestamp (ms)
    *   countSinceLast (number of unlocks)

3.  **ScreenEvents**

    *   id
    *   onTimestamp (ms)
    *   offTimestamp (ms)

4.  **NotificationEvents**

    *   id
    *   packageName
    *   notificationTimestamp (ms)
    *   actionTaken (viewed, dismissed, ignored)

5.  **Reminders**

    *   id
    *   type (global or per-app)
    *   packageName (nullable if global)
    *   limitSeconds
    *   alertThresholds (e.g., 0.8, 0.9)
    *   enabled (true/false)

6.  **BackupRecords**

    *   id
    *   filePath
    *   format (CSV, PDF, JSON)
    *   createdTimestamp (ms)

### SQL Schema (SQLite + SQLCipher)

`-- UsageStats table CREATE TABLE UsageStats ( id INTEGER PRIMARY KEY AUTOINCREMENT, packageName TEXT NOT NULL, startTimestamp INTEGER NOT NULL, endTimestamp INTEGER NOT NULL, activeDuration INTEGER NOT NULL, mode TEXT NOT NULL ); -- UnlockEvents table CREATE TABLE UnlockEvents ( id INTEGER PRIMARY KEY AUTOINCREMENT, eventTimestamp INTEGER NOT NULL, countSinceLast INTEGER NOT NULL ); -- ScreenEvents table CREATE TABLE ScreenEvents ( id INTEGER PRIMARY KEY AUTOINCREMENT, onTimestamp INTEGER NOT NULL, offTimestamp INTEGER NOT NULL ); -- NotificationEvents table CREATE TABLE NotificationEvents ( id INTEGER PRIMARY KEY AUTOINCREMENT, packageName TEXT NOT NULL, notificationTimestamp INTEGER NOT NULL, actionTaken TEXT NOT NULL ); -- Reminders table CREATE TABLE Reminders ( id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, packageName TEXT, limitSeconds INTEGER NOT NULL, alertThresholds TEXT NOT NULL, enabled INTEGER NOT NULL ); -- BackupRecords table CREATE TABLE BackupRecords ( id INTEGER PRIMARY KEY AUTOINCREMENT, filePath TEXT NOT NULL, format TEXT NOT NULL, createdTimestamp INTEGER NOT NULL ); -- Indexes CREATE INDEX idx_usage_start ON UsageStats(startTimestamp); CREATE INDEX idx_unlock_time ON UnlockEvents(eventTimestamp);`

## 4. API Design and Endpoints

Since the app is fully offline, there are no network APIs. Instead, we define internal repository methods and DAO queries that serve as the "backend endpoints" for the UI:

*   **UsageRepository**

    *   getUsageByDateRange(start, end)
    *   insertUsageSamples(listOfSamples)
    *   deleteUsageBefore(timestamp)

*   **UnlockRepository**

    *   getUnlockCountByDay(day)
    *   insertUnlockEvent(event)

*   **NotificationRepository**

    *   getNotificationsByApp(packageName)
    *   insertNotificationEvent(event)

*   **ReminderRepository**

    *   getActiveReminders()
    *   setReminder(reminder)
    *   removeReminder(id)

*   **BackupManager**

    *   exportData(format)
    *   importData(uri)
    *   listBackupRecords()

The UI layer invokes these repository methods. Data flows back as LiveData or Flow streams.

## 5. Hosting Solutions

*   **Hosting Environment:** Local device storage only (no cloud or server).

*   **Benefits:**

    *   Reliability: No network required; app works offline at all times.
    *   Privacy: All data stays on the user’s device, fully encrypted.
    *   Cost-Effectiveness: No server costs or maintenance.

## 6. Infrastructure Components

All components run on the Android device:

*   **Room Database with SQLCipher:** Secure local storage.
*   **WorkManager:** Schedules background tasks, such as daily database cleanup or backup reminders, respecting Doze mode and battery constraints.
*   **In-memory Caching:** ViewModels cache recent queries to avoid repeated database hits.
*   **File I/O:** Standard Android file APIs handle exports (CSV, JSON, PDF) to the user’s local storage.
*   **Coroutines and Dispatchers:** IO-optimized threads for database and file operations.

## 7. Security Measures

*   **Data Encryption:** Entire SQLite database encrypted via SQLCipher.
*   **Permissions Handling:** App checks for and requests Usage Access and Notification Access at runtime. If denied, the UI shows clear guidance and a link to system settings.
*   **Runtime Protection:** No third-party SDKs; no network connections.
*   **GDPR Compliance:** No personal identifiers or user profiles are ever stored or transmitted.
*   **Error Handling:** All database and file operations wrapped in try/catch. Failures are logged locally (e.g., to a file or via Timber) and surfaced to the user with actionable messages.

## 8. Monitoring and Maintenance

*   **In-App Logging:** Use Timber for structured logs. Optionally write logs to a file that advanced users can share when troubleshooting.
*   **Health Checks:** Before each major operation (export/import, cleanup), the app verifies database integrity using PRAGMA integrity_check.
*   **Automated Maintenance:** WorkManager runs periodic tasks to compact the database and delete temporary files if storage exceeds a threshold.
*   **Updates:** Modular codebase with clear boundaries makes patching or adding features straightforward. Unit tests on repository and DAO layers ensure reliability.

## 9. Conclusion and Overall Backend Summary

This backend structure keeps all data and logic on the user’s Android device, balancing performance, privacy, and maintainability. By leveraging Room, SQLCipher, and the MVVM pattern, we provide fast, encrypted data access that scales with the user’s activity. WorkManager and coroutines ensure smooth background processing without draining the battery. The absence of external servers guarantees offline operation and zero hosting costs, making it a robust, user-friendly solution for tracking digital well-being.
