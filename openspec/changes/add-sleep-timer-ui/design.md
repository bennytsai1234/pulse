# Design: Sleep Timer Integration

## Context
We found an existing, high-quality `SleepTimerBottomSheet` implementation. Instead of building a new Dialog, we will surface this robust component to the user.

## UI/UX Decisions
- **Access Point**: Add "Sleep Timer" to the Home Screen's `DropdownMenu` (3-dot menu).
- **Component**: Use `ModalBottomSheet` containing `SleepTimerBottomSheet`.
- **Consistency**: The Settings screen should ideally invoke the same BottomSheet or be removed if redundant. For this task, we will focus on **Home Screen integration**.

## Technical Implementation
- **ViewModel**:
    - `SleepTimerViewModel` already exists. We should use `hiltViewModel<SleepTimerViewModel>()` within the BottomSheet composable to keep logic encapsulated.
    - `HomeScreenRedesigned` only needs to manage the `showSleepTimer` boolean state.
- **Navigation**:
    - When "Sleep Timer" is clicked in the menu, set `showSleepTimer = true`.
    - Render `SleepTimerBottomSheet` when state is true.

## Risks
- **Dependency**: Ensure `SleepTimerViewModel` is correctly scoped and doesn't conflict with `HomeViewModel`'s lifecycle. Hilt should handle this fine.
