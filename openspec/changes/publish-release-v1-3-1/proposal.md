# Proposal: Publish Release v1.3.1

## Goal
Prepare and publish version 1.3.1 of the application, including a version bump and validation of the build process.

## Problem
The user has requested a release update to version 1.3.1, incorporating recent UI and performance improvements (Unified Top Bar, Consistent Player Animations). A formal release process ensures version tracking and build integrity.

## Solution
1.  **Bump Version**: Update `app/build.gradle.kts` to increment `versionCode` (to `9`) and set `versionName` to `1.3.1`.
2.  **Verify Build**: Run the Gradle build task to ensure no regressions were introduced by recent changes.
3.  **Release Artifact**: (Implicit) The build process will generate the APKs.

## Risks
- Build failure due to recent changes (low risk, as previous steps passed).
- Key signing issues (mitigated by using debug keystore for this proposal context).

## Verification
- `gradlew assembleRelease` completes successfully.
- `app/build.gradle.kts` reflects version 1.3.1.
