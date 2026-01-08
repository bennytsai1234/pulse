# Proposal: Enhance Player UX (v2.2.0)

## Goal
提升音樂播放器的使用體驗，修復多個 UI 問題並增加批量操作功能。

## Problems Identified

### 問題 1：Up Next (Queue) 介面問題
**1A - 返回按鈕位置不一致**：
- 使用 `Icons.Rounded.Close` (X 符號) 而非與其他頁面一致的返回箭頭
- TopBar 高度與主畫面不一致（使用標準 `PulseTopBar` 而非緊湊型 Row）

**1B - 歌曲時間永遠顯示 00:00**：
- **根本原因**：`MusicServiceConnection.kt` 中的 `MediaItem.toSong()` 擴展函數
- 第 298 行：`duration = 0` 硬編碼為 0
- `Song.toMediaItem()` 沒有將 duration 存入 MediaItem extras
- 因此 Queue 中所有歌曲的 duration 都是 0

### 問題 2：全螢幕播放器歌詞無法同步
**現狀**：
- `KaraokeLyrics` 組件已支援逐字高亮
- 但歌詞永遠不會按時間同步

**根本原因**：
- `NowPlayingScreen.kt` 第 202 行計算 `currentPosition`：
  ```kotlin
  currentPosition = (uiState.progress * (uiState.song?.duration ?: 1L)).toLong()
  ```
- 因為 `duration = 0`（問題 1B），所以 `currentPosition = progress * 0 = 0`
- 歌詞組件認為播放位置永遠是 0 毫秒，因此沒有任何行被高亮

**用戶期望**：
- 歌詞唱到哪行就「浮出來」的效果（類似 Apple Music 的歌詞動畫）
- 按時間戳同步高亮

### 問題 3：歌詞點擊返回專輯封面
**現狀**：
- 只有點擊專輯封面可以切換到歌詞 (`HeroImage.onClick`)
- 點擊歌詞區域無法返回封面

**用戶期望**：
- 點擊歌詞區域也能切換回專輯封面

### 問題 4：全螢幕播放器歌詞按鈕失效
**現狀**：
- `NowPlayingControls.kt` 第 175 行：
  ```kotlin
  IconButton(onClick = { /* Handled by HeroImage click or separate button if needed */ }) {
  ```
- 歌詞按鈕 (Icons.Rounded.Description) 的 onClick 是**空的**！
- 只有一個註解說明，沒有實際功能

**用戶期望**：
- 點擊歌詞按鈕可切換顯示歌詞

### 問題 5：主畫面長按選擇批量操作
**現狀**：
- 已有 `isSelectionMode` 機制
- 但沒有長按觸發進入選擇模式的功能
- 沒有批量刪除和創建播放清單的 UI

**用戶期望**：
- 長按歌曲進入選擇模式
- 可批量刪除歌曲
- 可將選中歌曲添加到新/現有播放清單

## Why (為何重要)
1. **導航一致性**：統一返回按鈕樣式提升專業感
2. **歌詞體驗**：卡拉OK效果增強沉浸感，讓用戶能享受跟唱
3. **操作便利性**：多種切換方式減少操作摩擦
4. **批量管理**：高效管理音樂庫，減少重複操作

## Proposed Solutions

### 解決方案 1：統一 QueueScreen 導航
- 將 TopBar 改為與 `PulseTopBarWithBack` 一致的緊湊型 Row
- 將 Close 圖示改為 ArrowBack
- 保持歌曲時間顯示（即使正在播放也顯示時間）

### 解決方案 2：增強歌詞「浮出」效果
- 為 `LyricLineView` 添加 Y 軸位移動畫（當前行向上浮動 -8dp）
- 增加發光/陰影效果強調當前歌詞
- 添加字體大小漸變動畫

### 解決方案 3：歌詞點擊返回封面
- 為 `KaraokeLyrics` 組件添加 `onLyricsClick` 回調
- 在 NowPlayingScreen 中處理點擊事件切換 `showLyrics` 狀態

### 解決方案 4：添加歌詞切換按鈕
- 在 `PlayerControls` 或 `NowPlayingBottomSection` 添加歌詞按鈕
- 按鈕位於播放控制區上方或下方
- 按鈕狀態反映當前歌詞顯示狀態

### 解決方案 5：長按選擇批量操作
- 為 `PulseSongListItem` 添加 `onLongClick` 事件
- 長按觸發進入選擇模式 (`enterSelectionMode()`)
- 添加選擇模式下的 BottomActionBar：
  - 「刪除」按鈕（批量刪除）
  - 「添加到播放清單」按鈕（彈出選擇/創建對話框）
  - 計數器顯示已選中數量

## Files to Modify
1. `player/.../manager/MusicServiceConnection.kt` - 修復 duration 資料流 (toSong/toMediaItem)
2. `ui/.../queue/QueueScreen.kt` - 修復 TopBar 樣式
3. `ui/.../component/KaraokeLyrics.kt` - 增強浮出動畫效果
4. `ui/.../nowplaying/NowPlayingScreen.kt` - 添加歌詞點擊和按鈕
5. `ui/.../nowplaying/components/PlayerControls.kt` - 添加歌詞按鈕
6. `ui/.../home/HomeScreenRedesigned.kt` - 添加長按和批量操作
7. `core/designsystem/.../component/PulseSongListItem.kt` - 添加 onLongClick

## Risks
1. **歌詞動畫**：過於複雜的動畫可能影響低端設備效能
2. **批量刪除**：需要確認系統權限處理（Android 11+ Scoped Storage）
3. **UI 複雜度**：增加太多控制按鈕可能使介面擁擠

## Verification
1. 對比 QueueScreen 返回按鈕與其他頁面 → 應一致
2. 播放歌曲時觀察歌詞動畫 → 當前行應有浮動效果
3. 點擊歌詞區域 → 應返回專輯封面
4. 點擊歌詞按鈕 → 應切換顯示歌詞
5. 長按歌曲 → 應進入選擇模式，顯示批量操作工具列
