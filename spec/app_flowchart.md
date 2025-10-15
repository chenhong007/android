flowchart TD
  A[Launch App] --> B{First time user?}
  B --Yes--> C[Onboarding]
  B --No--> D[Home]
  C --> E[Grant Permissions]
  E --> D
  D --> F[Statistics]
  D --> G[App Management]
  D --> H[History]
  D --> I[Settings]
  F --> J[Filter Data]
  F --> K[Export Data]
  G --> L[Set Usage Limit]
  H --> M[View Past Usage]
  I --> N[Configure Reminders]
  I --> O[Privacy Settings]
  L --> G
  J --> F
  K --> F
  M --> H
  N --> I
  O --> I