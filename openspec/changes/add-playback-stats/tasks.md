# Tasks: 播放統計與洞察實作清單

> **Change ID**: `add-playback-stats`
> **預估工時**: 4-5 天
> **依賴**: 無

---

## 1. 資料層建構 (Day 1)

### 1.1 Database Entities
- [ ] 1.1.1 建立 `PlaybackHistoryEntity` (`data/database/entity/`)
- [ ] 1.1.2 建立 `SongStatsEntity` (`data/database/entity/`)
- [ ] 1.1.3 建立 `DailyStatsEntity` (`data/database/entity/`)
- [ ] 1.1.4 新增資料庫版本遷移 (Room Migration)

### 1.2 DAO
- [ ] 1.2.1 建立 `StatsDao` (`data/database/dao/`)
- [ ] 1.2.2 實作播放記錄插入方法
- [ ] 1.2.3 實作歌曲統計 upsert 方法
- [ ] 1.2.4 實作每日統計彙總查詢
- [ ] 1.2.5 實作最常播放排行查詢
- [ ] 1.2.6 實作連續天數計算查詢

### 1.3 Repository
- [ ] 1.3.1 建立 `StatsRepository` 介面 (`domain/repository/`)
- [ ] 1.3.2 實作 `StatsRepositoryImpl` (`data/repository/`)
- [ ] 1.3.3 實作快取機制
- [ ] 1.3.4 提供 Hilt DI 綁定

---

## 2. Domain 層建構 (Day 2)

### 2.1 Models
- [ ] 2.1.1 建立 `OverviewStats` 資料模型 (`domain/model/`)
- [ ] 2.1.2 建立 `SongPlayStats` 資料模型
- [ ] 2.1.3 建立 `ListeningTrend` 資料模型
- [ ] 2.1.4 建立 `PlaybackCompletionRules` 物件

### 2.2 UseCases
- [ ] 2.2.1 建立 `RecordPlaybackUseCase` (`domain/usecase/stats/`)
- [ ] 2.2.2 建立 `GetOverviewStatsUseCase`
- [ ] 2.2.3 建立 `GetTopPlayedSongsUseCase`
- [ ] 2.2.4 建立 `GetListeningTrendUseCase`
- [ ] 2.2.5 建立 `GetCurrentStreakUseCase`
- [ ] 2.2.6 建立 `GetUnplayedSongsUseCase`

---

## 3. Player 整合 (Day 2-3)

### 3.1 播放追蹤器
- [ ] 3.1.1 建立 `PlaybackTracker` 類別 (`player/stats/`)
- [ ] 3.1.2 實作 `onPlaybackStarted()` 邏輯
- [ ] 3.1.3 實作 `onPlaybackProgress()` 邏輯
- [ ] 3.1.4 實作 `onPlaybackEnded()` 邏輯
- [ ] 3.1.5 實作完成/跳過判定邏輯

### 3.2 MusicController 整合
- [ ] 3.2.1 在 `MusicController` 注入 `PlaybackTracker`
- [ ] 3.2.2 連接播放狀態變化到追蹤器
- [ ] 3.2.3 處理各種播放情境 (暫停、恢復、跳轉)

---

## 4. UI 實作 (Day 3-4)

### 4.1 統計首頁
- [ ] 4.1.1 建立 `StatsScreen` Composable (`ui/stats/`)
- [ ] 4.1.2 實作概覽卡片區塊 (本週聆聽、連續天數)
- [ ] 4.1.3 實作總聆聽時長卡片
- [ ] 4.1.4 實作「最常播放」列表元件
- [ ] 4.1.5 實作「發現遺珠」區塊

### 4.2 趨勢圖表
- [ ] 4.2.1 建立 `TrendChart` Composable (`ui/stats/component/`)
- [ ] 4.2.2 實作柱狀圖渲染 (Canvas/Compose)
- [ ] 4.2.3 實作日期軸標籤
- [ ] 4.2.4 實作觸控互動 (點擊顯示詳細)

### 4.3 詳細頁面
- [ ] 4.3.1 建立 `TopSongsScreen` - 完整排行榜
- [ ] 4.3.2 建立 `TopArtistsScreen` - 藝人排行榜
- [ ] 4.3.3 建立 `TopAlbumsScreen` - 專輯排行榜

### 4.4 ViewModel
- [ ] 4.4.1 建立 `StatsViewModel` (`ui/stats/`)
- [ ] 4.4.2 實作各項統計狀態管理
- [ ] 4.4.3 實作資料載入與錯誤處理

### 4.5 導覽整合
- [ ] 4.5.1 在主選單新增「聆聽統計」入口
- [ ] 4.5.2 更新導覽圖 (Navigation Graph)
- [ ] 4.5.3 在個人資料/設定區塊新增捷徑

---

## 5. 進階功能 (Day 4-5)

### 5.1 背景彙總
- [ ] 5.1.1 建立 `StatsAggregationWorker` (WorkManager)
- [ ] 5.1.2 實作每日統計彙總邏輯
- [ ] 5.1.3 實作舊資料清理邏輯 (>1 年)
- [ ] 5.1.4 設定週期性執行 (每日凌晨)

### 5.2 歌曲詳情整合
- [ ] 5.2.1 在歌曲資訊彈窗顯示播放次數
- [ ] 5.2.2 在專輯詳情顯示播放統計
- [ ] 5.2.3 在藝人頁面顯示聆聽時長

### 5.3 發現功能
- [ ] 5.3.1 實作「遺忘的寶藏」推薦邏輯
- [ ] 5.3.2 實作「久未播放」歌曲篩選
- [ ] 5.3.3 在首頁添加發現入口

---

## 6. 測試與驗證

### 6.1 單元測試
- [ ] 6.1.1 `PlaybackCompletionRules` 判定測試
- [ ] 6.1.2 Streak 計算測試
- [ ] 6.1.3 週變化百分比計算測試

### 6.2 整合測試
- [ ] 6.2.1 `StatsDao` 查詢正確性測試
- [ ] 6.2.2 播放追蹤完整流程測試
- [ ] 6.2.3 資料庫遷移測試

### 6.3 UI 測試
- [ ] 6.3.1 統計卡片顯示測試
- [ ] 6.3.2 空狀態處理測試
- [ ] 6.3.3 圖表渲染測試

### 6.4 建構驗證
- [ ] 6.4.1 執行 `./gradlew assembleDebug` 確認建構成功
- [ ] 6.4.2 執行 `./gradlew test` 確認測試通過
- [ ] 6.4.3 執行 `./gradlew :app:lintDebug` 確認無 Lint 錯誤

---

## 7. 文件更新

- [ ] 7.1 更新 `RELEASE_NOTES.md` 新增功能說明
- [ ] 7.2 更新 `README.md` 功能列表
- [ ] 7.3 更新 OpenSpec specs (歸檔時)

---

## 依賴關係圖

```
1.1 Database Entities
    │
    └──► 1.2 DAO
              │
              └──► 1.3 Repository
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
        2.2 UseCases         3.1 PlaybackTracker
              │                   │
              └─────────┬─────────┘
                        ▼
                   4. UI Layer
                        │
                        └──► 5. Advanced Features
                                    │
                                    └──► 6. Testing
```

---

## 風險與緩解

| 風險 | 緩解策略 |
|------|----------|
| 資料庫增長過快 | 定期清理 >1 年的詳細記錄 |
| 播放追蹤遺漏 | 多點觸發 (開始/結束/中斷) |
| 統計計算耗時 | 使用背景 Worker + 快取 |
| 圖表效能問題 | 限制渲染資料點數量 |
