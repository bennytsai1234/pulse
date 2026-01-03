# 任務列表：清理與發布

1. [x] 刪除遺留的構建檔案 `GeminiMusic_v1.4.0.apk` <!-- id: del_apk_140 -->
2. [x] 刪除遺留的構建檔案 `GeminiMusic_v1.4.1.apk` <!-- id: del_apk_141 -->
3. [x] 刪除暫存檔案 `RELEASE_NOTES_v1.4.1.md` <!-- id: del_notes_141 -->
4. [x] 刪除暫存檔案 `RELEASE_NOTES_new.md` <!-- id: del_notes_new -->
5. [x] 在 `app/build.gradle.kts` 中將 `versionName` 升級為 "2.0.1"，`versionCode` 升級為 15 <!-- id: bump_version -->
6. [x] 在 `RELEASE_NOTES.md` 中新增 v2.0.1 的發布資訊 <!-- id: update_notes -->
7. [x] 建構 Signed Release APK (`./gradlew assembleRelease`) <!-- id: build_release -->
