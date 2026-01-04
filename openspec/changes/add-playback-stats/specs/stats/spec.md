# stats Spec Delta

> **Change ID**: `add-playback-stats`
> **類型**: ADDED (新建 Spec)

---

## Purpose

This specification defines the requirements for tracking, storing, and analyzing user playback behavior to provide meaningful listening insights.

---

## ADDED Requirements

### Requirement: Playback Tracking

The system MUST track user playback behavior in real-time and persist the data for statistical analysis.

#### Scenario: Record Complete Playback
- **WHEN** a user plays a song until completion (≥80% of duration OR ≥4 minutes played)
- **THEN** the system records this as a "completed" play
- **AND** increments the song's play count by 1
- **AND** adds the played duration to the song's total listening time
- **AND** updates the song's "last played" timestamp
- **AND** updates the daily listening statistics

#### Scenario: Record Skipped Playback
- **WHEN** a user plays a song for less than 30 seconds and then skips
- **THEN** the system records this as a "skipped" play
- **AND** increments the song's skip count by 1
- **AND** does NOT increment the song's play count
- **AND** still updates the daily listening time (actual time played)

#### Scenario: Handle Interrupted Playback
- **WHEN** playback is interrupted (app killed, device shutdown, call)
- **THEN** the system persists the current session's progress before termination
- **AND** on next app launch, finalizes the interrupted session appropriately

#### Scenario: Pause and Resume
- **WHEN** a user pauses and resumes the same song
- **THEN** the playback is tracked as a single continuous session
- **AND** pause duration is NOT counted toward listening time

---

### Requirement: Daily Statistics Aggregation

The system MUST maintain daily aggregated statistics for efficient trend analysis.

#### Scenario: Daily Summary
- **WHEN** playback occurs on any given day
- **THEN** the system maintains a daily record containing:
  - Total listening duration (milliseconds)
  - Total songs played (count)
  - Unique songs played (count)
- **AND** this data is aggregated in near real-time during playback

#### Scenario: Historical Data Retention
- **GIVEN** the system has been tracking for over 1 year
- **WHEN** storage optimization runs
- **THEN** detailed playback history older than 1 year MAY be pruned
- **BUT** daily aggregated statistics are retained indefinitely
- **AND** song-level statistics (play count, total time) are never pruned

---

### Requirement: Listening Streak Calculation

The system MUST calculate the user's current consecutive listening streak.

#### Scenario: Active Streak
- **GIVEN** the user has listened to music today
- **AND** the user listened yesterday
- **AND** the user listened the day before
- **WHEN** the streak is calculated
- **THEN** the streak count includes all consecutive days with listening activity

#### Scenario: Today Pending
- **GIVEN** the user has NOT yet listened to music today
- **AND** the user listened yesterday
- **WHEN** the streak is calculated
- **THEN** the streak includes yesterday and prior consecutive days
- **AND** today is NOT counted as breaking the streak until midnight

#### Scenario: Streak Break
- **GIVEN** the user did NOT listen yesterday
- **WHEN** the streak is calculated
- **THEN** the streak resets to 0 (or 1 if listened today)

---

### Requirement: Top Played Rankings

The system MUST provide rankings of most-played content.

#### Scenario: Top Songs
- **WHEN** the user views "Top Songs"
- **THEN** songs are ranked by play count in descending order
- **AND** each entry shows: song info, play count, total listening time

#### Scenario: Top Artists
- **WHEN** the user views "Top Artists"
- **THEN** artists are ranked by cumulative play count of their songs
- **AND** each entry shows: artist name, total plays, total listening time

#### Scenario: Top Albums
- **WHEN** the user views "Top Albums"
- **THEN** albums are ranked by cumulative play count of their songs
- **AND** each entry shows: album info, total plays, total listening time

---

### Requirement: Listening Trend Analysis

The system MUST provide historical listening trend data.

#### Scenario: Weekly Comparison
- **WHEN** the user views the statistics overview
- **THEN** the system displays this week's total listening time
- **AND** shows the percentage change compared to last week
- **AND** positive change is indicated visually (e.g., green, upward arrow)
- **AND** negative change is indicated visually (e.g., red, downward arrow)

#### Scenario: Trend Chart Data
- **WHEN** the user views the listening trend chart
- **THEN** the system provides daily listening data for the past 14 days
- **AND** data is displayed as a bar chart or line graph
- **AND** each data point shows the date and duration when tapped/hovered
