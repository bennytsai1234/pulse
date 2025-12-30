# Design: Cleanup and Workspace Optimization

## Overview
隨著專案開發規模變大，保持工作區與程式碼庫的整潔對於長期維護至關重要。本設計文件概述了清理殘留檔案、優化資源以及整合規範文件的策略。

## Cleanup Strategy

### 1. 檔案與目錄清理
- **build_log.txt**: 這是執行建置時產生的臨時日誌。開發者應參考 IDE 輸出或控制台日誌，不應在倉庫中保留此檔案。
- **.kotlin/**: kotlin 建置快取目錄。應將其加入 `.gitignore` 或直接刪除。
- **AGENTS.md**: 目前根目錄與 `openspec/` 目錄中各有一個 `AGENTS.md`。為了符合 OpenSpec 的規範，我們將所有行為準則統一收納在 `openspec/AGENTS.md`。

### 2. 資源稽核 (Resource Audit)
- 透過 Android Studio 的 "Remove Unused Resources" 邏輯，辨別在 `ui` 模組中未被參照的 Drawable 與 String。
- 檢查 `LocalAudioSource.kt` 等資料層組件，確保沒有無效的調試日誌 (Log.d) 或過時的 TODO。

### 3. 建置環境驗證
- 執行 `./gradlew clean` 並重新建置，確保專案結構清理後仍能 100% 通過編譯。

## Project Health Report (專案健康報告)
- **架構**: 符合 Clean Architecture (`UI -> Domain <- Data`)。
- **UI**: 基於 Jetpack Compose，已實作 Premium 級別的空狀態與歌詞同步。
- **規格**: 已存檔 5 個主要能力，覆蓋了播放器核心、UI 優化、等化器與穩定性。
- **目前問題**: 根目錄混亂，存在少量的建置殘留物，需進行本次 Cleanup 以達成「生產級別」的整潔度。
