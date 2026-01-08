# ui-consistency Spec Delta

## MODIFIED Requirements

### Requirement: Consistent Navigation Header
The application MUST use a standardized Top App Bar component across all main screens.

#### Scenario: Queue Screen Navigation (UPDATED)
- **GIVEN** the user opens the Queue (Up Next) screen
- **WHEN** they see the top bar
- **THEN** the navigation icon MUST be an ArrowBack icon (not Close/X)
- **AND** the navigation icon position MUST match other screens (same vertical alignment)
- **AND** the top bar height MUST match the home screen's compact Row layout

## ADDED Requirements

### Requirement: Queue Screen Song Duration Display
Each song in the queue MUST display its duration regardless of playback state.

#### Scenario: Currently Playing Song
- **GIVEN** a song is currently playing in the queue
- **WHEN** the user views the queue list
- **THEN** both the playing indicator AND the song duration MUST be visible
- **OR** the duration MUST be shown alongside the playing indicator (e.g., in a smaller font or separate position)

#### Scenario: Non-Playing Songs
- **GIVEN** songs in the queue are not currently playing
- **WHEN** the user views the queue list
- **THEN** each song MUST display its formatted duration (mm:ss or h:mm:ss)
