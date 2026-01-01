# ui-polish Specification

## Purpose
TBD - created by archiving change polish-ui-and-ux. Update Purpose after archive.
## Requirements
### Requirement: Premium Empty States
All list screens (Home, Playlist, etc.) MUST display a visually appealing "Empty State" when no data is available. This includes a prominent icon (or illustration) and a clear text message.

#### Scenario: User has no playlists
- **Given** the user navigates to the "Playlists" tab/screen.
- **And** no playlists exist.
- **Then** the screen displays a large "Playlist" icon (tinted/styled).
- **And** a text message "No Playlists yet".
- **And** (Optional) a button "Create Playlist".

### Requirement: Optimized Lyrics Scrolling
The Karaoke Lyrics view MUST keep the currently playing line vertically centered on the screen and animate smoothly between lines.

#### Scenario: Song plays
- **Given** the "Lyrics" view is open.
- **When** the song progresses to the next line.
- **Then** the list scrolls smoothly to bring the new line to the center.
- **And** the current line scales up/highlights.

### Requirement: Branded Launch Experience
> The application MUST provide a seamless, branded entry experience from the device launcher to the main interface.

#### Scenario: App Icon
- **Given** the device launcher.
- **When** the user views the app icon.
- **Then** it MUST display a unique, high-quality Adaptive Icon representing the "Gemini Music" brand (not the default Android robot).
- **And** it MUST support dynamic theming or standard adaptive layers (background + foreground).

#### Scenario: Animated Splash Screen
- **Given** the application is launching from a cold start.
- **When** the splash screen is displayed.
- **Then** it MUST show the branded logo.
- **And** the logo SHOULD animate (e.g., reveal, pulse, or scale) to indicate loading progress (Android 12+).
- **And** the splash screen background color MUST match the application's day/night theme context.

