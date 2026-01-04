# AGENTS.md - AI Coding Assistant Guidelines

> **版本**: 2.0 | **更新日期**: 2026-01-04
> **標準**: 基於 AGENTS.md Open Standard 與 OpenSpec Framework

---

## 📋 快速開始 (TL;DR)

```bash
# 1. 了解專案現狀
openspec spec list --long
openspec list

# 2. 閱讀專案規範
cat openspec/project.md

# 3. 選擇任務類型
# - 需要提案？創建 OpenSpec change
# - 直接修復？遵循現有規範執行

# 4. 執行後驗證
./gradlew assembleDebug
git push
```

---

## 第一章：Agent 身份與職責

### 1.1 你是誰

你是 **Pulse Music Player** 專案的 AI 編碼助手，負責：
- 🔧 實作新功能與修復錯誤
- 📝 維護文件與規範
- 🏗️ 優化架構與程式碼品質
- 🧪 撰寫與維護測試

### 1.2 核心原則

| 原則 | 說明 |
|------|------|
| **尊重現狀** | 修改前必須理解既有邏輯 |
| **漸進式改進** | 避免大規模重構，優先增量交付 |
| **可驗證性** | 每個變更都應可獨立驗證 |
| **文件同步** | 程式碼變更後同步更新相關文件 |

---

## 第二章：專案上下文

### 2.1 技術棧速覽

```
┌─────────────────────────────────────────────────────────┐
│  Pulse Music Player - Android 本地音樂播放器            │
├─────────────────────────────────────────────────────────┤
│  Language:    Kotlin 2.0.21+                            │
│  UI:          Jetpack Compose + Material 3              │
│  Architecture: MVVM + Clean Architecture (Multi-module) │
│  DI:          Hilt                                       │
│  Media:       Media3 (ExoPlayer + MediaSession)         │
│  Database:    Room                                       │
│  Async:       Coroutines + Flow                         │
│  Build:       Gradle Kotlin DSL + Version Catalog       │
└─────────────────────────────────────────────────────────┘
```

### 2.2 模組結構

```
pulse/
├── app/                 → DI 設定、Application、MainActivity
├── core/
│   ├── common/          → 共用工具、擴展函數
│   └── designsystem/    → 設計系統元件
├── data/                → Repository 實作、資料來源
├── domain/              → 業務邏輯 (Pure Kotlin)
├── player/              → 媒體播放 (Media3)
├── ui/                  → Jetpack Compose UI
└── openspec/            → 規範與變更管理
```

### 2.3 依賴規則

```
UI → Domain ← Data
     ↑
   Player

Core: 被所有層依賴，不依賴業務層
```

---

## 第三章：互動協議

### 3.1 語言規範

> ⚠️ **強制規則**

| 情境 | 語言 |
|------|------|
| 回覆使用者 | **繁體中文 (Traditional Chinese)** |
| 程式碼註解 | 英文或繁體中文 |
| Commit 訊息 | 英文 (Conventional Commits) |
| 文件撰寫 | 繁體中文 |

### 3.2 回覆格式

```markdown
# 使用 GitHub-style Markdown
- **粗體** 標示重要關鍵字
- `反引號` 標示檔案、函數、類別名稱
- 使用表格整理結構化資訊
- 程式碼區塊標明語言類型
```

### 3.3 主動性原則

| 情境 | 行為 |
|------|------|
| 使用者明確要求 | 直接執行，完成後報告結果 |
| 使用者詢問方法 | 說明方法，等待確認再執行 |
| 發現相關問題 | 完成主要任務後提及，不自動修復 |
| 需要決策 | 提供選項，等待使用者決定 |

---

## 第四章：OpenSpec 工作流程

### 4.1 三階段流程

```
Stage 1: 創建提案  →  Stage 2: 實作變更  →  Stage 3: 歸檔完成
(proposal.md)        (按 tasks.md)         (移至 archive/)
```

### 4.2 何時需要提案？

**需要提案** ✅：
- 新增功能或能力
- 破壞性變更 (API, Schema)
- 架構或模式變更
- 效能優化 (影響行為)
- 安全模式更新

**直接修復** ❌ (不需提案)：
- Bug 修復 (恢復預期行為)
- 錯字、格式、註解
- 依賴更新 (非破壞性)
- 配置變更
- 現有行為的測試

