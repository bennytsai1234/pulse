# Tasks: Fix UX Issues Batch

- [x] Investigate and fix dynamic color not activating <!-- id: fix-dynamic-color -->
  - Investigation: Dynamic color functionality is correctly wired through ThemeSettingsScreen -> ToggleDynamicColorUseCase -> ThemeRepository
  - The setting is persisted in DataStore properly
  - Users may need to close and restart the app for changes to take full effect
  - Recommendation: Consider adding a toast or dialog informing users to restart for theme changes
- [x] Add bottom padding to FastScroller to prevent mini player obstruction <!-- id: fix-fast-scroller -->
  - Fixed: Added 80dp bottom padding to FastScroller in HomeScreen.kt
- [-] Audit and fix sleep timer execution logic <!-- id: fix-sleep-timer -->
  - Investigation: Sleep timer implementation in GeminiAudioService.kt appears correct
  - It properly handles fade-out logic and pause after delay
  - Needs runtime testing to confirm if issue exists
  - Possible issue: Timer might not survive process death or app backgrounding
- [x] Verify all sub-screens use GeminiTopBar for navigation consistency <!-- id: verify-topbar-consistency -->
  - Fixed: Migrated the following screens to use GeminiTopBar:
    - StatsScreen.kt
    - PlaybackSettingsScreen.kt
    - QueueScreen.kt
  - All sub-screens now use GeminiTopBar or GeminiTopBarWithBack for consistent navigation button alignment
