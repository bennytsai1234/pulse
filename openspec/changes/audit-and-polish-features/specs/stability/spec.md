# Stability and Quality Requirements

## ADDED Requirements

### Requirement: Architecture Stability
> The application MUST maintain state consistency across lifecycle events.

#### Scenario: Process Death
Given the app is in the background playing music
When the system kills the UI process to reclaim memory
And the user returns to the app
Then the Now Playing screen SHOULD restore the correct song and playback state (Playing/Paused).

#### Scenario: Configuration Change
Given the user is in a deeply nested screen (e.g., Album Detail)
When the device rotates or theme changes
Then the navigation stack MUST be preserved.

### Requirement: Error Resilience
> The application MUST NOT crash on expected error conditions.

#### Scenario: Missing File
Given a song is listed in the database but the file has been deleted externally
When the user tries to play the song
Then the app SHOULD show a polite "File not found" toast and skip to the next track
And the app MUST NOT crash.

#### Scenario: Permission Denial
Given the user denied Storage permissions
When the user opens the Home screen
Then the app SHOULD show a rationale dialog or empty state with a "Grant Permission" button.
