# Spec: UI Consistency

## ADDED Requirements

### Requirement: Consistent Navigation Header
The application MUST use a standardized Top App Bar component across all main screens.

#### Scenario: Navigation Button Alignment
Given the user is on the Home Screen
When they see the Menu button
And they navigate to the Settings Screen
Then the Back button on the Settings Screen MUST appear in the exact same screen coordinates as the Menu button (relative to safe area).

#### Scenario: Header Height Consistency
Given the user considers the top header
The visual height of the header background MUST be identical across Home, Settings, and other secondary screens.

### Requirement: Material 3 Compliance
The unified header MUST use Material 3 tokens for sizing and spacing.

#### Scenario: Standard Touch Targets
The navigation icon MUST have a minimum touch target size of 48x48dp, centered within the standard leading icon slot of the Top App Bar.
