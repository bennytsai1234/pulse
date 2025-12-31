# Spec: Release Management

## ADDED Requirements

### Requirement: Versioning
The application MUST adhere to semantic versioning.

#### Scenario: v1.3.1 Release
Given the application source code includes recent UI consistency fixes
When the release build is generated
Then the version name MUST be "1.3.1"
And the version code MUST be greater than the previous version (8).
