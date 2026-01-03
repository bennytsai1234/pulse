# Change: Rebrand from Gemini Music to Pulse

## Why
「Gemini」與多個知名品牌（如 Google Gemini AI）重疊，缺乏獨特辨識度。「Pulse」(脈動) 簡潔有力、易於記憶，且完美呼應音樂的節奏與脈動，具有更強的品牌聯想。

## What Changes
- App 顯示名稱從 "Gemini Music" 更改為 "Pulse"（中文：從 "Gemini 音樂" 更改為 "脈動"）
- 專案根名稱 (settings.gradle.kts) 更新
- 重新設計 App 圖標 - 採用「脈搏波形 + 播放按鈕」視覺元素
- 重新設計啟動動畫 - 呈現脈動效果
- 更新所有文件中的品牌名稱 (README, RELEASE_NOTES, project.md)
- 更新 UI 中顯示的品牌名稱

## Impact
- Affected specs: `branding` (新增), `ui` (修改)
- Affected code:
  - `ui/src/main/res/values/strings.xml`
  - `ui/src/main/res/values-zh-rTW/strings.xml`
  - `ui/src/main/java/com/gemini/music/ui/home/HomeScreenRedesigned.kt`
  - `ui/src/main/java/com/gemini/music/ui/main/PermissionRequiredScreen.kt`
  - `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - `app/src/main/res/drawable/ic_launcher_background.xml`
  - `app/src/main/res/drawable/avd_splash_logo.xml`
  - `app/src/main/res/drawable/splash_background.xml`
  - `settings.gradle.kts`
  - `README.md`
  - `RELEASE_NOTES.md`
  - `openspec/project.md`

## Out of Scope
- Package 名稱 (`com.gemini.music`) - 影響範圍過大，暫不更改
- GitHub Repository 名稱 - 需使用者手動操作
- 第三方服務配置 (Firebase 等)
