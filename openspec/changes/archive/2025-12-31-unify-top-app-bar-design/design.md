# Design: Unified Top App Bar

## Architectural Overview
We will leverage the **Material 3 Design System** to strictly enforce layout consistency. Instead of ad-hoc headers, all screens will consume a shared `GeminiTopBar`.

## Component: `GeminiTopBar`
Located in `core/designsystem/component/GeminiComponents.kt`.

### API
```kotlin
@Composable
fun GeminiTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
)
```

### Implementation Details
- **Underlying Component**: `CenterAlignedTopAppBar` (default) or `TopAppBar`. Given `SettingsScreen` uses `CenterAligned`, we should standardized on that for a premium look, or expose a parameter if alignment needs to vary (though strict consistency suggests picking one).
- **Height**: Standard M3 height (`64.dp`).
- **Colors**: Use `MaterialTheme.colorScheme.surface` or `surfaceColorAtElevation`.

## Migration Strategy
1.  **Home Screen**:
    - Current: `Surface(height=48.dp) { Row { ... } }`
    - New: `GeminiTopBar(title = greeting, navigationIcon = { MenuIcon }, actions = { Search, etc. })`
    - *Note*: The "Greeting" might need to move into the content body if the TopBar title is meant for "App Name" or context. However, for Home, the TopBar often contains the app logic.
    - *Decision*: We will place an empty title or "Gemini Music" in the TopBar for Home, or use the Greeting if appropriate. The user's primary concern is **button alignment**. The AppBar height must match.

2.  **Selection Mode**:
    - The `GeminiTopBar` should support a "Contextual" mode (changing colors/actions), or we swap standard `GeminiTopBar` for a `GeminiContextBar` that shares the exact same layout structure but different styling. For simplicity, `GeminiTopBar` will accept colors customization or distinct parameters.

## Trade-offs
- **Custom vs Standard**: We are trading the custom condensed 48dp header for a taller 64dp standard header. This reduces vertical space for content slightly but gains massive consistency and touch-target improvements.
