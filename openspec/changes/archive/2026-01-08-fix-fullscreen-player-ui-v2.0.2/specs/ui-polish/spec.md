# ui-polish Spec Delta

## MODIFIED Requirements

### Requirement: Alphabetical Fast Scroller Visibility
The alphabetical fast scroller MUST remain fully visible and interactive regardless of mini player state.

#### Scenario: Fast Scroller with Mini Player Present
- **GIVEN** the user is on the Home Screen with songs loaded
- **AND** the Mini Player is visible at the bottom
- **WHEN** the user looks at the alphabetical fast scroller on the right edge
- **THEN** all 27 characters (A-Z and #) MUST be fully visible
- **AND** all characters MUST be tappable and respond to touch/drag gestures
- **AND** the fast scroller column MUST fit within the visible area above the Mini Player

#### Scenario: Compact Alphabet Spacing
- **GIVEN** the fast scroller needs to display 27 characters above the Mini Player
- **WHEN** the available vertical space is limited
- **THEN** the character spacing MUST be compact enough to fit all characters
- **AND** each character MUST meet the minimum touch target size of 44dp in tap detection area (can overlap via `pointerInput` gesture detection)
