# ui-navigation Specification

## Purpose
TBD - created by archiving change fix-player-transition-consistency. Update Purpose after archive.
## Requirements
### Requirement: Player Sheet Animation
The full-screen player sheet MUST animate smoothly during programmatic state changes (expand/collapse).

#### Scenario: Collapse via Back Trigger
Given the player is Expanded
When the user presses the system Back button or the UI Collapse button
Then the sheet MUST animate to the Collapsed state with a duration of at least 300ms and standard easing (FastOutSlowIn).
(Previously: Unspecified/Implementation detail resulted in 150ms linear snap)

#### Scenario: Expand via Mini Player
Given the player is Collapsed
When the user taps the Mini Player
Then the sheet MUST animate to the Expanded state with the same consistent easing and duration.

