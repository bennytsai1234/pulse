# Project Context - Pulse Music Player

> **最後更新**: 2026-01-04
> **版本**: v3.0 - 採用 2025/2026 年最新最佳實踐

---

## 第一章：專案概述 (Project Overview)

### 1.1 專案願景
**Pulse** 是一款現代化、極致體驗的 Android 本地音樂播放器。我們致力於提供：
- 🎨 **Premium 視覺體驗** - Material Design 3 動態主題與流暢動畫
- ⚡ **卓越效能** - 優化的播放引擎與資源管理
- 🏗️ **可維護架構** - Clean Architecture + 模組化設計
- 🎵 **完整音樂體驗** - 專業級功能與系統整合

### 1.2 品牌識別 (Brand Identity)
| 屬性 | 值 |
|------|-----|
| **英文名稱** | Pulse |
| **中文名稱** | 脈動 |
| **品牌理念** | 音樂是生命的脈動 - 節奏、心跳、能量 |
| **主色調** | Cyan (#00F2FF) → Blue (#0066FF) 漸層 |
| **強調色** | Magenta (#FF0080) |

---

## 第二章：技術棧 (Tech Stack)

### 2.1 核心技術 (2025/2026 最新)

| 類別 | 技術 | 版本 | 說明 |
|------|------|------|------|
| **語言** | Kotlin | 2.0.21+ | 官方推薦，100% Kotlin |
| **UI 框架** | Jetpack Compose | Material 3 | 宣告式 UI，業界標準 (60%+ 頂尖 App 採用) |
| **最低 SDK** | Android 8.0 | API 26 | 平衡相容性與現代功能 |
| **目標 SDK** | Android 15 | API 36 | 最新平台功能 |
| **JVM 目標** | Java 17 | - | 長期支援版本 |

### 2.2 架構與框架

| 類別 | 技術 | 用途 |
|------|------|------|
| **架構模式** | MVVM + Clean Architecture | 關注點分離、可測試性 |
| **依賴注入** | Hilt (Dagger 2) | 編譯時 DI，官方推薦 |
| **資料庫** | Room | 類型安全的 SQLite ORM |
| **媒體播放** | Media3 (ExoPlayer + MediaSession) | 官方媒體框架，支援 Android Auto |
| **非同步處理** | Kotlin Coroutines + Flow | 結構化並發與響應式串流 |
| **網路** | Retrofit + OkHttp | REST API 與 HTTP |
| **圖片載入** | Coil | Compose 優先，輕量高效 |
| **序列化** | Kotlinx Serialization | 類型安全 JSON 處理 |
| **建構工具** | Gradle Kotlin DSL | 類型安全建構腳本 |

### 2.3 依賴管理最佳實踐

**Version Catalog (`libs.versions.toml`)** 是 2025+ 的標準做法：

```toml
# ✅ 正確做法：使用版本引用
[versions]
kotlin = "2.0.21"
compose-bom = "2024.12.01"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }

# ✅ 使用 bundles 組合常用依賴
[bundles]
compose-ui = ["androidx-compose-ui", "androidx-compose-ui-graphics", "androidx-compose-material3"]
```

**規範要求**：
- ✅ 所有版本必須定義在 `[versions]` 區塊
- ✅ 使用 kebab-case 命名 (例如 `androidx-core-ktx`)
- ✅ 使用 `bundles` 組合相關依賴
- ❌ 禁止在單一模組的 `build.gradle.kts` 中硬編碼版本

---

## 第三章：架構規範 (Architecture Guidelines)

### 3.1 Clean Architecture 分層

```
┌─────────────────────────────────────────────────────────────┐
│                        App Module                           │
│  (DI Setup, Application, MainActivity)                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│   │     UI       │    │   Player     │    │     Data     │  │
│   │  (Compose,   │    │  (Media3,    │    │  (Room, API, │  │
│   │  ViewModels) │    │  Service)    │    │  Repository) │  │
│   └──────┬───────┘    └──────┬───────┘    └──────┬───────┘  │
│          │                   │                   │          │
│          ▼                   ▼                   ▼          │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                    Domain Layer                      │   │
│   │         (Pure Kotlin - UseCases, Entities)          │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                              │
│   ┌─────────────────────────────────────────────────────┐   │
│   │                    Core Modules                      │   │
│   │         (DesignSystem, Common, Extensions)          │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 各層職責

| 層級 | 職責 | 規範 |
|------|------|------|
| **Domain** | 業務邏輯、UseCases、Entities | ✅ 純 Kotlin，無 Android 依賴 |
| **Data** | Repository 實作、資料來源 | ✅ 隱藏實作細節，僅暴露介面 |
| **Player** | 媒體播放、MediaSession | ✅ 與 UI 分離，透過 Domain 通訊 |
| **UI** | Compose UI、ViewModels | ✅ MVVM + UDF (單向資料流) |
| **Core** | 共用元件、工具函數 | ✅ 無業務邏輯，純工具性質 |

### 3.3 依賴規則

```kotlin
// ✅ 正確：UI 依賴 Domain，Data 依賴 Domain
UI → Domain ← Data

// ❌ 錯誤：Domain 不可依賴任何其他層
Domain → UI    // 禁止！
Domain → Data  // 禁止！
```

---

## 第四章：Jetpack Compose 最佳實踐

### 4.1 效能優化 (2025 標準)

```kotlin
// ✅ 使用 @Immutable 標註不可變狀態類別
@Immutable
data class SongUiState(
    val title: String,
    val artist: String,
    val isPlaying: Boolean
)

// ✅ 使用 Key 避免不必要的 recomposition
LazyColumn {
    items(songs, key = { it.id }) { song ->
        SongItem(song)
    }
}

// ✅ 使用 derivedStateOf 減少 recomposition
val showScrollToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 5 }
}

