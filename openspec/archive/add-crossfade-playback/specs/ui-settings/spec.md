# ui-settings Spec Delta

> **Change ID**: `add-crossfade-playback`
> **類型**: ADDED

---

## ADDED Requirements

### Requirement: Crossfade Settings Screen

The application MUST provide a dedicated settings interface for configuring crossfade playback behavior.

#### Scenario: Access Crossfade Settings
- **GIVEN** the user is on the Playback Settings screen
- **WHEN** the user taps "Crossfade" or "交叉淡入淡出"
- **THEN** the Crossfade Settings screen is displayed
- **AND** shows the current crossfade configuration

#### Scenario: Toggle Crossfade
- **GIVEN** the user is on the Crossfade Settings screen
- **WHEN** the user toggles the main crossfade switch
- **THEN** the setting is immediately persisted
- **AND** dependent options are enabled/disabled accordingly
- **AND** when OFF, duration and curve options are visually dimmed

#### Scenario: Adjust Duration with Slider
- **GIVEN** the user is on the Crossfade Settings screen
- **AND** crossfade is enabled
- **WHEN** the user drags the duration slider
- **THEN** the slider displays values from 1s to 12s
- **AND** the current value is shown as a label (e.g., "5 秒")
- **AND** haptic feedback is provided at 1-second intervals
- **AND** the setting is persisted when the user releases the slider

#### Scenario: Select Fade Curve
- **GIVEN** the user is on the Crossfade Settings screen
- **AND** crossfade is enabled
- **WHEN** the user selects a curve option
- **THEN** one of three options can be selected: 線性 (Linear), 指數 (Exponential), S 曲線 (S-Curve)
- **AND** the selection is immediately persisted
- **AND** visual radio buttons indicate the current selection

#### Scenario: Configure Advanced Options
- **GIVEN** the user is on the Crossfade Settings screen
- **WHEN** the user views the "進階設定" (Advanced Settings) section
- **THEN** the following toggle options are available:
  - "手動跳轉時套用" (Apply on Manual Skip)
  - "專輯連續模式" (Album Continuous Mode)
  - "智慧靜音偵測" (Smart Silence Detection) - *optional*
- **AND** each option has a descriptive subtitle explaining its behavior

#### Scenario: Settings Persistence
- **GIVEN** the user has configured crossfade settings
- **WHEN** the user closes and reopens the app
- **THEN** all crossfade settings are restored to their previous values
- **AND** the crossfade behavior matches the persisted configuration
