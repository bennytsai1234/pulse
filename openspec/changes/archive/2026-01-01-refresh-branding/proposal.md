# Proposal: Refresh App Icon and Loading Animation

## Summary
Update the application's launcher icon and the splash screen loading animation to reflect a modern, premium brand identity suitable for "Gemini Music Player".

## Motivation
The current application uses a default launcher icon and a standard, unconfigured splash screen. To align with the "Premium Design" goal stated in the project guidelines, the app requires a custom, distinctive visual identity upon entry.

## Proposed Changes
1.  **New App Icon**: Generate and implement a new Adaptive Icon (Foreground + Background) featuring a "Gemini" inspired music aesthetic (e.g., dual musical notes, cosmic/starry themes, or a stylized 'G').
2.  **Custom Splash Screen**: Configure `androidx.core:core-splashscreen` with a custom theme.
3.  **Loading Animation**: Create an Animated Vector Drawable (AVD) for the splash screen icon to provide a smooth, dynamic entry experience.

## Design Concept
- **Theme**: "Cosmic Harmony" - integrating Gemini (The Twins) touches with Music.
- **Colors**: Deep indigo/black background (Space) with vibrant gradients (Neon Blue/Purple) for the foreground.
- **Animation**: The logo should pulse or draw itself (`trimPath`) upon launch.

## Risks
- **Asset Generation**: AI-generated assets might need manual tuning to fit perfect Adaptive Icon safe zones.
- **Device Compatibility**: Animated icons are supported on Android 12+. Older versions will degrade to a static splash icon.
