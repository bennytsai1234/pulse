# ui-stats Spec Delta

> **Change ID**: `add-playback-stats`
> **類型**: ADDED (新建 Spec)

---

## Purpose

This specification defines the user interface requirements for displaying playback statistics and listening insights.

---

## ADDED Requirements

### Requirement: Statistics Overview Screen

The application MUST provide a dedicated screen for viewing listening statistics.

#### Scenario: Access Statistics
- **GIVEN** the user is on the Home screen or Settings
- **WHEN** the user taps "聆聽統計" (Listening Stats)
- **THEN** the Statistics Overview screen is displayed
- **AND** relevant data begins loading immediately

#### Scenario: Display Overview Cards
- **GIVEN** the user is on the Statistics screen
- **WHEN** the screen loads
- **THEN** the following overview cards are displayed:
  - "本週聆聽" (This Week) - hours/minutes, % change vs last week
  - "連續天數" (Streak) - current streak count
  - "總聆聽時長" (Total Time) - cumulative listening time

#### Scenario: Loading State
- **GIVEN** statistics are being calculated
- **WHEN** the Statistics screen is displayed
- **THEN** skeleton loading placeholders are shown
- **AND** actual data replaces placeholders once loaded (< 500ms)

#### Scenario: Empty State
- **GIVEN** the user has no listening history
- **WHEN** the Statistics screen is displayed
- **THEN** a friendly empty state message is shown
- **AND** a prompt to "Start Listening!" is displayed

---

### Requirement: Top Songs List

The Statistics screen MUST display a ranked list of most-played songs.

#### Scenario: View Top Songs
- **GIVEN** the user is on the Statistics screen
- **WHEN** the user views the "最常播放" (Top Played) section
- **THEN** the top 3-5 songs are displayed inline
- **AND** each item shows: rank medal (🥇🥈🥉), song title, artist, play count

#### Scenario: Expand Top Songs
- **GIVEN** the user is viewing the top songs section
- **WHEN** the user taps "查看更多" (See More)
- **THEN** a full-screen list of top songs is displayed (top 50)
- **AND** the list can be scrolled
- **AND** tapping a song initiates playback

#### Scenario: Song Statistics Detail
- **GIVEN** the user is viewing the full top songs list
- **WHEN** the user long-presses or taps the info icon on a song
- **THEN** detailed statistics for that song are shown:
  - Total play count
  - Total listening time
  - Skip count
  - Completion rate (%)
  - First played date
  - Last played date

---

### Requirement: Listening Trend Chart

The Statistics screen MUST display a visual trend chart.

#### Scenario: Display Trend Chart
- **GIVEN** the user is on the Statistics screen
- **WHEN** the trend chart section is visible
- **THEN** a bar chart showing daily listening for the past 14 days is displayed
- **AND** the chart uses the app's accent color for bars
- **AND** the height of each bar is proportional to listening duration

#### Scenario: Chart Interaction
- **GIVEN** the trend chart is displayed
- **WHEN** the user taps on a specific bar/day
- **THEN** a tooltip shows: date, exact duration, song count
- **AND** the tapped bar is visually highlighted

#### Scenario: No Data Days
- **GIVEN** the user did not listen on certain days
- **WHEN** those days appear in the chart
- **THEN** they are shown with zero-height bars (or baseline)
- **AND** they are still labeled on the date axis

---

### Requirement: Discovery Section

The Statistics screen MUST help users rediscover forgotten music.

#### Scenario: Show Unplayed Songs
- **GIVEN** the user has songs that haven't been played in > 30 days
- **WHEN** the Statistics screen loads
- **THEN** a "發現遺珠" (Hidden Gems) section is displayed
- **AND** 3-5 randomly selected long-unplayed songs are shown

#### Scenario: Play Unplayed Songs
- **GIVEN** the discovery section is displayed
- **WHEN** the user taps "探索這些歌曲" (Explore These Songs)
- **THEN** a playlist of unplayed songs is created and starts playing
- **OR** the user is navigated to a filtered list of unplayed songs

---

### Requirement: Inline Song Statistics

Song statistics MUST be accessible from existing song detail views.

#### Scenario: Song Info Integration
- **GIVEN** the user views a song's detail/info sheet
- **WHEN** statistics exist for that song
- **THEN** play count and last played date are shown
- **AND** tapping the stats section navigates to full statistics

#### Scenario: Album Detail Integration
- **GIVEN** the user views an album's detail page
- **WHEN** statistics exist for songs in that album
- **THEN** total album plays and listening time are summarized
- **AND** individual song play counts may be shown inline
