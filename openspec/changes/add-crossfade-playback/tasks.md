# Tasks: 交叉淡入淡出播放實作清單

> **Change ID**: `add-crossfade-playback`
> **預估工時**: 3-4 天
> **依賴**: 無

---

## 1. 基礎架構 (Day 1)

### 1.1 Domain Layer
- [x] 1.1.1 建立 `CrossfadeSettings` 資料模型 (`domain/model/`)
- [x] 1.1.2 建立 `CrossfadeCurve` 列舉類型
- [x] 1.1.3 建立 `CrossfadeSettingsRepository` 介面 (`domain/repository/`)
- [x] 1.1.4 建立 `GetCrossfadeSettingsUseCase` (`domain/usecase/crossfade/`)
- [x] 1.1.5 建立 `UpdateCrossfadeSettingsUseCase` (`domain/usecase/crossfade/`)

### 1.2 Data Layer
- [x] 1.2.1 建立 CrossfadeSettings DataStore schema (使用 Preferences DataStore)
- [x] 1.2.2 實作 `CrossfadeSettingsRepositoryImpl` (`data/repository/`)
- [x] 1.2.3 提供 Hilt DI 綁定

---

## 2. Player 核心實作 (Day 2)

### 2.1 雙播放器管理
- [x] 2.1.1 建立 `DualPlayerManager` 類別 (`player/crossfade/`)
- [x] 2.1.2 實作播放器池化與重用機制
- [x] 2.1.3 實作播放器切換邏輯

### 2.2 音量動畫
- [x] 2.2.1 建立 `VolumeAnimator` 類別 (`player/crossfade/`)
- [x] 2.2.2 實作線性/指數/S 曲線計算
- [x] 2.2.3 使用 Coroutine 實作平滑動畫

### 2.3 交叉淡入淡出控制器
- [x] 2.3.1 建立 `CrossfadeController` 類別 (`player/crossfade/`)
- [x] 2.3.2 實作觸發點偵測邏輯
- [x] 2.3.3 實作完整交叉淡入淡出流程
- [x] 2.3.4 整合到現有 `MusicController` (已整合至 PulseAudioService)

---

## 3. UI 實作 (Day 3)

### 3.1 播放設定畫面
- [x] 3.1.1 建立 `CrossfadeSettingsScreen` Composable (`ui/settings/crossfade/`)
- [x] 3.1.2 實作主開關 Switch 元件
- [x] 3.1.3 實作時長調整 Slider 元件
- [x] 3.1.4 實作曲線選擇 RadioGroup 元件
- [x] 3.1.5 實作進階設定區塊 (可收合)

### 3.2 ViewModel
- [x] 3.2.1 建立 `CrossfadeSettingsViewModel` (`ui/settings/crossfade/`)
- [x] 3.2.2 實作設定狀態管理
- [x] 3.2.3 實作設定更新處理

### 3.3 導覽整合
- [x] 3.3.1 在「播放設定」頁面新增入口
- [x] 3.3.2 更新導覽圖 (Navigation Graph)

---

## 4. 進階功能 (Day 4)

### 4.1 智慧功能
- [x] 4.1.1 實作「手動跳轉時套用」邏輯 (在 CrossfadeController 中實作)
- [x] 4.1.2 實作「專輯連續模式」邏輯 (在 CrossfadeController 中實作)
- [ ] 4.1.3 (可選) 實作靜音偵測 `SilenceDetector` (已預留介面，待後續實作)

### 4.2 錯誤處理
- [x] 4.2.1 處理短歌曲邊界情況 (在 CrossfadeController 中實作)
- [x] 4.2.2 處理快速連續跳轉 (在 CrossfadeController 中實作)
- [x] 4.2.3 處理記憶體不足降級 (在 DualPlayerManager 中實作)

---

## 5. 測試與驗證

### 5.1 單元測試
- [ ] 5.1.1 `VolumeAnimator` 曲線計算測試 (待後續實作)
- [ ] 5.1.2 `CrossfadeController` 狀態測試 (待後續實作)
- [ ] 5.1.3 `DualPlayerManager` 切換測試 (待後續實作)

### 5.2 整合驗證
- [ ] 5.2.1 完整播放流程手動測試 (需裝置測試)
- [ ] 5.2.2 與等化器共存測試 (需裝置測試)
- [ ] 5.2.3 低端設備效能驗證 (需裝置測試)

### 5.3 建構驗證
- [x] 5.3.1 執行 `./gradlew assembleDebug` 確認建構成功
- [ ] 5.3.2 執行 `./gradlew test` 確認測試通過
- [ ] 5.3.3 執行 `./gradlew :app:lintDebug` 確認無 Lint 錯誤

---

## 6. 文件更新

- [ ] 6.1 更新 `RELEASE_NOTES.md` 新增功能說明
- [ ] 6.2 更新 `README.md` 功能列表 (若適用)
- [ ] 6.3 更新 OpenSpec specs (歸檔時自動處理)

---

## 依賴關係圖

```
1.1 Domain Model
    │
    ├──► 1.2 Data Layer
    │         │
    │         └──► 2.3 CrossfadeController
    │                    │
    ├──► 2.1 DualPlayerManager ──┘
    │         │
    └──► 2.2 VolumeAnimator ─────┘
                    │
                    └──► 3. UI Implementation
                              │
                              └──► 4. Advanced Features
                                        │
                                        └──► 5. Testing
```

---

## 風險與緩解

| 風險 | 緩解策略 |
|------|----------|
| 雙播放器記憶體過高 | 實作降級機制，低記憶體時禁用 |
| 音訊同步問題 | 使用精確的時間戳同步 |
| 與其他音效處理衝突 | 確保音效鏈正確串接 |

---

## 實作總結

**已完成的核心功能：**
1. ✅ Domain Layer 完整實作 (CrossfadeSettings, CrossfadeCurve, Repository 介面, Use Cases)
2. ✅ Data Layer 完整實作 (Repository 實作, DI 綁定)
3. ✅ Player 核心元件 (DualPlayerManager, VolumeAnimator, CrossfadeController)
4. ✅ UI 完整實作 (CrossfadeSettingsScreen, ViewModel, 導覽整合)
5. ✅ 整合 CrossfadeController 到 PulseAudioService
6. ✅ 建構驗證通過

**待後續迭代完成：**
1. ⏳ 單元測試
2. ⏳ 靜音偵測功能
3. ⏳ 裝置整合測試
