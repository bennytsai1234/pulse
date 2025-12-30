# Change: Integrate Sleep Timer BottomSheet

## Why
Users need easy access to the Sleep Timer from the Home Screen. The project already contains a high-quality `SleepTimerBottomSheet` component in the `ui` module, but it is currently underutilized and not accessible from the main navigation flow. The existing implementation in `SettingsScreen` is basic and inconsistent.

## What Changes
- **Feature**: Integrate the existing `SleepTimerBottomSheet` into the Home Screen menu.
- **Refactor**: Replace the basic sleep timer dialog in `SettingsScreen` with the shared `SleepTimerBottomSheet` (or link to it).
- **Architecture**: Ensure `HomeViewModel` or a shared `SleepTimerViewModel` correctly manages the sheet's state.

## Impact
- **Modules**: `ui`.
- **User Experience**: Provides a rich, consistent sleep timer interface with "End of Song" and "Track Count" features, elevating the perceived quality of the app.
