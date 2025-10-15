# Project Requirements Document (PRD)

## 1. Project Overview

**TimeSpent App** is a mobile application for Android devices (API 33+, Android 13 and above) that tracks and analyzes how users spend time on their phones. It collects millisecond-accurate usage data for each app session, visualizes trends through interactive charts, and sends timely reminders to help users manage screen time and improve digital well-being. All data stays on the device, encrypted locally, ensuring full privacy and compliance with GDPR.

We’re building TimeSpent App to give users clear insights into their smartphone habits—unlock counts, screen-on durations, notification frequency—and to nudge them toward healthier phone usage. Success will be measured by (1) accurate data collection, (2) intuitive visual reports, (3) reliable local backup/restore, and (4) responsive performance with minimal battery impact.

## 2. In-Scope vs. Out-of-Scope

### In-Scope (v1.0)

*   Android support: Android 13 (API 33) and above.
*   Millisecond-level app usage tracking with three performance modes (standard, high-precision, energy-saving).
*   Local encryption of usage data using SQLCipher.
*   Interactive visualization: bar charts, calendar heatmaps, timelines, comparison views.
*   Flexible filtering: time ranges (today, custom), app categories, smart grouping, blacklists.
*   Local backup/restore via encrypted files (CSV/PDF/JSON) — no cloud integration.
*   Screen unlock and screen-on/off event monitoring.
*   Notification history tracking (counts, response rates, categories).
*   Reminder settings: global and per-app limits, rest intervals, Do-Not-Disturb scheduling.
*   Dual-language support (Chinese + English) and dark/light theme.
*   Offline-only operation, no third-party SDKs, GDPR compliance.
*   Basic accessibility: screen reader labels, high-contrast mode, font scaling.

### Out-of-Scope (v1.0)

*   iOS support.
*   Parental-child role separation.
*   Monetization (ads, in-app purchases, subscriptions).
*   Cloud backup or third-party analytics/crash reporting.
*   Integration with external APIs or social platforms.
*   Advanced AI features or recommendation engines.

## 3. User Flow

When a new user installs and opens TimeSpent App for the first time, they see a welcome screen outlining core benefits: precise usage tracking, smart charts, and local privacy. Next, the app requests two permissions—Usage Access (to read app usage stats) and Notification Access (to log incoming alerts). Brief explanations accompany each permission prompt. Once granted, the app initializes its local database and navigates the user to the Home screen.

On the Home screen, the user finds a greeting (“Good morning”) and a summary card showing today’s total screen time versus yesterday. Below are two quick-stat cards for unlock count and screen-on duration, followed by a list of top five most-used apps (with usage bars and percentages). A fixed bottom navigation bar offers fast switching among five tabs: Home, Statistics, App Management, Reminders, and History. Tapping each tab reveals its dedicated interface and controls.

## 4. Core Features

*   **Authentication & Permissions**\
    • Onboarding with Usage and Notification Access requests.\
    • Local user profile (no login).
*   **Usage Tracking Module**\
    • Foreground/background monitoring with session thresholds (1–3 s).\
    • Three performance modes (standard, high-precision, energy-saving).\
    • Exclude lock-screen and screen-off durations.
*   **Data Storage & Privacy**\
    • Room database with SQLCipher encryption.\
    • Local-only data; GDPR compliant.\
    • Backup/restore via encrypted local files.
*   **Data Visualization**\
    • Bar charts (horizontal/vertical).\
    • Calendar heatmap for daily intensity.\
    • 24-hour timeline view.\
    • Comparison (time-range vs. time-range, app vs. app).
*   **Filtering & Comparison**\
    • Pre-set and custom date ranges.\
    • App-category and name search filters.\
    • Multi-select for side-by-side comparison.\
    • Smart grouping by frequency, blacklist feature.
*   **Behavior Monitoring**\
    • Unlock count, average interval.\
    • Screen-on/off events and total durations.\
    • Notification history & response analysis.
*   **Reminders & Settings**\
    • Global and per-app usage thresholds.\
    • Progress-based alerts (80%, 90%, etc.).\
    • Rest reminders at regular intervals.\
    • Do-Not-Disturb scheduling with whitelist.
*   **History & Export**\
    • Month-view calendar with intensity colors.\
    • Trend line charts (weekly, monthly, quarterly, yearly).\
    • Export filtered data as CSV, PDF, JSON.\
    • Export progress indicator with batching.

## 5. Tech Stack & Tools

*   **Platform & Language**:\
    • Kotlin on Android 13+ (API 33).
*   **UI Framework**:\
    • Jetpack Compose for declarative UI.
*   **Architecture & State**:\
    • Android Jetpack (ViewModel, LiveData, Navigation).
*   **Database**:\
    • Room + SQLCipher for encrypted local storage.
*   **Charts**:\
    • MPAndroidChart for interactive graphs.
*   **Icons & Fonts**:\
    • Lucide Icons (line style, 1.5–2 px).\
    • System fonts: -apple-system, Segoe UI, Roboto.
*   **File I/O**:\
    • Standard Android APIs for local backup/export.
*   **Build & IDE**:\
    • Android Studio Arctic Fox or later.\
    • Kotlin linting and Compose preview plugins.

## 6. Non-Functional Requirements

*   **Performance**\
    • App startup ≤ 1.5 s after permissions granted.\
    • Chart rendering and data queries return < 300 ms.\
    • Background data sampling optimized for ≤ 2% daily battery drain.
*   **Security & Privacy**\
    • All usage data encrypted at rest (SQLCipher).\
    • GDPR compliance: no personal identifiers, full local control.
*   **Usability & Accessibility**\
    • Dark/light theme following system settings.\
    • High-contrast support and dynamic font scaling.\
    • Content descriptions for screen readers.
*   **Reliability**\
    • Data consistency with Transaction boundaries in Room.\
    • Backup/restore error handling with user feedback.

## 7. Constraints & Assumptions

*   **Device & OS**:\
    • Minimum Android 13 (API 33). No backward support.
*   **SDK & Permissions**:\
    • UsageStatsManager and NotificationListenerService are available and enabled by user.
*   **Offline Operation**:\
    • No Internet required or used; all features rely on local device.
*   **User Environment**:\
    • Users grant required permissions; failure to grant will disable core features.

## 8. Known Issues & Potential Pitfalls

*   **Battery Consumption**\
    • Frequent sampling may drain battery.\
    • Mitigation: use WorkManager or AlarmManager with adjustable intervals; batch database writes.
*   **Permission Denial**\
    • If Usage Access or Notification Access is denied, tracking fails.\
    • Mitigation: clear user messaging and reminder prompts to grant permissions.
*   **Large Data Volumes**\
    • Long-term retention can grow DB size.\
    • Mitigation: offer data cleanup options and limit in-memory queries.
*   **Chart Performance**\
    • Rendering highly detailed timelines might slow down older devices.\
    • Mitigation: limit data points, use sampling or down-sampling for visuals.
*   **Encryption Overhead**\
    • SQLCipher may add latency.\
    • Mitigation: cache frequent reads, use background threads for writes.

End of PRD. This document provides a clear, unambiguous reference for all future technical specifications, including tech stack details, UI guidelines, backend structure, and security rules.
