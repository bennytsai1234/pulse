# Design: 交叉淡入淡出播放技術設計

> **Change ID**: `add-crossfade-playback`
> **版本**: 1.0
> **建立日期**: 2026-01-05

---

## 1. 系統架構

### 1.1 元件圖

```
┌─────────────────────────────────────────────────────────────────┐
│                         App Layer                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                 CrossfadeSettingsScreen                  │    │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐    │    │
│  │  │ Toggle      │ │ Duration    │ │ Curve Selector  │    │    │
│  │  │ Switch      │ │ Slider      │ │ (Lin/Exp/S)     │    │    │
│  │  └─────────────┘ └─────────────┘ └─────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              PlaybackSettingsViewModel                   │    │
│  │  • crossfadeEnabled: StateFlow<Boolean>                  │    │
│  │  • crossfadeDuration: StateFlow<Int>                     │    │
│  │  • crossfadeCurve: StateFlow<CrossfadeCurve>            │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                CrossfadeSettingsUseCase                  │    │
│  │  + getCrossfadeSettings(): Flow<CrossfadeSettings>      │    │
│  │  + updateCrossfadeSettings(settings): Unit              │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              CrossfadeSettings (Model)                   │    │
│  │  • enabled: Boolean                                      │    │
│  │  • durationMs: Int (1000-12000)                         │    │
│  │  • curve: CrossfadeCurve                                │    │
│  │  • applyOnManualSkip: Boolean                           │    │
│  │  • albumContinuous: Boolean                             │    │
│  │  • silenceDetection: Boolean                            │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Player Layer                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                  CrossfadeController                     │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │              DualPlayerManager                   │    │    │
│  │  │  ┌─────────────┐     ┌─────────────┐            │    │    │
│  │  │  │ ExoPlayer A │     │ ExoPlayer B │            │    │    │
│  │  │  │ (Primary)   │     │ (Secondary) │            │    │    │
│  │  │  └─────────────┘     └─────────────┘            │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │           VolumeAnimator                         │    │    │
│  │  │  + fadeOut(player, duration, curve)             │    │    │
│  │  │  + fadeIn(player, duration, curve)              │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │           SilenceDetector                        │    │    │
│  │  │  + detectSilenceStart(audio): Long?             │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 資料流

```
User toggles crossfade
        │
        ▼
┌─────────────────┐
│ Update Settings │
│ (DataStore)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Flow emits new  │
│ settings        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ CrossfadeCtrl   │
│ reconfigures    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Next transition │
│ uses new config │
└─────────────────┘
```

---

## 2. 核心演算法

### 2.1 交叉淡入淡出時序

```kotlin
// 時序計算
val crossfadeDuration = settings.durationMs  // e.g. 5000ms
val triggerPosition = currentTrackDuration - crossfadeDuration

// 當播放位置到達觸發點
if (currentPosition >= triggerPosition) {
    startCrossfade()
}
```

### 2.2 音量曲線

```kotlin
enum class CrossfadeCurve {
    LINEAR,      // y = x
    EXPONENTIAL, // y = x^2
    S_CURVE      // y = 3x^2 - 2x^3 (smooth step)
}

fun calculateVolume(progress: Float, curve: CrossfadeCurve): Float {
    return when (curve) {
        LINEAR -> progress
        EXPONENTIAL -> progress * progress
        S_CURVE -> progress * progress * (3 - 2 * progress)
    }
}
```

### 2.3 雙播放器管理

```kotlin
class DualPlayerManager @Inject constructor(
    private val playerFactory: ExoPlayer.Factory
) {
    private var primaryPlayer: ExoPlayer? = null
    private var secondaryPlayer: ExoPlayer? = null
    private var activePlayer: ExoPlayer? = null

    fun swapPlayers() {
        val temp = primaryPlayer
        primaryPlayer = secondaryPlayer
        secondaryPlayer = temp
        activePlayer = primaryPlayer
    }

    fun prepareNextTrack(mediaItem: MediaItem) {
        secondaryPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            volume = 0f  // Start silent
        }
    }
}
```

---

## 3. 資料模型

### 3.1 CrossfadeSettings

```kotlin
@Immutable
data class CrossfadeSettings(
    val enabled: Boolean = false,
    val durationMs: Int = 5000,
    val curve: CrossfadeCurve = CrossfadeCurve.LINEAR,
    val applyOnManualSkip: Boolean = true,
    val albumContinuous: Boolean = true,
    val silenceDetection: Boolean = false,
    val silenceThresholdDb: Float = -45f
) {
    companion object {
        const val MIN_DURATION_MS = 1000
        const val MAX_DURATION_MS = 12000
        const val DEFAULT_DURATION_MS = 5000
    }
}
```

### 3.2 Proto DataStore Schema

```protobuf
message CrossfadeSettingsProto {
  bool enabled = 1;
  int32 duration_ms = 2;
  CrossfadeCurveProto curve = 3;
  bool apply_on_manual_skip = 4;
  bool album_continuous = 5;
  bool silence_detection = 6;
  float silence_threshold_db = 7;
}

