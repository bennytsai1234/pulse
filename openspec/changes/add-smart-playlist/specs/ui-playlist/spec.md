# ui-playlist Spec Delta

> **Change ID**: `add-smart-playlist`
> **類型**: ADDED

---

## ADDED Requirements

### Requirement: Smart Playlist Editor Screen

The application MUST provide a dedicated screen for creating and editing smart playlists.

#### Scenario: Editor Layout
- **GIVEN** the user opens the smart playlist editor
- **WHEN** the screen loads
- **THEN** the following sections are displayed: Name/Icon editor, Logic selector (AND/OR), Rule list, Sort options, Limit option, Live preview

#### Scenario: Add Rule Condition
- **GIVEN** the user is in the editor
- **WHEN** the user taps "Add Condition"
- **THEN** a new rule row is added with dropdowns for: condition type, operator, value input

#### Scenario: Live Preview
- **GIVEN** rules are defined
- **WHEN** rules change
- **THEN** matching songs are queried with 300ms debounce
- **AND** song count and preview list are displayed

---

### Requirement: Smart Playlist List Display

The playlist section MUST display smart playlists distinctly from regular playlists.

#### Scenario: Visual Distinction
- **GIVEN** the user views the playlist list
- **WHEN** smart playlists are present
- **THEN** they are displayed in a separate "Smart Playlists" section
- **AND** each item shows: icon, name, estimated song count

#### Scenario: System vs Custom
- **GIVEN** both system and custom smart playlists exist
- **WHEN** viewing the list
- **THEN** system playlists show a "system" indicator
- **AND** custom playlists show edit/delete options

---

### Requirement: Smart Playlist Detail Screen

The application MUST provide a detail view for playing and managing smart playlist contents.

#### Scenario: View Contents
- **GIVEN** the user taps a smart playlist
- **WHEN** the detail screen opens
- **THEN** matching songs are queried and displayed
- **AND** play all, shuffle, and queue options are available

#### Scenario: Play Smart Playlist
- **GIVEN** the user is on a smart playlist detail
- **WHEN** "Play All" is tapped
- **THEN** all matching songs are added to queue and playback starts
