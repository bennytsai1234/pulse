# Change: Optimize Player Architecture

## Why
To achieve "world-class" status, the player architecture needs refinement to reduce duplication, improve error handling, and support advanced audio features natively.

## What Changes
1.  **Code Deduplication**: Shared mappers for MediaItem <-> Song conversion.
2.  **Robust Error Handling**: Centralized error reporting via `MusicController` instead of Service-level Toasts.
3.  **Gapless & Crossfade**: Enhanced `DualPlayerManager` with better state management.
4.  **Audio Quality**: Preparation for ReplayGain and DSP integration.

## Impact
- **Modules**: `player`, `domain`, `core`
- **Risk**: Low (Refactoring internal implementation details)
