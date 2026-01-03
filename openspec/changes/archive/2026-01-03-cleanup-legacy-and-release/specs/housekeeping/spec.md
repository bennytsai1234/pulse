# Spec Delta: 專案維護 (Housekeeping)

## ADDED Requirements

### Requirement: Clean Root Directory (根目錄整潔)
The project root directory **MUST** be kept clean of compiled artifacts and temporary files. (專案根目錄必須保持整潔，不得包含編譯後的構建產物 (APK) 或零碎的發布說明檔案。)

#### Scenario: Verify Root Directory
- **WHEN** 檢查專案根目錄時
- **THEN** 不應存在 `*.apk` 檔案 (它們應位於 `app/build/outputs/apk`)
- **AND** 只有主要的 `RELEASE_NOTES.md` 可以存在，不應有其他 `RELEASE_NOTES_*.md` 碎片檔案。
