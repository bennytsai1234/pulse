# Design: Refresh Branding Assets

## Asset Requirements

### App Icon (Adaptive)
- **Background**: `ic_launcher_background.xml` (Vector or color). Dark premium gradient.
- **Foreground**: `ic_launcher_foreground.xml` (Vector). Clean symbol representing Music + Gemini.
- **Legacy**: `ic_launcher.png` for older APIs.

### Splash Screen (`core-splashscreen`)
We need to define a new theme `Theme.Gemini.Starting` that inherits from `Theme.SplashScreen`.

**Theme Attributes**:
- `windowSplashScreenBackground`: Matches the app's dark mode background.
- `windowSplashScreenAnimatedIcon`: An AnimatedVectorDrawable (AVD) of the logo.
- `postSplashScreenTheme`: Refer to the main app theme (`Theme.Material.NoActionBar` currently, but usually should be the app's main theme).

### Implementation Steps
1.  **Generate Concepts**: Use DALL-E/Image Gen to create a logo concept.
2.  **Vectorize**: Since I cannot directly output SVG files via image gen, I will generate a concept image, and then implement a *similar* design using Android Vector Drawable XML code (hand-coding simple shapes like circles, notes, gradients). **Correction**: I can generate pixels, but for an App Icon, Vector is best. I will try to generate a high-res PNG and use it, or construct a simple geometric Vector XML.
    *   *Decision*: I will construct a geometric Vector XML for the best quality and "Premium" feel (sharp edges, small file size). Sticking to simple shapes: detailed notes, circles, stars.
3.  **Animation**: Use `android.graphics.drawable.AnimatedVectorDrawable` to animate the vector properties (scale, alpha, rotation).

## Architecture Impact
- **Modules**:
    - `app`: `AndroidManifest.xml` updates, resource files (`mipmap`, `drawable`, `values`).
    - `ui`: No code changes expected in Compose directly, mostly Resources.
