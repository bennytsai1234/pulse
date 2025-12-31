# Proposal: Unify Top App Bar Design

## Goal
The goal of this change is to standardize the design of the Top App Bar across the application, specifically addressing the inconsistency in the positioning of the navigation icon (Menu/Back button) between the Main Screen and other screens (Settings, etc.).

## Problem
Currently, the `HomeScreen` uses a custom-built top bar with a height of `48.dp` and specific padding. Other screens, like `SettingsScreen`, use the standard Material 3 `CenterAlignedTopAppBar` (typically `64.dp`). This results in a visual disconnect where the menu button and back buttons jump position when navigating.

## Solution
1.  **Introduce `GeminiTopBar`**: Create a reusable Top App Bar component in `GeminiComponents.kt` that wraps Material 3's `CenterAlignedTopAppBar` (or `TopAppBar`). This guarantees consistent height, padding, and typography.
2.  **Migrate `HomeScreen`**: Refactor `HomeScreen` to use `GeminiTopBar` instead of the custom `Row`-based implementation.
3.  **Migrate Other Screens**: Ensure `SettingsScreen` and others use `GeminiTopBar` to enforce the single source of truth for header design.

## Risks
- The `HomeScreen` header contains multiple action buttons (Play, Select All, Add, Delete) in selection mode. The `GeminiTopBar` must be flexible enough to handle these complex action states or accept `actions` composable content.
- Visual regression: The "search bar" feel of the home screen might change, but consistency is the user's priority.

## Verification
- Visual inspection: Toggle between Home and Settings; the Back/Menu icons should align perfectly.
