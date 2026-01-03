# Tasks: rebrand-to-pulse

## 任務總覽
將 **Gemini Music** 品牌重塑為 **Pulse**，包含名稱更換、圖標設計和動畫設計。

---

## Phase 1: 圖標設計與實現

### Task 1.1: 設計並實現新的 App 圖標前景
- [x] 設計 `ic_launcher_foreground.xml` - 脈搏波形 + 播放按鈕
- [x] 確保安全區域內的視覺元素完整
- [x] 套用青藍漸層色彩系統

**檔案**：`app/src/main/res/drawable/ic_launcher_foreground.xml`

### Task 1.2: 設計並實現新的 App 圖標背景
- [x] 設計 `ic_launcher_background.xml` - 深色漸層 + 波紋紋理
- [x] 確保與前景圖視覺協調

**檔案**：`app/src/main/res/drawable/ic_launcher_background.xml`

---

## Phase 2: 啟動動畫設計與實現

### Task 2.1: 設計並實現新的啟動動畫
- [x] 設計 `avd_splash_logo.xml` - 脈動動畫效果
- [x] 實現波形繪製動畫 (Path Animation)
- [x] 實現縮放脈動效果 (Scale Animation)
- [x] 實現光暈擴散效果 (Glow Animation)

**檔案**：`app/src/main/res/drawable/avd_splash_logo.xml`

### Task 2.2: 更新啟動背景
- [x] 更新 `splash_background.xml` - 配合新品牌的視覺風格
- [x] 移除舊有的 Gemini 相關元素

**檔案**：`app/src/main/res/drawable/splash_background.xml`

---

## Phase 3: 名稱替換

### Task 3.1: 更新 App 顯示名稱 (strings.xml)
- [x] 更新 `values/strings.xml` 中的 `app_name` 為 "Pulse"
- [x] 更新 `values-zh-rTW/strings.xml` 中的 `app_name` 為 "脈動"

**檔案**：
- `ui/src/main/res/values/strings.xml`
- `ui/src/main/res/values-zh-rTW/strings.xml`

### Task 3.2: 更新 UI 中的品牌名稱
- [x] 更新 `HomeScreenRedesigned.kt` 中的 "Gemini Music" 為 "Pulse"
- [x] 更新 `PermissionRequiredScreen.kt` 中的品牌說明文字

**檔案**：
- `ui/src/main/java/com/gemini/music/ui/home/HomeScreenRedesigned.kt`
- `ui/src/main/java/com/gemini/music/ui/main/PermissionRequiredScreen.kt`

### Task 3.3: 更新專案設定
- [x] 更新 `settings.gradle.kts` 中的 `rootProject.name`

**檔案**：`settings.gradle.kts`

### Task 3.4: 額外發現的遺漏項目
- [x] 更新 `app/src/main/res/values/strings.xml` 中的 `app_name`
- [x] 更新 `player/src/main/res/values/strings.xml` 中的 `browse_root_title`
- [x] 更新 `data/.../GoogleDriveService.kt` 中的應用程式名稱
- [x] 更新 `openspec/specs/ui-polish/spec.md` 中的品牌引用

---

## Phase 4: 文件更新

### Task 4.1: 更新 OpenSpec 專案文檔
- [x] 更新 `openspec/project.md` 中的專案名稱和描述

**檔案**：`openspec/project.md`

### Task 4.2: 更新 README.md
- [x] 更新標題和所有 "Gemini Music" 引用
- [x] 更新 GitHub repository 連結（如適用）
- [x] 更新致謝區塊

**檔案**：`README.md`

### Task 4.3: 更新發布說明
- [x] 在 `RELEASE_NOTES.md` 頂部新增品牌重塑說明
- [x] 保留舊版本歷史並標註 (formerly Gemini Music)

**檔案**：`RELEASE_NOTES.md`

---

## Phase 5: 驗證與清理

### Task 5.1: 構建驗證
- [x] 執行 `./gradlew clean`
- [x] 執行 `./gradlew assembleDebug`
- [x] 確認無編譯錯誤

### Task 5.2: 搜尋殘留引用
- [x] 使用 `grep` 搜尋任何殘留的 "Gemini Music" 字串
- [x] 修復發現的遺漏項目

### Task 5.3: 最終構建確認
- [x] 重新執行 `./gradlew clean assembleDebug`
- [x] 確認構建成功

---

## 完成摘要

| Phase | 狀態 | 完成時間 |
|-------|------|---------|
| Phase 1: 圖標設計 | ✅ 完成 | 2026-01-03 |
| Phase 2: 啟動動畫 | ✅ 完成 | 2026-01-03 |
| Phase 3: 名稱替換 | ✅ 完成 | 2026-01-03 |
| Phase 4: 文件更新 | ✅ 完成 | 2026-01-03 |
| Phase 5: 驗證清理 | ✅ 完成 | 2026-01-03 |

**所有任務已完成！** 品牌已成功從 Gemini Music 重塑為 Pulse。
