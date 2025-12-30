## 1. Home Screen Integration
- [x] 1.1 Update `HomeScreenRedesigned.kt`:
    - [x] Add `showSleepTimer` boolean state.
    - [x] Add "Sleep Timer" `DropdownMenuItem` to the top-bar menu.
        - [x] Icon: `Icons.Rounded.Timer`
        - [x] Action: Set `showSleepTimer = true`.
    - [x] Implement conditional rendering of `SleepTimerBottomSheet` when `showSleepTimer` is true.
        - [x] Pass `onDismiss = { showSleepTimer = false }`.

## 2. Testing
- [x] 2.1 Verify "Sleep Timer" menu item appears.
- [x] 2.2 Verify BottomSheet opens correctly.
- [x] 2.3 Verify timer functions (set time, cancel) work via the BottomSheet.
