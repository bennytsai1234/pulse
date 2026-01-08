# ui-polish Spec Delta

## ADDED Requirements

### Requirement: Enhanced Karaoke Lyrics Animation
The lyrics view MUST provide a visually engaging karaoke-style experience where the current line appears to "float" or "pop out".

#### Scenario: Current Line Float Animation
- **GIVEN** the user is viewing lyrics in the full-screen player
- **WHEN** the song progresses to a new lyric line
- **THEN** the current line MUST animate upward (translationY: -8dp)
- **AND** the current line MUST have a glow/shadow effect for emphasis
- **AND** the current line MUST scale up (1.1x) with smooth easing
- **AND** surrounding lines MUST have reduced opacity

### Requirement: Lyrics Toggle Accessibility
The user MUST be able to toggle between album artwork and lyrics through multiple methods.

#### Scenario: Toggle via Album Art Tap
- **GIVEN** the user is viewing the album artwork
- **WHEN** they tap the artwork
- **THEN** the view switches to lyrics

#### Scenario: Toggle via Lyrics Tap
- **GIVEN** the user is viewing lyrics
- **WHEN** they tap anywhere on the lyrics view
- **THEN** the view switches back to album artwork

#### Scenario: Toggle via Control Button
- **GIVEN** the user is in the full-screen player
- **WHEN** they tap the lyrics button in the control area
- **THEN** the view toggles between album artwork and lyrics
