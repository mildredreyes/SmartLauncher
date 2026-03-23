# SmartLauncher

An Android home screen launcher that automatically ranks and organises your apps by usage frequency and smart category detection.

## Features

- **AI-ranked grid** — apps sorted by a blended score: 70% usage frequency + 30% category priority
- **Smart categorisation** — 80+ package-prefix rules + label-keyword fallback covering:
  - Social (WhatsApp, Telegram, Instagram, etc.)
  - Work & Productivity (Gmail, Slack, Notion, etc.)
  - Entertainment & Media (Netflix, Spotify, YouTube, etc.)
  - Games (Supercell, EA, Mojang, etc.)
  - Shopping & Finance (Amazon, PayPal, Shopee, etc.)
  - Utilities & System (Camera, Maps, Clock, etc.)
- **Category filter chips** — tap a chip to filter to one category
- **Live search** — instant search across all app labels
- **Long-press** — opens the system app info sheet

## Architecture

```
data/
  AppCategory.kt          -- Enum of 7 categories with weights
  AppItem.kt              -- Data class: icon, label, score, category
  CategoryClassifier.kt   -- Package-prefix + keyword classifier
  AppRepository.kt        -- Loads apps, fetches UsageStats, blends scores

viewmodel/
  LauncherViewModel.kt    -- Filter, search, LiveData

ui/
  LauncherActivity.kt     -- Main screen: search bar, chip strip, grid
  AppGridAdapter.kt       -- RecyclerView ListAdapter with DiffUtil
  PermissionActivity.kt   -- Usage Access onboarding

res/layout/
  activity_launcher.xml
  item_app.xml
  activity_permission.xml
```

## Ranking formula

```
rankScore = (usageScore × 0.70) + (category.basePriority × 0.30)
```

`usageScore` is log-normalised foreground time over the last 30 days so that the single most-used app does not crush everything else.

## Project Documents

- [Copyright](copyright.md)
- [License](license.md)
- [Privacy Policy](privacy-policy.md)
- [Terms and Conditions](terms-and-conditions.md)

## Setup & Build

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- Kotlin 1.9+

### Steps

1. Clone / open the project in Android Studio
2. Let Gradle sync finish
3. Run on a device or emulator (API 26+)
4. On first launch, grant **Usage Access**:
   - Settings → Apps → Special app access → Usage access → SmartLauncher → Allow
5. The launcher will ask to be set as default home app — tap **Always**

### Build from CLI
```bash
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Permissions

| Permission | Why |
|---|---|
| `QUERY_ALL_PACKAGES` | List all installed apps |
| `PACKAGE_USAGE_STATS` | Read foreground usage times for ranking |

`PACKAGE_USAGE_STATS` is a **special permission** — the user must grant it manually in Settings. The app prompts them on first launch.

## Extending the classifier

Edit `CategoryClassifier.kt`. Add entries to `packageRules` (prefix match, checked first) or `labelKeywords` (substring match on the app label):

```kotlin
// packageRules — add before the closing bracket
"com.myapp.newapp" to AppCategory.WORK,

// labelKeywords — add before the closing bracket  
"crm" to AppCategory.WORK,
```

## Customising rank weights

In `AppRepository.kt`, change the coefficients:
```kotlin
val rankScore = (usageScore * 0.70f) + (category.basePriority * 0.30f)
//                             ^^^^                               ^^^^
//                          usage weight                    category weight
```

Raise the usage weight for a pure frequency ranking; raise the category weight to group similar apps together more strongly.
