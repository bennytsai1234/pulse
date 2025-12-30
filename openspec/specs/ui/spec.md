# ui Specification

## Purpose
TBD - created by archiving change fix-core-gaps. Update Purpose after archive.
## Requirements
### Requirement: Batch Deletion
The system MUST support deleting multiple media files in a single user transaction.

#### Scenario: Batch Delete on Android 11+
- **WHEN** the user selects multiple songs and taps "Delete"
- **THEN** the system presents a **single** system dialog requesting permission to trash/delete all selected files.
- **AND** upon confirmation, all files are removed from the library and storage.

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

