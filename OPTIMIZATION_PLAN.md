# Refactoring & Optimization Plan: "Pulse Ultimate"

## 1. Architectural Improvements
- **Deduplication**: Move `Song.toMediaItem` / `MediaItem.toSong` mappers to `core/common` or a dedicated `mapper` module.
- **Error Handling**: Replace direct `Toast` in Service with `SharedFlow<PlayerError>` in `MusicController` for UI-driven error handling.
- **Configurability**: Move hardcoded thresholds (e.g., Crossfade 30MB check) to `PlayerConfig` or `Domain` constants.

## 2. Player Engine Enhancements
- **Gapless & Crossfade Harmony**: Ensure `DualPlayerManager` gracefully handles gapless metadata when crossfade is off.
- **Audio Quality**:
    - Implement **ReplayGain** / Loudness Normalization.
    - Add **Pitch/Speed** control with high-quality time stretching (`SonicAudioProcessor`).
- **Resilience**:
    - Better network error recovery for streaming (if applicable in future).
    - Robust handling of corrupt local files (skip and blacklist).

## 3. UI/UX Polish (Pending Agent Report)
- **Visuals**: Dynamic theming engine (Monet) refinement.
- **Animations**: Shared element transitions between List and Player.
- **Performance**: Optimize `LazyColumn` item keys and image loading (Coil).

## 4. Feature Additions (The "Best" Factors)
- **Smart Playlists**: "Most Played", "Never Played", "Recently Added" (Dynamic SQL generation).
- **Lyrics**: Synced lyrics support (LRC parser).
- **Tag Editor**: Built-in ID3 tag editor.
