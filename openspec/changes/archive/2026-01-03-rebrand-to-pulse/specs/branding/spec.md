## ADDED Requirements

### Requirement: App Display Name
The application SHALL use "Pulse" as the brand name in all user-visible contexts.

#### Scenario: English display
- **WHEN** user device language is set to English
- **THEN** app name SHALL display as "Pulse"

#### Scenario: Traditional Chinese display
- **WHEN** user device language is set to Traditional Chinese
- **THEN** app name SHALL display as "脈動"

### Requirement: App Icon Design
The application icon SHALL adopt pulse wave and play button visual design with Adaptive Icon support.

#### Scenario: Adaptive Icon display
- **WHEN** device supports Adaptive Icon (API 26+)
- **THEN** icon SHALL display pulse wave foreground with dark gradient background
- **AND** icon SHALL correctly adapt to different launcher shape masks

#### Scenario: Legacy Icon display
- **WHEN** device does not support Adaptive Icon
- **THEN** icon SHALL display complete brand icon in square or round shape

### Requirement: Splash Animation
The application SHALL display brand animation with "pulse" effect during startup.

#### Scenario: Cold start animation
- **WHEN** app is launched from cold start
- **THEN** splash screen SHALL display pulsing animation effect
- **AND** animation SHALL complete within 2 seconds
- **AND** animation SHALL be smooth without stuttering

#### Scenario: Animation elements
- **WHEN** splash screen animation is playing
- **THEN** animation SHALL include pulse wave drawing effect
- **AND** animation SHALL include icon scale pulsing effect
- **AND** colors SHALL use brand primary colors (cyan-blue gradient)

### Requirement: Color System
Brand colors SHALL follow the defined color system.

#### Scenario: Primary color usage
- **WHEN** brand colors are applied to icon and animation
- **THEN** primary gradient SHALL be from Primary Cyan (#00F2FF) to Secondary Blue (#0066FF)

#### Scenario: Dark background
- **WHEN** rendering dark mode or splash screen background
- **THEN** background SHALL use dark gradient (#0A0E1A to #1A1625)
