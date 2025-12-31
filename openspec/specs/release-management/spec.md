# release-management Specification

## Purpose
TBD - created by archiving change publish-release-v1-2. Update Purpose after archive.
## Requirements
### Requirement: Version Tagging
> Every major milestone MUST be market with a git tag and incremented version codes.

#### Scenario: Successful release tagging
- **Given** The `cleanup-workspace` and UI polish tasks are merged and verified.
- **When** Building the production APK.
- **Then** The `versionCode` MUST be incremented.
- **And** A git tag following the version name (e.g., `v1.3.0`) MUST be created.

### Requirement: Release Documentation
> All public releases MUST be accompanied by human-readable release notes.

#### Scenario: Update release notes
- **Given** New features like "Swipe to minimize" and "Gemini Empty State" are implemented.
- **When** Preparing for release v1.3.0.
- **Then** `RELEASE_NOTES.md` MUST reflect these changes in a structured format.

