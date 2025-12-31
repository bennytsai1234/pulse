# Proposal: Fix Player Transition Consistency

## Goal
To standardize the visual transition when collapsing or expanding the full-screen player, ensuring a smooth and consistent user experience.

## Problem
The user reports inconsistent animation behavior when returning from the full-screen player to the main screen.
Analysis of `MainScreen.kt` reveals that the `AnchoredDraggableState` uses a `tween(150, LinearEasing)` for its `snapAnimationSpec`.
- **150ms** is extremely fast for a full-screen transition (Standard is ~300-400ms).
- **LinearEasing** looks robotic and can feel like a "snap" or "cut" rather than an animation, especially if meaningful frames are dropped.
- In contrast, dragging (gesture) feels 1:1 and smooth. This discrepancy causes the "inconsistent" feeling.

## Solution
1.  **Update Animation Spec**: Replace the 150ms linear tween with a standard Material Design motion spec.
    - **Duration**: `350ms` or similar (Goldilocks zone for full screen).
    - **Easing**: `FastOutSlowInEasing` (Standard Material easing) or a standard `Spring` spec.
    - We will use `tween(400, params = FastOutSlowInEasing)` for a premium feel.

2.  **Verify Usage**: Ensure all programmatic calls (`animateTo`) implicitly use this spec (they do, as they use the state's internal spec by default unless overridden).

## Risks
- None. This is a pure UI polish change.

## Verification
- Open Player -> Click "Back" -> Should smoothly slide down (approx 0.4s).
- Open Player -> Click "Collapse" Chevron -> Should smoothly slide down.
- Drag Player -> Should still track finger.
