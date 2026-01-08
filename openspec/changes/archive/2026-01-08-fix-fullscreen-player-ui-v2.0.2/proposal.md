# Proposal: Fix Fullscreen Player UI Issues (v2.1.1)

## Goal
修復兩個影響用戶體驗的 UI 問題，確保全螢幕播放器和導航介面的一致性與可用性。

## Problems Identified

### 問題 1：字母索引被 Mini Player 遮擋
**位置**：首頁全螢幕播放器右側的字母快速滾動條
**症狀**：字母 `Z` 和 `#` 符號被底部的 Mini Player 遮住，無法點擊
**現有措施**：`FastScroller` 已套用 `Modifier.padding(bottom = 80.dp)` 但仍不足
**用戶建議**：
  - 選項 A：整體上移字母索引
  - 選項 B：減少字母間距

### 問題 2：導航返回鍵一致性問題
**2A - DiscoverScreen 缺少返回鍵**：
- 「探索」頁面使用 `LargeTopAppBar` 而非 `PulseTopBarWithBack`
- 用戶無法按返回鍵離開此頁面（需依賴系統手勢或物理返回）

**2B - 返回鍵垂直位置不一致**：
- 從抽屜選單進入的子頁面（播放清單、最愛、探索等）
- 其返回鍵位置比主畫面的選單按鈕稍低
- 破壞視覺一致性，不符合 `ui-consistency` spec 的 "Navigation Button Alignment" 要求

## Why (為何重要)
1. **字母索引遮擋**：阻止用戶快速導航至 Z 或 # 開頭的歌曲，降低使用效率
2. **缺少返回鍵**：違反 Android 導航慣例，用戶可能不知如何返回
3. **返回鍵位置不一致**：違反 `ui-consistency` spec 中 "Navigation Button Alignment" 要求，破壞專業感

## Proposed Solution

### 解決方案 1：優化 FastScroller 字母布局
採用用戶建議的 **選項 B**：減少字母間距（搭配適當上移）

**原因**：
- 27 個字符（A-Z + #）需要在有限空間內完全顯示
- 減少間距可保持全部字母可見且可點擊
- 配合增加底部邊距確保不被 Mini Player 遮擋

**實作要點**：
- 調整 `FastScroller` 的 Column 佈局策略
- 優化頂部/底部 padding 分配
- 確保在各種螢幕尺寸下字母完整顯示

### 解決方案 2A：DiscoverScreen 添加返回鍵
- 將 `LargeTopAppBar` 替換為自訂組合：
  - 保留現有的大標題效果（可使用 `collapsible` 行為）
  - 添加導航返回按鈕（與 `PulseTopBarWithBack` 一致）
- 接收 `onBackClick` callback 參數

### 解決方案 2B：統一 TopBar 高度
- 檢視 `PulseTopBar` 與 `PulseTopBarWithBack` 的高度設定
- 如有差異，統一使用 M3 標準 `TopAppBarDefaults` 的固定高度
- 確保所有使用 `PulseNavigation` 組件的畫面視覺對齊

## Files to Modify
1. `ui/src/main/java/com/pulse/music/ui/home/HomeScreen.kt` - FastScroller 組件
2. `ui/src/main/java/com/pulse/music/ui/discover/DiscoverScreen.kt` - 添加返回鍵
3. `core/designsystem/.../PulseNavigation.kt` - 確認 TopBar 高度一致性（如需）

## Risks
1. **FastScroller 調整**：過小的間距可能影響觸控精準度（將測試最小觸控目標 44dp）
2. **DiscoverScreen TopBar 變更**：需保持可折疊大標題的視覺效果
3. **高度統一化**：可能影響其他已使用 TopBar 的畫面

## Verification
1. 滾動首頁至底部 → 確認 Z 和 # 完全可見且可點擊
2. 從抽屜選單進入「探索」→ 確認有返回按鈕且可用
3. 對比首頁選單按鈕與子頁面返回按鈕的垂直位置 → 應完全對齊
4. 在不同螢幕尺寸測試字母索引的可用性
