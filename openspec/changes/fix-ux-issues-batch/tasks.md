# Tasks: Fix UX Issues Batch

- [x] Investigate and fix dynamic color not activating <!-- id: fix-dynamic-color -->
  - **ROOT CAUSE**: `NowPlayingScreen` was not extracting album artwork colors to its own ViewModel
  - **FIXED**: Connected `HeroImage.onImageLoaded` to trigger `NowPlayingEvent.UpdatePalette`
  - Now album artwork colors are properly extracted and applied to the full-screen player background gradient
  - Colors are also passed to `MainViewModel` for MiniPlayer theming
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