enum CrossfadeCurveProto {
  LINEAR = 0;
  EXPONENTIAL = 1;
  S_CURVE = 2;
}
```

---

## 4. UI 設計

### 4.1 設定畫面結構

```
┌─────────────────────────────────────────┐
│ ← 播放設定                              │
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 交叉淡入淡出                    [ON]│ │
│ │ 歌曲之間平滑過渡                    │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ 淡入淡出時長                            │
│ ┌─────────────────────────────────────┐ │
│ │ 1s ────●───────────────────── 12s  │ │
│ │              5 秒                   │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ 淡入淡出曲線                            │
│ ┌─────────────────────────────────────┐ │
│ │ ○ 線性    ● 指數    ○ S 曲線       │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ─────────── 進階設定 ─────────────────  │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 手動跳轉時套用              [ON]   │ │
│ │ 切換歌曲時也使用淡入淡出            │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 專輯連續模式                [ON]   │ │
│ │ 同專輯歌曲使用無縫過渡              │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 智慧靜音偵測                [OFF]  │ │
│ │ 自動偵測歌曲結尾靜音段落            │ │
│ └─────────────────────────────────────┘ │
│                                         │
└─────────────────────────────────────────┘
```

### 4.2 視覺化預覽 (可選)

在設定頁面頂部顯示動態預覽：
- 兩個音量條動畫展示淡入淡出效果
- 根據選擇的曲線即時更新動畫

---

## 5. 錯誤處理

### 5.1 邊界情況

| 情況 | 處理方式 |
|------|----------|
| 歌曲長度 < 淡入淡出時長 | 使用歌曲長度的 50% 作為淡入淡出時長 |
| 快速連續跳轉 | 取消進行中的淡入淡出，直接切換 |
| 記憶體不足 | 降級為單播放器模式 |
| 播放器初始化失敗 | 使用主播放器，禁用交叉淡入淡出 |

### 5.2 資源管理

```kotlin
// 在交叉淡入淡出完成後釋放舊播放器資源
private fun onCrossfadeComplete() {
    secondaryPlayer?.let { player ->
        player.stop()
        player.clearMediaItems()
        // 不釋放播放器實例，重複使用以減少 GC
    }
}
```

---

## 6. 效能考量

### 6.1 記憶體使用

| 元件 | 預估記憶體 |
|------|-----------|
| Secondary ExoPlayer | ~8-10 MB |
| Audio Buffer (雙倍) | ~2-4 MB |
| 總增加 | ~10-15 MB |

### 6.2 CPU 使用

- 正常播放：無額外負擔
- 交叉淡入淡出期間：+3-5% (兩個解碼器同時運作)
- 使用 Coroutine 進行音量動畫，避免主執行緒阻塞

### 6.3 電池影響

- 背景播放時交叉淡入淡出仍運作
- 預估增加 1-2% 電池消耗 (8 小時背景播放)

---

## 7. 測試策略

### 7.1 單元測試

- `CrossfadeController` 狀態轉換測試
- `VolumeAnimator` 曲線計算測試
- `DualPlayerManager` 播放器切換測試

### 7.2 整合測試

- 完整交叉淡入淡出流程測試
- 設定變更即時生效測試
- 與等化器/音量正規化整合測試

### 7.3 手動測試

- 各種歌曲長度的交叉淡入淡出效果
- 快速連續操作的穩定性
- 低端設備效能驗證

---

## 8. 未來擴展

1. **視覺化波形預覽**：在設定頁面顯示歌曲波形與淡入淡出區間
2. **每首歌自訂設定**：允許特定歌曲禁用交叉淡入淡出
3. **AI 驅動過渡**：根據歌曲 BPM 和調性自動調整過渡時機