// ✅ 延遲讀取狀態 (Deferred State Reading)
Box(
    modifier = Modifier.offset {
        IntOffset(0, scrollState.value)  // lambda 內讀取
    }
)
```

### 4.2 狀態管理

| 情境 | 工具 | 說明 |
|------|------|------|
| UI 暫存狀態 | `remember` + `mutableStateOf` | 組件內部狀態 |
| 存活配置變更 | `rememberSaveable` | Process Death 恢復 |
| 業務狀態 | `ViewModel` + `StateFlow` | 跨組件共享 |
| 深層狀態保存 | `SavedStateHandle` | ViewModel 中使用 |

```kotlin
// ✅ ViewModel 最佳實踐
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playMusicUseCase: PlayMusicUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 使用 StateFlow 暴露狀態
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // 處理 UI 事件
    fun onPlayClick() {
        viewModelScope.launch {
            playMusicUseCase()
        }
    }
}
```

### 4.3 組件設計原則

```kotlin
// ✅ 無狀態組件 (Stateless) - 優先採用
@Composable
fun SongCard(
    song: Song,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 只負責呈現，不持有狀態
}

// ✅ 狀態提升 (State Hoisting)
@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<Song>,
    modifier: Modifier = Modifier
) {
    // 狀態由呼叫端管理
}
```

---

## 第五章：Media3 播放器規範

### 5.1 核心架構

```
┌─────────────────────────────────────────────┐
│              MediaSessionService             │
│  ┌─────────────────────────────────────┐    │
│  │            MediaSession              │    │
│  │  ┌─────────────────────────────┐    │    │
│  │  │        ExoPlayer            │    │    │
│  │  │  (實際播放引擎)              │    │    │
│  │  └─────────────────────────────┘    │    │
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
           ▲                    ▲
           │                    │
    ┌──────┴──────┐      ┌─────┴─────┐
    │ App UI      │      │ 外部控制   │
    │ (Compose)   │      │ (通知、   │
    │             │      │ Android    │
    │             │      │ Auto、     │
    │             │      │ 耳機按鍵)  │
    └─────────────┘      └───────────┘
