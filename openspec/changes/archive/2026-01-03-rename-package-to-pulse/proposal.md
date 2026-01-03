# Change: Rename Package from com.gemini.music to com.pulse.music

## Why
完成品牌重塑的最後一步，將 Package 名稱從 `com.gemini.music` 統一更名為 `com.pulse.music`，實現全面的品牌一致性。這個變更是 `rebrand-to-pulse` 的延伸，確保程式碼層面也完全反映新的 Pulse 品牌identity。

## What
- 將所有模組的 package 名稱從 `com.gemini.music` 更改為 `com.pulse.music`
- 更新所有 `build.gradle.kts` 中的 `namespace` 和 `applicationId`
- 重新組織目錄結構以反映新的 package 命名
- 更新所有 `AndroidManifest.xml` 中的相關引用
- 更新所有 import 語句和 package 宣告

## Scope
**影響模組：**
- `:app` - applicationId, namespace
- `:ui` - namespace, 所有 UI 類別
- `:data` - namespace, 資料層類別
- `:domain` - namespace, 領域模型
- `:player` - namespace, 播放器服務
- `:core:common` - namespace, 共用工具
- `:core:designsystem` - namespace, 設計系統元件

**變更統計預估：**
- 約 150+ 個 Kotlin 檔案的 package 宣告
- 約 150+ 個 Kotlin 檔案的 import 語句
- 8 個 build.gradle.kts 檔案
- 多個 AndroidManifest.xml 檔案
- 7 個模組的目錄結構

## Out of Scope
- Google Play 上架相關配置（後續處理）
- Firebase 等第三方服務的重新配置（需額外工作）
- 用戶資料遷移（Application ID 變更會被視為新 App）

## Impact
- **Breaking Change**: 是，Application ID 變更會導致現有安裝無法直接更新
- **Affected Specs**: `branding` (間接)
- **Affected Code**: 專案中所有 Kotlin 檔案

## Dependencies
- 依賴 `rebrand-to-pulse` 已完成

## Implementation Strategy
使用 **Android Studio Refactor > Rename** 功能進行批量重構，這是最安全且最有效率的方式。
