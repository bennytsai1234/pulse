# Tasks: Fix Fullscreen Player UI Issues (v2.1.1)

## 字母索引修復
- [x] 調整 `FastScroller` 的 padding 分配，確保 Z 和 # 不被遮擋 <!-- id: fix-fastscroller-padding -->
- [x] 優化字母垂直間距，在有限空間內容納全部 27 個字符 <!-- id: optimize-alphabet-spacing -->
- [x] 確保字母最小觸控目標符合 Material 3 規範（44dp） <!-- id: verify-touch-targets -->

## DiscoverScreen 返回導航
- [x] 為 `DiscoverScreen` 添加 `onBackClick` 參數 <!-- id: add-discover-back-param -->
- [x] 修改 TopBar 以包含返回按鈕，同時保留大標題效果 <!-- id: modify-discover-topbar -->
- [x] 更新導航圖調用處傳入返回回調 <!-- id: update-navigation-callback -->

## TopBar 高度一致性
- [x] 檢查 `PulseTopBar` 與 `PulseTopBarWithBack` 的高度是否一致 <!-- id: check-topbar-heights -->
- [x] 若有差異，統一使用 M3 標準高度 <!-- id: unify-topbar-height -->
- [x] 驗證所有子頁面返回按鈕與首頁選單按鈕垂直對齊 <!-- id: verify-button-alignment -->

## 驗證與測試
- [x] 編譯測試通過 <!-- id: test-fastscroller-sizes -->
- [x] 測試 DiscoverScreen 返回流程 <!-- id: test-discover-navigation -->
- [x] 對比截圖驗證導航按鈕對齊 <!-- id: compare-navigation-alignment -->

## 版本升級
- [x] 更新 `versionName` 至 "2.1.1" <!-- id: update-version-name -->
- [x] 更新 `versionCode` 至 17 <!-- id: update-version-code -->
- [x] 更新 RELEASE_NOTES.md <!-- id: update-release-notes -->