```

### 5.2 必要實作

| 功能 | 實作要求 |
|------|----------|
| **背景播放** | `MediaSessionService` + Foreground Service |
| **媒體通知** | `MediaStyle` Notification with MediaSession |
| **音訊焦點** | `setAudioAttributes()` 設定正確類型 |
| **外部控制** | MediaSession Callback 處理 |
| **Android Auto** | `MediaBrowserService` 提供可瀏覽內容 |
| **播放恢復** | `onPlay()` Callback 支援冷啟動恢復 |

### 5.3 生命週期管理

```kotlin
// ✅ 正確的播放器生命週期管理
class MusicService : MediaSessionService() {

    override fun onCreate() {
        super.onCreate()
        // 初始化 ExoPlayer 與 MediaSession
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(USAGE_MEDIA)
                    .build(),
                true  // handleAudioFocus = true
            )
            .build()
    }

    override fun onDestroy() {
        // 釋放資源
        player.release()
        mediaSession.release()
        super.onDestroy()
    }
}
```

---

## 第六章：程式碼風格規範

### 6.1 命名規範

| 類型 | 規範 | 範例 |
|------|------|------|
| **類別** | PascalCase | `MusicRepository`, `PlayerViewModel` |
| **函數** | camelCase, 動詞開頭 | `fetchSongs()`, `playMusic()` |
| **變數** | camelCase | `currentSong`, `isPlaying` |
| **常數** | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT` |
| **Package** | 小寫，無底線 | `com.pulse.music.domain` |
| **Composable** | PascalCase, 名詞 | `SongCard()`, `PlayerScreen()` |
| **UseCase** | 動詞 + 名詞 + UseCase | `GetSongsUseCase`, `PlayMusicUseCase` |

### 6.2 檔案結構

```kotlin
// 1. 版權聲明 (如有)
// 2. Package 宣告
package com.pulse.music.ui.player

// 3. Imports (按字母排序，分組)
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.pulse.music.domain.model.Song
import javax.inject.Inject

// 4. 類別/函數定義

/**
 * 播放器畫面的 ViewModel
 *
 * 負責管理播放狀態與處理 UI 事件
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playMusicUseCase: PlayMusicUseCase
) : ViewModel() {
    // ...
}
```

### 6.3 Kotlin 慣用語法

```kotlin
// ✅ 使用 scope functions
song?.let { playSong(it) }

// ✅ 使用 sealed class/interface
sealed interface PlayerState {
    data object Idle : PlayerState
    data object Loading : PlayerState
    data class Playing(val song: Song) : PlayerState
    data class Error(val message: String) : PlayerState
}

// ✅ 使用 Extension Functions
fun Long.formatDuration(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return "%d:%02d".format(minutes, seconds)
}

// ✅ 使用 Flow 操作符
repository.getSongs()
    .map { songs -> songs.sortedBy { it.title } }
    .catch { emit(emptyList()) }
    .collect { songs -> _uiState.update { it.copy(songs = songs) } }
```

---

## 第七章：測試策略

### 7.1 測試金字塔

```
          ┌─────────────┐
          │   E2E 測試   │  ← 少量，驗證關鍵流程
          │  (10-20%)   │
          ├─────────────┤
          │  整合測試    │  ← 中等，驗證模組互動
          │  (20-30%)   │
          ├─────────────┤
          │   單元測試   │  ← 大量，快速回饋
          │  (50-70%)   │
          └─────────────┘
```

### 7.2 各層測試策略

| 層級 | 測試類型 | 工具 |
|------|----------|------|
| **Domain** | 純 Kotlin 單元測試 | JUnit 5, MockK |
| **Data** | Repository 整合測試 | Room In-Memory DB |
| **ViewModel** | 狀態測試 | Turbine (Flow Testing) |
| **Compose UI** | 組件測試 | Compose Test Rule |

### 7.3 測試範例

```kotlin
// ✅ UseCase 單元測試
class GetSongsUseCaseTest {
    @Test
    fun `when repository returns songs, usecase returns Success`() = runTest {
        // Given
        val songs = listOf(Song(id = 1, title = "Test"))
        coEvery { repository.getSongs() } returns flowOf(songs)

        // When
        val result = getSongsUseCase().first()

        // Then
        assertThat(result).isEqualTo(songs)
    }
}

// ✅ ViewModel 狀態測試
class PlayerViewModelTest {
    @Test
    fun `when play clicked, state updates to Playing`() = runTest {
        viewModel.uiState.test {
            viewModel.onPlayClick()

            assertThat(awaitItem()).isInstanceOf(PlayerState.Idle::class.java)
            assertThat(awaitItem()).isInstanceOf(PlayerState.Playing::class.java)
        }
    }
}
```

