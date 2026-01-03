---
description: 完成任務後推送變更到 GitHub
---

# Workflow: Push Changes

當完成任務或一系列相關工作後，執行此工作流程確保變更已同步到 GitHub。

## Steps

// turbo-all

1. **檢查 Git 狀態**
   ```bash
   git status
   ```

2. **暫存所有變更**
   ```bash
   git add -A
   ```

3. **提交變更**（使用適當的 Conventional Commit 訊息）
   ```bash
   git commit -m "<type>: <description>"
   ```

   常用類型：
   - `feat:` - 新功能
   - `fix:` - 修復 bug
   - `chore:` - 雜項維護
   - `refactor:` - 重構
   - `docs:` - 文件更新

4. **推送到遠端**
   ```bash
   git push origin main
   ```

5. **確認推送成功**
   - 檢查輸出確認無錯誤
   - 確認 `main -> main` 訊息

## Reminder

> ⚠️ **重要提醒**：每次完成任務後，務必執行 push 確保：
> - 變更不會遺失
> - 團隊成員可以同步最新進度
> - GitHub 上的程式碼與本地一致
