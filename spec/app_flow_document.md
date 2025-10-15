# App Flow Document

## Onboarding and Sign-Up

When a user installs the app on their Android device and launches it for the first time, they see a welcome screen that briefly explains the main benefits of the TimeSpent App, namely accurate usage tracking, clear visual summaries, and full privacy. There is no account sign-up or login required, since all data remains on the device. Immediately after the welcome screen, the app prompts the user to grant two essential permissions: Usage Access and Notification Access. Each permission request is accompanied by a short explanation of why it is needed and how it improves the app’s accuracy. If the user grants both permissions, the app initializes its local encrypted database and proceeds to the Home screen. If the user denies a permission, a friendly message explains that some features will be limited and offers a button to open the system settings page where they can enable the permission later. The user can move forward with partial functionality or return to grant permissions at any time.

## Main Dashboard or Home Page

Once permissions are in place, the app displays the Home screen as the default view. At the top, the user sees a greeting adjusted to the time of day, such as “Good Morning,” followed by a prominent card showing today’s total screen time and a comparison to yesterday’s usage. Below that, two smaller cards present the unlock count and total screen-on duration with simple progress bars and trend arrows. A list of the five most-used applications appears next, each entry showing the app icon, name, usage time, and percentage share. At the bottom of the screen, a fixed navigation bar with five icons allows quick access to the other main sections: Statistics, App Management, Reminders, History, and a floating menu for General Settings. Tapping any icon instantly transitions the user to that section while preserving the state of the Home screen for easy return.

## Detailed Feature Flows and Page Transitions

### Usage Statistics Flow

When the app is running in the background, it continuously monitors foreground app sessions using the Android UsageStatsManager. It records each app’s start and end timestamps, filters out very short or idle sessions, and writes encrypted records to the local Room database. The data sampling frequency adjusts based on the chosen performance mode. There is no visible UI for this background process, but the user can trust that the app is capturing usage accurately.

### Statistics Screen Flow

Tapping the Statistics icon in the bottom bar brings the user to a screen titled “Usage Statistics.” At the top, a date picker bar shows quick buttons for Today, Yesterday, This Week, This Month, and a Custom Range option. Choosing Custom Range overlays a calendar picker where the user selects start and end dates. Below the picker, three summary cards display total screen time, number of apps used, and average session length. Further down, a tab control lets the user switch among bar charts, ring charts, and a 24-hour timeline. Swiping left or right changes chart types, and pinch gestures zoom in on any chart. Tapping on a bar or slice opens a detail overlay with breakdown by app or time period. A back arrow in the toolbar returns the user to the Home page at any time.

### App Management Flow

Selecting the App Management icon navigates to the “App Management” screen. Here the user sees a row of summary cards indicating total installed apps, apps used today, and apps not used for a long time. Below these cards is a searchable list of all installed applications. The user can enter text in the search field to filter by name or category. Long-pressing an app entry switches to multi-select mode, highlighting selected apps and revealing a bottom action bar where the user can choose to add apps to a blacklist or remove usage limits. Tapping the sort button in the toolbar presents a dialog for sorting by usage time, frequency, installation date, or app size. Exiting multi-select mode returns the screen to its normal state.

### Reminder Configuration Flow

When the user taps the Reminders icon, they land on the “Reminder Settings” screen. At the top is a master switch labeled “Enable Reminders.” Turning it on expands additional settings. The first section allows setting a global screen time threshold using a slider or numeric input, and choosing an alert style such as notification, vibration, or popup. The next section configures per-app limits: tapping “Add App” opens a searchable list where the user picks an app and sets its personal threshold and reminder percentages (for example 80%, 90%). Further down, the user can define rest reminders by setting an interval and recommended break duration. Finally, a Do-Not-Disturb block can be scheduled by choosing start and end times and selecting emergency apps that bypass restrictions. Each setting change instantly updates a live preview area showing how the reminder will appear. A save button at the bottom locks in all changes and returns the user to the Home screen.

### History Browsing and Export Flow

By tapping the History icon, the user opens the “History” screen, which initially displays a monthly calendar heatmap. Each date cell is shaded to represent usage intensity. Tapping a day expands a pop-up with that day’s detailed stats. Swiping left or right navigates to the previous or next month. Below the calendar, a segment control lets the user view weekly, quarterly, or annual trend lines. A section of comparison cards shows week-over-week and month-over-month changes with arrows and percentages. At the bottom, the user finds an Export button. Pressing it opens a modal dialog where the user selects CSV, PDF, or JSON format and confirms the date range. Once the export begins, a progress bar indicates status and estimated time. The process runs in the background and presents a toast notification when complete. If the data exceeds the single-file limit, the app creates multiple files and names them sequentially.

### Local Backup and Restore Flow

Accessed from the floating General Settings menu, the user taps “Backup and Restore.” On the Backup tab, a single button labeled “Export Encrypted File” generates a local file containing all usage data and settings. A system file picker appears for the user to choose storage location. On the Restore tab, the user taps “Import Backup File,” selects a previously generated file, and the app validates its integrity. If the file matches the expected format, the app decrypts it, merges or replaces existing data, and displays a confirmation message before navigating back to the Home screen. If validation fails, a clear error message explains the problem and prompts the user to try again.

## Settings and Account Management

Although the app does not support user accounts, a General Settings menu is available via an icon in the top toolbar on any screen. Within General Settings, the user can switch between English and Chinese languages, toggle Light or Dark theme following the system preference, and set the performance mode (Standard, High Precision, Energy Saving). There is also an option to clear all local data or reset reminder configurations to default. All changes apply immediately, and the app remains in its current tab so the user can continue without disruption. A back arrow in the toolbar returns the user to the previously viewed section.

## Error States and Alternate Paths

If the user denies Usage Access or Notification Access at any point, the app disables the corresponding tracking features and shows a persistent banner explaining which data cannot be collected. Tapping the banner opens the system settings screen so the user can grant the needed permission. When entering an invalid value in a numeric field, such as negative thresholds or non-numeric characters, the input box highlights in red and a small inline message clarifies the valid range. During export or backup, if the device runs out of storage or the file picker operation is canceled, an alert dialog informs the user of the failure and offers to retry. If a backup file fails validation during restore, the app displays an error dialog with details and returns the user to the restore tab. Any unexpected crash in the UI triggers a simple recovery screen with a button to restart the app and preserve unsaved settings.

## Conclusion and Overall App Journey

From the moment a user installs TimeSpent App, they are guided through a friendly welcome and permission requests, then dropped into a concise Home screen that summarizes daily phone usage. Through the bottom navigation, they explore in-depth statistics with interactive charts, manage applications and set personalized limits, configure reminders and Do-Not-Disturb schedules, review historical trends, and export or restore their data locally. All settings and preferences apply instantly, and the app responds gracefully to missing permissions or input errors. With no login required and full offline operation, each user can easily discover, track, and control their smartphone behavior in a clear and private environment every day.
