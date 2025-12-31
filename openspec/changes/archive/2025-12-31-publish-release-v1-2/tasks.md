# Tasks: Publish Release v1.3.0

## 準備工作
- [x] 執行 `git status` 確保工作區乾淨。
- [x] 執行 `./gradlew clean` 準備建置。

## 版本與日誌更新
- [x] 更新 `app/build.gradle.kts`：`versionCode = 8`, `versionName = "1.3.0"`。
- [x] 更新 `RELEASE_NOTES.md`：
    - [x] 新增 v1.3.0 章節。
    - [x] 整理本次迭代的功能亮點（歌詞優化、下滑手勢、Gemini 空狀態）。

## 建置驗證
- [x] 執行 `./gradlew assembleRelease` 生成 APK。
- [x] 確認 `app/build/outputs/apk/release/` 資料夾下已生成簽署的 APK。

## 存檔與標記
- [x] 提交版本變更。
- [x] 執行 `git tag v1.3.0`。
- [x] 推送變更與 Tag 至 GitHub。
- [ ] 存檔 `publish-release-v1-2` 提案。
