## ADDED Requirements

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
