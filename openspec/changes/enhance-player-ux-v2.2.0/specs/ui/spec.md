# ui Spec Delta

## ADDED Requirements

### Requirement: Batch Selection Operations
The home screen MUST support batch selection operations for efficient music library management.

#### Scenario: Enter Selection Mode via Long Press
- **GIVEN** the user is on the home screen with songs displayed
- **WHEN** they long-press on any song item (~500ms)
- **THEN** the app enters selection mode
- **AND** the long-pressed song is automatically selected
- **AND** a selection action bar appears at the bottom

#### Scenario: Selection Action Bar
- **GIVEN** the app is in selection mode
- **WHEN** the user views the bottom action bar
- **THEN** it MUST display:
  - Selected count (e.g., "3 selected")
  - Delete button (trash icon)
  - Add to Playlist button
- **AND** tapping the Delete button triggers batch deletion with confirmation
- **AND** tapping Add to Playlist shows playlist selection/creation dialog

#### Scenario: Batch Delete
- **GIVEN** songs are selected in selection mode
- **WHEN** the user taps Delete and confirms
- **THEN** all selected songs are deleted from the device (respecting Scoped Storage on Android 11+)
- **AND** a success/failure feedback is shown

#### Scenario: Batch Add to Playlist
- **GIVEN** songs are selected in selection mode
- **WHEN** the user taps Add to Playlist
- **THEN** a dialog appears showing existing playlists
- **AND** option to create a new playlist
- **AND** selecting a playlist adds all selected songs to it

#### Scenario: Exit Selection Mode
- **GIVEN** the app is in selection mode
- **WHEN** the user presses the back button/gesture
- **OR** taps elsewhere to deselect all
- **THEN** the app exits selection mode
- **AND** the selection action bar disappears
