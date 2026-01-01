<!-- OPENSPEC:START -->
- [x] 1. **Design & Generate Icon**: Generate a "Gemini Music" logo concept using image generation, then manually recreate it as a clean Vector Drawable (`ic_logo_vector.xml`) for infinite scaling.
- [x] 2. **Implement App Icon**: Create `mipmap` directories and `ic_launcher.xml` (adaptive-icon) resources using the vector foreground and a compatible background. (Directly implemented in drawable resources).
- [x] 3. **Create Splash Theme**: Add `Theme.Gemini.Splash` in `values/themes.xml` (and `values-night`) configuring `core-splashscreen` attributes.
- [x] 4. **Create Loading Animation**: Create an Animated Vector Drawable (`avd_splash_logo.xml`) that animates the logo (e.g., scale up, pulse) for the splash screen.
- [x] 5. **Update Manifest**: Point `android:theme` in `AndroidManifest.xml` to `Theme.Gemini.Starting` and ensure `postSplashScreenTheme` transitions correctly to the main app theme.
- [x] 6. **Verify**: Build and run to verify the new icon appears on the launcher and the splash screen animates on startup.
<!-- OPENSPEC:END -->
