## ADDED Requirements
### Requirement: Sleep Timer UI
The App SHALL provide a user interface to schedule automatic playback termination.

#### Scenario: Set preset timer
- **WHEN** the user selects "Sleep Timer" from the menu
- **AND** chooses a preset duration (e.g., 30 minutes)
- **THEN** the system requests the audio service to stop playback after that duration
- **AND** a confirmation message is shown

#### Scenario: Cancel timer
- **WHEN** the user opens the Sleep Timer dialog while a timer is active
- **AND** selects "Turn Off Timer" (or similar cancellation option)
- **THEN** the pending sleep timer is cancelled
