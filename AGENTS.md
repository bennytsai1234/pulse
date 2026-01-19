# PROJECT KNOWLEDGE BASE (Pulse)

**Context:** Android Local Music Player
**Stack:** Kotlin, Jetpack Compose, Media3, Hilt, Room
**Architecture:** MVVM + Clean Architecture

## CRITICAL: WORKFLOW & STANDARDS
**MUST READ:** [`openspec/AGENTS.md`](./openspec/AGENTS.md)
*This file is the single source of truth for architectural rules, code standards, and AI behavior guidelines.*

## STRUCTURE & NAVIGATION
| Module | Purpose | Key Tech |
|--------|---------|----------|
| `app/` | Entry point, DI graph, NavHost | Hilt, Navigation |
| `domain/` | **PURE** Business Logic, UseCases | Kotlin (No Android) |
| `data/` | Repositories, DB, Preferences | Room, DataStore |
| `player/` | Media playback implementation | Media3 (ExoPlayer) |
| `ui/` | UI Components, Screens, Theme | Compose, Material3 |
| `core/` | Common utils, extensions | Logger, Coroutines |
| `openspec/` | **AI Specs & Architecture docs** | Markdown, Mermaid |

## DEVELOPMENT RULES
1.  **Strict Layering:** `ui` -> `domain` <- `data`. `domain` knows NOTHING of Android.
2.  **State Management:** Unidirectional Data Flow (UDF) with `ViewModel` & `StateFlow`.
3.  **UI:** 100% Compose. No XML layouts.
4.  **Specs First:** Consult `openspec/` before major refactors.

## COMMANDS
```bash
# Build & Install
./gradlew assembleDebug      # Build APK
./gradlew installDebug       # Install to device

# Verification
./gradlew test               # Unit tests
./gradlew lintDebug          # Lint check
```

## QUICK START
1.  Read [`openspec/AGENTS.md`](./openspec/AGENTS.md).
2.  Sync Gradle.
3.  Run `assembleDebug` to verify environment.
