# Tasks: Cleanup Workspace

## 準備工作
- [x] 執行 `git status` 確認當前工作區狀態。
- [x] 執行 `openspec list --specs` 確保基礎規格已同步。

## 檔案清理
- [x] 刪除根目錄的 `build_log.txt`。
- [x] 檢查 `.kotlin` 目錄內容，若為快取則刪除或加入 `.gitignore`。
- [x] 比對 `AGENTS.md` 與 `openspec/AGENTS.md`，合併後刪除根目錄的舊版。
- [x] 檢查並移除專案中的 `.bak` 或 `~` 結尾的殘留檔案。

## 代碼與資源清理
- [x] 使用 `grep` 搜尋並列出標記為 `TODO` 或 `UNUSUED` 的區域。
- [x] 檢查 `ui` 模組中是否有已刪除功能留下的資源檔案。
- [x] 刪除已廢棄的測試類別或舊版實作備份。

## 規格更新
- [x] 更新 `openspec/specs/stability/spec.md` 以包含關於專案整潔度的非功能性需求（如有必要）。

## 驗證與存檔
- [x] 執行 `./gradlew clean build` 確保清理後專案仍能正確編譯。
- [x] 執行 `openspec validate cleanup-workspace --strict`。
- [x] 存檔變更並推送至 GitHub。
