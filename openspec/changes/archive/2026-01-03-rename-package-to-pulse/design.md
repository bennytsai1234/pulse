# Design: rename-package-to-pulse

## 設計概述
本文件詳述使用 Android Studio Refactor 功能將 package 從 `com.gemini.music` 重命名為 `com.pulse.music` 的技術設計與執行策略。

---

## 1. 重命名策略

### 1.1 為什麼使用 Android Studio Refactor？
- **安全性**：IDE 會自動追蹤所有引用並批量更新
- **完整性**：確保 import、package 宣告、AndroidManifest 同步更新
- **效率**：比手動修改 150+ 個檔案更快且不易出錯
- **回溯性**：可透過 Git 輕鬆回溯

### 1.2 重構順序
由於模組間存在依賴關係，建議按以下順序執行：

```
1. :domain (無依賴，純 Kotlin)
2. :core:common (被多個模組依賴)
3. :core:designsystem (依賴 common)
4. :data (依賴 domain, common)
5. :player (依賴 domain, data)
6. :ui (依賴 domain, player, designsystem)
7. :app (依賴所有模組)
```

---

## 2. 變更對照表

| 項目 | 舊值 | 新值 |
|------|------|------|
| **Application ID** | `com.gemini.music` | `com.pulse.music` |
| **Base Package** | `com.gemini.music` | `com.pulse.music` |
| **App Namespace** | `com.gemini.music` | `com.pulse.music` |
| **UI Namespace** | `com.gemini.music.ui` | `com.pulse.music.ui` |
| **Data Namespace** | `com.gemini.music.data` | `com.pulse.music.data` |
| **Domain Namespace** | `com.gemini.music.domain` | `com.pulse.music.domain` |
| **Player Namespace** | `com.gemini.music.player` | `com.pulse.music.player` |
| **Core Common** | `com.gemini.music.core.common` | `com.pulse.music.core.common` |
| **Core Designsystem** | `com.gemini.music.core.designsystem` | `com.pulse.music.core.designsystem` |

---

## 3. 目錄結構變更

### 3.1 舊結構
```
app/src/main/java/com/gemini/music/
ui/src/main/java/com/gemini/music/ui/
data/src/main/java/com/gemini/music/data/
domain/src/main/java/com/gemini/music/domain/
player/src/main/java/com/gemini/music/player/
core/common/src/main/java/com/gemini/music/core/common/
core/designsystem/src/main/java/com/gemini/music/core/designsystem/
```

### 3.2 新結構
```
app/src/main/java/com/pulse/music/
ui/src/main/java/com/pulse/music/ui/
data/src/main/java/com/pulse/music/data/
domain/src/main/java/com/pulse/music/domain/
player/src/main/java/com/pulse/music/player/
core/common/src/main/java/com/pulse/music/core/common/
core/designsystem/src/main/java/com/pulse/music/core/designsystem/
```

---

## 4. 關鍵檔案變更

### 4.1 build.gradle.kts
每個模組的 `build.gradle.kts` 需要更新 `namespace`：

```kotlin
// 舊
android {
    namespace = "com.gemini.music.xxx"
}

// 新
android {
    namespace = "com.pulse.music.xxx"
}
```

### 4.2 app/build.gradle.kts
除了 namespace，還需更新 `applicationId`：

```kotlin
// 舊
defaultConfig {
    applicationId = "com.gemini.music"
}

// 新
defaultConfig {
    applicationId = "com.pulse.music"
}
```

### 4.3 AndroidManifest.xml
需要檢查並更新：
- `package` 屬性（如有）
- `android:name` 屬性中的完整類別路徑
- Intent filter 中的 action 名稱（如有自訂）

---

## 5. 風險與緩解措施

| 風險 | 影響 | 緩解措施 |
|------|------|---------|
| 遺漏引用導致編譯失敗 | 中 | 執行完整 gradle build 驗證 |
| Hilt 注入失敗 | 高 | 確保 @AndroidEntryPoint 類別正確更新 |
| Media Service 註冊失敗 | 高 | 檢查 Manifest 中的 service 宣告 |
| ProGuard 規則失效 | 中 | 檢查並更新 proguard-rules.pro |
| 資源 ID 衝突 | 低 | R 類別引用會自動更新 |

---

## 6. 驗證清單

- [ ] 所有模組無編譯錯誤
- [ ] App 可正常安裝
- [ ] App 可正常啟動
- [ ] 音樂播放功能正常
- [ ] 通知控制正常
- [ ] Widget 正常運作
- [ ] 所有 UI 頁面可正常導航

---

## 7. 決策記錄

| 決策點 | 選項 | 決定 | 原因 |
|--------|------|------|------|
| 重構工具 | 手動/Android Studio/腳本 | Android Studio | 最安全、最完整 |
| Package 命名 | com.pulse.music / pulse.music / io.pulse.music | com.pulse.music | 遵循 Android 標準命名慣例 |
| 執行時機 | 漸進式/一次性 | 一次性 | 避免中間狀態的編譯問題 |
