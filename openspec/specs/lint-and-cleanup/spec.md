# lint-and-cleanup Specification

## Purpose
TBD - created by archiving change cleanup-workspace. Update Purpose after archive.
## Requirements
### Requirement: Workspace Hygiene
> The repository workspace MUST remain clean and free of build artifacts, logs, or temporary files.

#### Scenario: Cleanup Residual Files
- **Given** building the project or running agents has created `build_log.txt` or `.kotlin/` artifacts in the root.
- **When** the cleanup task is executed.
- **Then** these non-essential files MUST be deleted or moved to a git-ignored location.

#### Scenario: Resource Audit
- **Given** the project contains unused resources (drawables, layouts) or orphaned code.
- **When** the audit and cleanup process is performed.
- **Then** the unused files SHOULD be removed to reduce the application footprint.

### Requirement: Centralized Configuration
> All project-wide guidelines and agent instructions MUST be centralized in the `openspec/` directory.

#### Scenario: Consolidate Documentation
- **Given** `AGENTS.md` exists in the root directory.
- **When** the documentation audit is performed.
- **Then** any unique guidelines MUST be moved to `openspec/AGENTS.md`.
- **And** the root `AGENTS.md` MUST be deleted to avoid duplication.

