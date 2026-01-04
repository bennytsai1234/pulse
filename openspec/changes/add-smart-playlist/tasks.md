# Tasks: 智慧播放清單實作清單

> **Change ID**: `add-smart-playlist`
> **預估工時**: 4-5 天
> **依賴**: `add-playback-stats` (需要 song_stats 表格)

---

## 1. 資料層建構 (Day 1)

### 1.1 Database Entities
- [ ] 1.1.1 建立 `SmartPlaylistEntity` (`data/database/entity/`)
- [ ] 1.1.2 建立 `SmartPlaylistRuleEntity` (`data/database/entity/`)
- [ ] 1.1.3 新增資料庫版本遷移 (Room Migration)
- [ ] 1.1.4 建立 `SmartPlaylistWithRules` 關聯類別

### 1.2 DAO
- [ ] 1.2.1 建立 `SmartPlaylistDao` (`data/database/dao/`)
- [ ] 1.2.2 實作智慧清單 CRUD 方法
- [ ] 1.2.3 實作規則 CRUD 方法
- [ ] 1.2.4 實作動態查詢執行 (`@RawQuery`)

### 1.3 Repository
- [ ] 1.3.1 建立 `SmartPlaylistRepository` 介面 (`domain/repository/`)
- [ ] 1.3.2 實作 `SmartPlaylistRepositoryImpl` (`data/repository/`)
- [ ] 1.3.3 實作規則到 Entity 的映射
- [ ] 1.3.4 提供 Hilt DI 綁定

---

## 2. Domain 層建構 (Day 1-2)

### 2.1 Models
- [ ] 2.1.1 建立 `SmartPlaylist` 資料模型 (`domain/model/`)
- [ ] 2.1.2 建立 `RuleCondition` sealed class 及所有子類別
- [ ] 2.1.3 建立 `RuleOperator` 列舉
- [ ] 2.1.4 建立 `RuleLogic`, `SortOption`, `SortOrder` 列舉

### 2.2 Query Engine
- [ ] 2.2.1 建立 `SmartPlaylistQueryEngine` (`domain/smartplaylist/`)
- [ ] 2.2.2 實作 `buildQuery()` 方法
- [ ] 2.2.3 實作各種 `RuleCondition` 到 SQL 轉換
- [ ] 2.2.4 實作排序與限制子句生成

### 2.3 UseCases
- [ ] 2.3.1 建立 `CreateSmartPlaylistUseCase` (`domain/usecase/smartplaylist/`)
- [ ] 2.3.2 建立 `UpdateSmartPlaylistUseCase`
- [ ] 2.3.3 建立 `DeleteSmartPlaylistUseCase`
- [ ] 2.3.4 建立 `GetSmartPlaylistsUseCase`
- [ ] 2.3.5 建立 `GetSmartPlaylistSongsUseCase`
- [ ] 2.3.6 建立 `GetSystemSmartPlaylistsUseCase`

### 2.4 系統預設清單
- [ ] 2.4.1 建立 `SystemSmartPlaylists` 物件
- [ ] 2.4.2 定義 6 個系統預設智慧清單
- [ ] 2.4.3 確保系統清單使用負數 ID 區分

---

## 3. UI 實作 - 編輯器 (Day 2-3)

### 3.1 編輯器畫面
- [ ] 3.1.1 建立 `SmartPlaylistEditorScreen` (`ui/playlist/smart/`)
- [ ] 3.1.2 實作名稱與圖示編輯區塊
- [ ] 3.1.3 實作邏輯切換 (AND/OR) UI
- [ ] 3.1.4 實作排序選項 UI
- [ ] 3.1.5 實作數量限制 UI

### 3.2 規則條件編輯器
- [ ] 3.2.1 建立 `RuleConditionEditor` Composable
- [ ] 3.2.2 實作條件類型下拉選單
- [ ] 3.2.3 實作運算子下拉選單 (根據類型動態變化)
- [ ] 3.2.4 實作各類型值輸入元件 (數字/文字/日期)
- [ ] 3.2.5 實作規則新增/刪除功能

