# Proposal: Fix UX Issues Batch

## Goal
Address four user-reported UX issues to improve the overall application experience.

## Problems Identified
1. **Dynamic Color Activation**: Material 3 dynamic color (Material You) feature appears inactive after being enabled in settings
2. **Fast Scroller Obstruction**: The alphabetical fast scroller on the main screen has its bottom letter (Z) obscured by the mini player
3. **Sleep Timer Functionality**: Sleep timer may not be operating correctly
4. **Navigation Button Misalignment**: Back buttons in sub-screens (e.g., from drawer menu items) are not vertically aligned with the menu button on the home screen

## Solutions

### 1. Dynamic Color Investigation
- Verify the theme toggle switch is properly wired to user preferences
- Ensure MainActivity correctly observes and applies the `useDynamicColor` preference
- Check that NowPlayingScreen's dynamic gradient doesn't conflict with global dynamic theming

### 2. Fast Scroller Bottom Padding
- Add appropriate bottom padding to the FastScroller component to account for mini player height (72-80dp)
- Ensure the scroller remains fully interactive even when mini player is visible

### 3. Sleep Timer Validation
- Review SleepTimerViewModel logic for duration countdown
- Verify MusicServiceConnection properly invokes the timer on the service
- Check timer cancellation and pause/resume behavior

### 4. Navigation Icon Consistency
- This should已經 be addressed by the `unify-top-app-bar-design` change
- If issues persist, verify all sub-screens (Settings, Theme Settings, Playback Settings, etc.) use `GeminiTopBar`
- Check that drawer menu items correctly navigate and that TopBar height is consistent

## Risks
- **Dynamic Color**: Forcing recreation of MainActivity activity might be needed for theme changes to take effect
- **Fast Scroller**: Adding bottom padding might affect gesture detection area
- **Sleep Timer**: Changes to timer logic could impact playback state

## Verification
1. Enable dynamic color in settings → restart app → verify NowPlayingScreen background adapts to album art AND system colors are applied globally
2. Scroll to songs starting with 'Z' → verify the 'Z' section button is fully visible and tappable
3. Set sleep timer for 1 minute → verify music pauses after countdown
4. Navigate Home → Settings → Verify back button aligns with home menu button
