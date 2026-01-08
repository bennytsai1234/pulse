# ui-consistency Spec Delta

## MODIFIED Requirements

### Requirement: Consistent Navigation Header
The application MUST use a standardized Top App Bar component across all main screens.

#### Scenario: Navigation Button Alignment (UPDATED)
- **GIVEN** the user is on the Home Screen
- **WHEN** they see the Menu button at position (x, y)
- **AND** they navigate to ANY sub-screen via the drawer menu
  - Including: Settings, Playlists, Favorites, Discover (探索), Folders, Stats
- **THEN** the Back button on the sub-screen MUST appear at the exact same vertical position (y-coordinate) as the Menu button
- **AND** the horizontal position MUST align with the standard leading icon slot

## ADDED Requirements

### Requirement: Discover Screen Navigation
The Discover (探索) screen MUST provide standard navigation back functionality.

#### Scenario: Back Button Presence
- **GIVEN** the user navigates to the Discover screen from the drawer menu
- **WHEN** the screen loads
- **THEN** a back/navigation button MUST be visible in the top-left area
- **AND** tapping it MUST navigate back to the Home Screen

#### Scenario: Back Button Style Consistency
- **GIVEN** the Discover screen has a back button
- **THEN** the button icon MUST match other sub-screens (ArrowBack icon)
- **AND** the button size and tap target MUST match `PulseTopBarWithBack` standards (48dp touch target)
