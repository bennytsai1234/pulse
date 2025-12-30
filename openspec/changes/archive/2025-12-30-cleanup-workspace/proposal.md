# Proposal: Cleanup Workspace and Residual Files

## Why
隨著專案的快速迭代，根目錄中累積了一些臨時文件、建置日誌以及可能冗餘的資源。為了保持開發環境的整潔，並確保專案結構符合 OpenSpec 的最佳實踐，需要進行一次全面的清理與優化。這有助於提高建置速度，減少 Git 倉庫的雜訊，並使新加入的開發者（或 AI 代理）更容易理解專案結構。

## What Changes

### 1. 檔案清理 (File Cleanup)
- 刪除根目錄中的臨時建置日誌 (`build_log.txt`)。
- 檢查並移除隱藏的 `.kotlin` 目錄（如果是臨時快取）。
- 確認 `AGENTS.md` 是否應整合至 `openspec/AGENTS.md` 並移除重複項。
- 移除不再使用的指令碼或臨時備份文件。

### 2. 代碼與資源優化 (Code & Resource Optimization)
- 掃描並移除未使用的 Kotlin 類別、函數或屬性。
- 移除各模組 `res/` 目錄中未使用的圖片、字串或佈局資源。
- 清理無用的 `TODO` 標籤或過時的註解。

### 3. 建置優化 (Build Optimization)
- 執行 `./gradlew clean` 以清除殘留的建置產物。
- 檢查 `build.gradle.kts` 中是否有冗餘的依賴項。

## Impact
- **整潔度**: 專案根目錄將只包含必要的配置檔案，結構更清晰。
- **維護性**: 減少冗餘資源可以降低誤用舊代碼或資源的風險。
- **效能**: 減少專案體積，並可能略微提升掃描與索引的速度。