### 3.3 即時預覽
- [ ] 3.3.1 建立 `LivePreview` Composable
- [ ] 3.3.2 實作防抖延遲查詢
- [ ] 3.3.3 實作載入狀態顯示
- [ ] 3.3.4 實作歌曲數量顯示與預覽列表

### 3.4 ViewModel
- [ ] 3.4.1 建立 `SmartPlaylistEditorViewModel`
- [ ] 3.4.2 實作編輯狀態管理
- [ ] 3.4.3 實作預覽查詢邏輯
- [ ] 3.4.4 實作儲存/更新邏輯

---

## 4. UI 實作 - 列表與播放 (Day 3-4)

### 4.1 智慧清單列表
- [ ] 4.1.1 在現有播放清單頁面新增「智慧清單」區塊
- [ ] 4.1.2 建立 `SmartPlaylistItem` Composable
- [ ] 4.1.3 區分系統清單與自訂清單顯示
- [ ] 4.1.4 實作建立新智慧清單入口

### 4.2 智慧清單詳情
- [ ] 4.2.1 建立 `SmartPlaylistDetailScreen`
- [ ] 4.2.2 顯示歌曲列表 (即時查詢)
- [ ] 4.2.3 實作播放全部功能
- [ ] 4.2.4 實作隨機播放功能
- [ ] 4.2.5 實作編輯/刪除選項 (僅自訂清單)

### 4.3 導覽整合
- [ ] 4.3.1 更新 Navigation Graph 新增智慧清單路由
- [ ] 4.3.2 在「新增清單」對話框加入智慧清單選項
- [ ] 4.3.3 確保導覽動畫流暢

---

## 5. 進階功能 (Day 4-5)

### 5.1 播放整合
- [ ] 5.1.1 實作將智慧清單內容載入播放佇列
- [ ] 5.1.2 確保隨機播放正確運作
- [ ] 5.1.3 處理空清單邊界情況

### 5.2 首頁整合
- [ ] 5.2.1 在首頁新增「智慧清單」快捷區塊
- [ ] 5.2.2 顯示常用智慧清單卡片
- [ ] 5.2.3 實作快速播放功能

### 5.3 錯誤處理
- [ ] 5.3.1 處理無效規則配置
- [ ] 5.3.2 處理查詢超時
- [ ] 5.3.3 顯示友善的空狀態/錯誤訊息

---

## 6. 測試與驗證

### 6.1 單元測試
- [ ] 6.1.1 `SmartPlaylistQueryEngine` SQL 生成測試
- [ ] 6.1.2 `RuleCondition` 各類型轉換測試
- [ ] 6.1.3 複雜多條件組合測試

### 6.2 整合測試
- [ ] 6.2.1 完整查詢流程測試
- [ ] 6.2.2 系統預設清單測試
- [ ] 6.2.3 CRUD 操作測試

### 6.3 效能測試
- [ ] 6.3.1 1000 首歌曲查詢效能 (< 200ms)
- [ ] 6.3.2 多條件組合效能測試
- [ ] 6.3.3 預覽防抖行為測試

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
        2.1 Models           2.2 QueryEngine
              │                   │
              └─────────┬─────────┘
                        ▼
                   2.3 UseCases
                        │
              ┌─────────┴─────────┐
              ▼                   ▼
        3. Editor UI         4. List/Detail UI
              │                   │
              └─────────┬─────────┘
                        ▼
                   5. Advanced
                        │
                        └──► 6. Testing
```

---

## 風險與緩解

| 風險 | 緩解策略 |
|------|----------|
| 動態 SQL 注入 | 使用參數化查詢或嚴格 escape |
| 複雜查詢效能差 | 限制規則數量、加入索引 |
| 規則 UI 過於複雜 | 提供常用規則範本 |
| 與播放統計未整合 | 確保 `add-playback-stats` 先完成 |
