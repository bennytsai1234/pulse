# player Specification

## Purpose
TBD - created by archiving change fix-core-gaps. Update Purpose after archive.
## Requirements
### Requirement: Media Browser Support (Android Auto)
The system MUST provide a browsable media hierarchy for external consumers (Android Auto, Bluetooth Browsers).

#### Scenario: Browsing Root Categories
- **WHEN** a media browser connects and requests the root (GET_LIBRARY_ROOT)
- **THEN** the system returns a root node containing "All Songs", "Albums", "Artists", and "Recently Added".

#### Scenario: Browsing Category Content
- **WHEN** a media browser selects the "All Songs" category
- **THEN** the system returns a list of all playable songs sorted by title.

### Requirement: Crossfade Playback

The player MUST support crossfade (cross-fading) between consecutive tracks, allowing the outgoing track to fade out while the incoming track fades in simultaneously.

#### Scenario: Automatic Crossfade on Track End
- **WHEN** the current track reaches the crossfade trigger point (track duration minus crossfade duration)
- **AND** crossfade is enabled in settings
- **THEN** the system begins fading out the current track
- **AND** simultaneously prepares and fades in the next track
- **AND** both tracks play concurrently during the transition period
- **AND** at the end of the transition, only the new track is audible at full volume

#### Scenario: Crossfade Duration Configuration
- **GIVEN** the user is on the Crossfade Settings screen
- **WHEN** the user adjusts the duration slider
- **THEN** values between 1 second and 12 seconds are available
- **AND** the selected duration is persisted across app restarts
- **AND** takes effect on the next track transition

#### Scenario: Crossfade Curve Selection
- **GIVEN** the user is on the Crossfade Settings screen
- **WHEN** the user selects a fade curve (Linear, Exponential, S-Curve)
- **THEN** the volume animation uses the selected curve formula
- **AND** Linear produces constant rate change
- **AND** Exponential produces slow start, fast end
- **AND** S-Curve produces smooth acceleration and deceleration

#### Scenario: Manual Skip with Crossfade
- **GIVEN** "Apply on Manual Skip" is enabled in settings
- **WHEN** the user manually skips to the next or previous track
- **THEN** the crossfade transition is applied
- **AND** if disabled, the track switches immediately without fade

#### Scenario: Album Continuous Mode
- **GIVEN** "Album Continuous Mode" is enabled in settings
- **WHEN** transitioning between two tracks from the same album
- **THEN** crossfade is NOT applied
- **AND** gapless playback is used instead for seamless album experience

#### Scenario: Short Track Handling
- **GIVEN** a track's duration is shorter than the configured crossfade duration
- **WHEN** playing this track
- **THEN** the system uses a reduced crossfade duration (50% of track length)
- **AND** normal crossfade behavior applies with the adjusted duration

#### Scenario: Disable Crossfade
- **GIVEN** crossfade is disabled in settings
- **WHEN** any track transition occurs
- **THEN** no fade effects are applied
- **AND** tracks switch immediately as in standard playback

### Requirement: Dual Player Resource Management

The system MUST efficiently manage resources when using dual ExoPlayer instances for crossfade.

#### Scenario: Memory Efficiency
- **WHEN** crossfade is enabled
- **THEN** the secondary player is only fully prepared when approaching a transition
- **AND** after transition completes, the old player's media is cleared
- **AND** total memory increase does not exceed 15MB compared to single-player mode

#### Scenario: Graceful Degradation
- **GIVEN** the device is low on available memory
- **WHEN** the system detects insufficient resources for dual playback
- **THEN** crossfade is temporarily disabled
- **AND** standard single-player mode is used
- **AND** the user is NOT interrupted or shown error dialogs