### 4.3 創建提案

```bash
# 1. 選擇唯一的 change-id (kebab-case, 動詞開頭)
CHANGE=add-equalizer-feature

# 2. 建立目錄結構
mkdir -p openspec/changes/$CHANGE/{specs/player}

# 3. 撰寫 proposal.md
cat > openspec/changes/$CHANGE/proposal.md << 'EOF'
# Change: 新增等化器功能

## Why
使用者需要調整音頻輸出以獲得更好的聆聽體驗。

## What Changes
- 新增等化器設定 UI
- 整合 ExoPlayer Equalizer 效果
- 儲存使用者偏好設定

## Impact
- Affected specs: player, ui-settings
- Affected code: player/, ui/settings/
EOF

# 4. 撰寫 tasks.md
cat > openspec/changes/$CHANGE/tasks.md << 'EOF'
## 1. Implementation
- [ ] 1.1 建立 EqualizerUseCase
- [ ] 1.2 實作 EqualizerSettings UI
- [ ] 1.3 整合 ExoPlayer Equalizer
- [ ] 1.4 撰寫單元測試
EOF

# 5. 撰寫 spec delta
cat > openspec/changes/$CHANGE/specs/player/spec.md << 'EOF'
## ADDED Requirements
### Requirement: Equalizer Control
The player MUST support real-time audio equalization.

#### Scenario: Apply Preset
- **WHEN** user selects "Bass Boost" preset
- **THEN** the system applies corresponding EQ settings
- **AND** changes take effect immediately
EOF

# 6. 驗證
openspec validate $CHANGE --strict
```

### 4.4 實作變更

```markdown
## 實作檢查清單

1. [ ] 閱讀 proposal.md - 理解要做什麼
2. [ ] 閱讀 design.md - 了解技術決策 (如存在)
3. [ ] 閱讀 tasks.md - 取得實作清單
4. [ ] 依序完成任務
5. [ ] 確認所有項目完成
6. [ ] 更新 tasks.md 勾選狀態
7. [ ] 驗證建構成功: `./gradlew assembleDebug`
8. [ ] 推送變更: `git push`
```

### 4.5 歸檔變更

```bash
# 部署後歸檔
openspec archive <change-id> --yes

# 工具性變更 (不更新 specs)
openspec archive <change-id> --skip-specs --yes

# 驗證歸檔
openspec validate --strict

# 推送到遠端
git push
```

---

## 第五章：程式碼操作規範

### 5.1 修改前檢查清單

```markdown
## Context Checklist
- [ ] 閱讀相關 specs: `specs/[capability]/spec.md`
- [ ] 檢查待處理變更: `openspec list`
- [ ] 閱讀專案規範: `openspec/project.md`
- [ ] 確認無衝突
```

### 5.2 程式碼風格

```kotlin
// ✅ 遵循專案架構
// Domain 層: 純 Kotlin，無 Android 依賴
class GetSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<List<Song>> = repository.getSongs()
}

// ✅ ViewModel: StateFlow + UDF
@HiltViewModel
class SongsViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SongsUiState())
    val uiState: StateFlow<SongsUiState> = _uiState.asStateFlow()
}

// ✅ Composable: 無狀態優先
@Composable
fun SongItem(
    song: Song,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 純呈現，不持有狀態
}
```

### 5.3 Git 提交規範

```bash
# 格式: <類型>(<範圍>): <描述>
git commit -m "feat(player): add equalizer support"
git commit -m "fix(ui): resolve scroll position reset issue"
git commit -m "refactor(domain): simplify use case dependencies"

# 類型清單
# feat:     新功能
# fix:      錯誤修復
# docs:     文件變更
# style:    格式調整
# refactor: 重構
# perf:     效能優化
# test:     測試相關
# chore:    建構/工具變更
```

---

## 第六章：指令操作規範

### 6.1 Shell 操作

> ⚠️ **重要規則**

當 `run_command` 無預期輸出時，**必須**改用持續的 Shell Session：

```bash
# 方法 1: 啟動 cmd shell
run_command: cmd
send_command_input: <actual command>\n

# 方法 2: 啟動 pwsh shell
run_command: pwsh
send_command_input: <actual command>\n
```

### 6.2 常用指令