---

## 第八章：Git 工作流程

### 8.1 Commit 規範 (Conventional Commits)

```
<類型>(<範圍>): <描述>

[可選的主體內容]

[可選的頁腳]
```

| 類型 | 用途 |
|------|------|
| `feat` | 新功能 |
| `fix` | 錯誤修復 |
| `docs` | 文件變更 |
| `style` | 格式調整 (不影響程式邏輯) |
| `refactor` | 重構 (不新增功能或修復錯誤) |
| `perf` | 效能優化 |
| `test` | 測試相關 |
| `chore` | 建構或輔助工具變更 |

**範例**：
```
feat(player): add sleep timer functionality

- Add SleepTimerUseCase for countdown logic
- Integrate with MediaSessionService
- Add UI controls in NowPlayingScreen

Closes #123
```

### 8.2 分支策略

```
main ─────────────────────────────────────────────────►
  │                                        ▲
  │ checkout                               │ merge
  ▼                                        │
feature/add-sleep-timer ──────────────────►┘
```

### 8.3 推送規範

> ⚠️ **強制規則**：完成任務或歸檔變更後，**必須立即**將變更推送到遠端儲存庫。

```bash
git push origin <branch>
# 或 (如果上游已設定)
git push
```

---

## 第九章：互動協議

### 9.1 語言規範

| 項目 | 規範 |
|------|------|
| **AI 回覆語言** | **繁體中文 (Traditional Chinese)** |
| **程式碼註解** | 英文 (簡潔) 或繁體中文 |
| **文件撰寫** | 繁體中文 |
| **Commit 訊息** | 英文 (遵循 Conventional Commits) |

### 9.2 Agent 操作規範

1. **Context 優先**：任何任務開始前，先讀取 `openspec/project.md` 與相關 specs
2. **增量交付**：避免巨大變更，每個階段性成果都應可獨立驗證
3. **錯誤處理**：當 `run_command` 無預期輸出時，改用 Shell Session
4. **自主執行**：多步驟任務應自動連續執行，僅在致命錯誤時暫停

---

## 第十章：效能指標

### 10.1 目標指標

| 指標 | 目標 | 說明 |
|------|------|------|
| **冷啟動時間** | < 1.5s | 首次啟動到可互動 |
| **播放延遲** | < 300ms | 點擊到音樂開始 |
| **記憶體使用** | < 150MB | 正常使用情境 |
| **電池消耗** | < 3%/hr | 背景播放時 |
| **APK 大小** | < 20MB | Release 版本 |

### 10.2 優化原則

1. **重運算移至背景**：使用 `Dispatchers.Default` 處理資料操作
2. **延遲初始化**：非必要元件使用 `lazy` 或 `@Inject lateinit`
3. **資源釋放**：及時釋放 ExoPlayer、Bitmap 等資源
4. **避免過度繪製**：使用 Layout Inspector 檢查

---

## 附錄：快速參考

### A. 模組依賴快速查表

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":ui"))
    implementation(project(":player"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
}
```

### B. 常用 ADB 指令

```bash
# 安裝 Debug APK
./gradlew installDebug

# 清除應用資料
adb shell pm clear com.pulse.music

# 啟動 App
adb shell am start -n com.pulse.music/.MainActivity

# 查看 Logcat (過濾)
adb logcat -s Pulse
```

### C. 建構指令

```bash
# Debug 建構
./gradlew assembleDebug

# Release 建構
./gradlew assembleRelease

# 執行測試
./gradlew test

# Lint 檢查
./gradlew :app:lintDebug
```

---

*此規範採用 2025/2026 年 Android 開發最佳實踐，定期更新以保持與時俱進。*
