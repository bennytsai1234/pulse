# Tasks: Fix Player Transition Consistency

- [x] Update `MainScreen.kt` `AnchoredDraggableState` animation spec to `tween(400, FastOutSlowInEasing)` <!-- id: update-anim-spec -->
- [x] Verify `MiniPlayer` expand animation uses the same consistent feel <!-- id: verify-expand -->
- [x] Refine `SwipeablePlayerSheet` alpha transition logic (Mini fades out early, Full fades in late) <!-- id: refine-alpha -->
