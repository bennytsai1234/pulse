# Tasks: rename-package-to-pulse

## 任務總覽
使用腳本將 package 從 `com.gemini.music` 重命名為 `com.pulse.music`。

---

## Phase 0: 準備工作

### Task 0.1: 備份與準備
- [x] 確保所有變更已 commit 到 Git
- [x] 執行 `./gradlew clean` 清理構建緩存

---

## Phase 1: 執行 Package 重命名

### Task 1.1: 執行重命名腳本
- [x] 建立 `rename-package.ps1` 腳本
- [x] 執行腳本批量更新所有檔案內容
- [x] 重命名所有目錄結構 (com/gemini -> com/pulse)
- [x] 清理空目錄

---

## Phase 2: 更新 build.gradle.kts

### Task 2.1: 確認更新
- [x] `namespace = "com.pulse.music"` (已由腳本自動更新)
- [x] `applicationId = "com.pulse.music"` (已由腳本自動更新)
- [x] 更新 `versionCode = 14`
- [x] 更新 `versionName = "2.0.0"`

---

## Phase 3: 驗證構建

### Task 3.1: 構建驗證
- [x] 執行 `./gradlew clean assembleDebug`
- [x] 確認無編譯錯誤 (Exit code: 0)

### Task 3.2: 搜尋殘留引用
- [x] 搜尋 `com.gemini.music`，確認無殘留
- [x] 所有檔案已更新為 `com.pulse.music`

---

## Phase 4: 清理

### Task 4.1: 清理臨時檔案
- [x] 刪除 `rename-package.ps1` 腳本

### Task 4.2: 提交變更
- [x] 提交所有變更到 Git

---

## 完成摘要

| Phase | 狀態 | 備註 |
|-------|------|------|
| Phase 0: 準備 | ✅ 完成 | Git checkpoint 已建立 |
| Phase 1: 重命名 | ✅ 完成 | 腳本自動處理 |
| Phase 2: build.gradle | ✅ 完成 | 腳本 + 手動更新版本號 |
| Phase 3: 驗證 | ✅ 完成 | 構建成功 |
| Phase 4: 清理 | ✅ 完成 | 腳本已刪除 |

**所有任務已完成！** Package 已成功從 `com.gemini.music` 重命名為 `com.pulse.music`。

---

## 變更統計

- **更新的 Kotlin 檔案**: ~150+ 個
- **更新的 XML 檔案**: 多個
- **重命名的目錄**: 7 個模組
- **新版本**: v2.0.0 (Build 14)
