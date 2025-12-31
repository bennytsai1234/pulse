# Spec: UI Polish

## MODIFIED Requirements

### Requirement: Fast Scroller Visibility
The alphabetical fast scroller MUST remain fully visible and interactive regardless of mini player state.

#### Scenario: Fast Scroller with Mini Player
Given the user is on the home screen with songs displayed
And the mini player is visible at the bottom
When the user views the fast scroller
Then the 'Z' section button MUST be fully visible and tappable
And the scroller MUST have sufficient bottom padding to clear the mini player (at least 80dp).

(Previously: Fast scroller had no bottom padding, causing 'Z' to be obscured)

### Requirement: Navigation Icon Alignment
All top app bars MUST use consistent heights and icon positioning.

#### Scenario: Consistent Back Button Position
Given the user navigates from Home to Settings
When viewing both screens' top bars
Then the Back button in Settings MUST appear in the same screen coordinates as the Menu button in Home.

(This requirement is a refinement of the existing ui-consistency spec)

## ADDED Requirements

### Requirement: Sleep Timer Execution
The sleep timer MUST correctly pause playback after the configured duration.

#### Scenario: Timer Duration Mode
Given the user sets a sleep timer for 5 minutes
When 5 minutes have elapsed
Then playback MUST pause
And the timer state MUST reset to OFF.

#### Scenario: Timer Track Count Mode
Given the user sets a sleep timer for 3 tracks
When 3 songs have completed playback
Then playback MUST pause after the 3rd song ends
And the timer state MUST reset to OFF.
