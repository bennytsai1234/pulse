# playlist Spec Delta

> **Change ID**: `add-smart-playlist`
> **類型**: ADDED

---

## ADDED Requirements

### Requirement: Smart Playlist Definition

The system MUST support rule-based dynamic playlists that automatically include songs matching specified criteria.

#### Scenario: Create Smart Playlist
- **GIVEN** the user is on the playlist creation screen
- **WHEN** the user selects "Smart Playlist"
- **THEN** the Smart Playlist Editor is displayed
- **AND** the user can define name, icon, rules, and sorting

#### Scenario: Define Rule Conditions
- **GIVEN** the user is editing a smart playlist
- **WHEN** the user adds rule conditions
- **THEN** condition types include: Duration, Play count, Added date, Last played, Artist, Album, Title, Is favorite

#### Scenario: Combine Multiple Rules
- **GIVEN** multiple rule conditions exist
- **WHEN** the user selects logic mode
- **THEN** "Match ALL" (AND) or "Match ANY" (OR) can be selected

---

### Requirement: System Smart Playlists

The system MUST provide pre-defined smart playlists for common use cases.

#### Scenario: Access System Playlists
- **GIVEN** the user opens the playlist section
- **WHEN** viewing smart playlists
- **THEN** system playlists are available: Recently Added, Most Played, Long Unplayed, Short Songs, Long Songs, Favorites

---

### Requirement: Smart Playlist Dynamic Update

Smart playlists MUST reflect current library state without manual refresh.

#### Scenario: New Song Matches Rule
- **GIVEN** a smart playlist with date-based rule
- **WHEN** a new song is added
- **THEN** it appears immediately if matching