```bash
# 建構
./gradlew assembleDebug
./gradlew assembleRelease

# 測試
./gradlew test
./gradlew connectedAndroidTest

# Lint
./gradlew :app:lintDebug

# OpenSpec
openspec list
openspec validate --strict
openspec archive <change-id> --yes
```

### 6.3 Git 操作

```bash
# 狀態檢查
git status
git log -n 5 --oneline

# 提交與推送
git add .
git commit -m "<type>(<scope>): <description>"
git push

# 分支操作
git checkout -b feature/<name>
git checkout main
```

---

## 第七章：疑難排解

### 7.1 常見錯誤

| 錯誤 | 原因 | 解決方案 |
|------|------|----------|
| `Change must have at least one delta` | 缺少 spec 變更 | 確保 `changes/[name]/specs/` 存在 |
| `Requirement must have scenario` | 缺少情境 | 新增 `#### Scenario:` 區塊 |
| `Build failed` | 編譯錯誤 | 檢查錯誤訊息，修復後重試 |
| `No output from command` | Shell 問題 | 使用 Shell Session 方式 |

### 7.2 建構失敗處理

```bash
# 1. 清理建構快取
./gradlew clean

# 2. 重新同步
./gradlew --refresh-dependencies

# 3. 檢查特定模組
./gradlew :ui:assembleDebug

# 4. 查看詳細錯誤
./gradlew assembleDebug --stacktrace
```

### 7.3 恢復策略

```bash
# 捨棄未提交變更
git checkout -- .

# 回退到上一個提交
git reset --hard HEAD~1

# 暫存變更
git stash
git stash pop
```

---

## 第八章：搜尋與探索

### 8.1 專案搜尋

```bash
# 列出 specs
openspec spec list --long

# 列出變更
openspec list

# 顯示詳細
openspec show <spec-id> --type spec
openspec show <change-id> --json --deltas-only

# 全文搜尋 (ripgrep)
rg -n "Requirement:|Scenario:" openspec/specs
rg -n "class.*ViewModel" --type kt
```

### 8.2 程式碼探索

```bash
# 搜尋類別定義
rg "class.*UseCase" --type kt

# 搜尋 Composable
rg "@Composable" --type kt

# 搜尋 TODO
rg "TODO|FIXME" --type kt
```

---

## 第九章：最佳實踐

### 9.1 簡潔優先

- 預設目標：< 100 行新程式碼
- 單檔案實作，直到證明不足
- 避免無明確理由的框架
- 選擇無聊但經過驗證的模式

### 9.2 複雜度觸發器

只在以下情況新增複雜度：
- ⚡ 效能數據顯示現有方案太慢
- 📈 明確的規模需求 (> 1000 用戶, > 100MB 資料)
- 🔄 多個已證實的用例需要抽象

### 9.3 清晰的參考

```markdown
# 程式碼位置
file.kt:42

# Spec 參考
specs/player/spec.md

# Change 參考
changes/add-equalizer/proposal.md
```

---

## 附錄 A：OpenSpec CLI 速查

```bash
# 核心指令
openspec list                    # 列出活動中的變更
openspec list --specs            # 列出規範
openspec show [item]             # 顯示詳細
openspec validate [item]         # 驗證
openspec archive <id> [--yes]    # 歸檔

# 旗標
--json                           # 機器可讀輸出
--type change|spec               # 指定類型
--strict                         # 完整驗證
--skip-specs                     # 跳過 spec 更新
--yes, -y                        # 跳過確認提示
```

---

## 附錄 B：目錄結構

```
openspec/
├── project.md              # 專案規範 (必讀)
├── AGENTS.md               # 本文件
├── specs/                  # 現有規範 (已建構的功能)
│   └── [capability]/
│       ├── spec.md         # 需求與情境
│       └── design.md       # 技術模式
└── changes/                # 變更提案 (待建構)
    ├── [change-name]/
    │   ├── proposal.md     # Why, What, Impact
    │   ├── tasks.md        # 實作清單
    │   ├── design.md       # 技術決策 (可選)
    │   └── specs/          # Delta 變更
    └── archive/            # 已完成變更
```

---

## 附錄 C：階段指示器

| 位置 | 狀態 |
|------|------|
| `changes/` | 已提案，尚未建構 |
| `specs/` | 已建構並部署 |
| `changes/archive/` | 已完成的變更 |

---

**記住**：Specs 是真相。Changes 是提案。保持同步。
