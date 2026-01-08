# Tasks: Enhance Player UX (v2.2.0)

## Up Next (Queue) 介面修復
- [x] 將 QueueScreen TopBar 改為緊湊型 Row 佈局 <!-- id: fix-queue-topbar-layout -->
- [x] 將 Close 圖示改為 ArrowBack 返回箭頭 <!-- id: fix-queue-back-icon -->
- [x] 修復 `Song.toMediaItem()` - 將 duration 存入 extras <!-- id: fix-song-to-mediaitem-duration -->
- [x] 修復 `MediaItem.toSong()` - 從 extras 讀取 duration <!-- id: fix-mediaitem-to-song-duration -->
- [x] 驗證 Queue 中歌曲時間正確顯示 <!-- id: verify-queue-duration-display -->

## 歌詞卡拉OK強化
- [x] 為 LyricLineView 添加 Y 軸浮動動畫 (translationY) <!-- id: add-lyrics-float-animation -->
- [x] 添加當前歌詞行的發光/陰影效果 <!-- id: add-lyrics-glow-effect -->
- [x] 優化字體大小漸變效果 <!-- id: enhance-lyrics-scale-animation -->

## 歌詞互動優化
- [x] 修復 PlayerControls 歌詞按鈕的空 onClick（添加 onLyricsClick 參數） <!-- id: fix-lyrics-button-onclick -->
- [x] 為 KaraokeLyrics 添加 onLyricsClick 回調參數 <!-- id: add-lyrics-click-callback -->
- [x] 在 NowPlayingScreen 傳遞 onLyricsClick 到 PlayerControls <!-- id: pass-lyrics-click-handler -->
- [x] 在 NowPlayingScreen 處理歌詞區域點擊切換回封面 <!-- id: handle-lyrics-click -->
- [x] 驗證 duration 修復後歌詞按時間戳同步 <!-- id: verify-lyrics-sync -->

## 批量操作功能
- [x] 為 PulseSongListItem 添加 onLongClick 參數 <!-- id: add-longclick-param -->
- [x] 實作長按觸發進入選擇模式 <!-- id: implement-longpress-select -->
- [x] 創建 SelectionActionBar 組件 (刪除、添加到播放清單) <!-- id: create-selection-action-bar -->
- [x] 實作批量刪除功能 (含權限處理) <!-- id: implement-batch-delete -->
- [x] 實作批量添加到播放清單功能 <!-- id: implement-batch-add-to-playlist -->
- [x] 添加選中計數器顯示 <!-- id: add-selection-counter -->

## 驗證與測試
- [x] 測試 QueueScreen 導航一致性 <!-- id: test-queue-navigation -->
- [x] 測試歌詞浮動動畫在不同設備上的流暢度 <!-- id: test-lyrics-animation -->
- [x] 測試歌詞點擊和按鈕功能 <!-- id: test-lyrics-toggle -->
- [x] 測試長按進入選擇和批量操作 <!-- id: test-batch-operations -->

## 版本升級
- [x] 更新 versionName 至 "2.2.0" <!-- id: update-version-name -->
- [x] 更新 versionCode <!-- id: update-version-code -->
- [x] 更新 RELEASE_NOTES.md <!-- id: update-release-notes -->
