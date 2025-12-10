# Gemini CLI 核心行為準則 (Core Guidelines)

本檔案定義了 Gemini CLI 的核心操作規範、架構偏好與技術最佳實踐。此準則適用於所有 Android/Kotlin 專案開發，旨在確保代碼品質、一致性與使用者體驗。

---

### **第一章：互動協議 (Interaction Protocol)**

*   **溝通語言**: 預設使用 **繁體中文 (Traditional Chinese)**。
*   **自主執行 (Autonomous Execution)**: 對於多步驟任務，應自動連續執行，僅在遇到致命錯誤或需人工決策時暫停。
*   **Shell 會話管理 (Shell Session Management)**: 
    *   **Rule**: 嚴禁使用單次 `run_command` 執行連續指令。
    *   **Action**: 必須使用 `run_command` 啟動一個持續的 Shell Session，並透過 `send_command_input` 發送後續指令。這能確保環境變數（如 `export`）與工作目錄上下文被正確保留。

---

### **第二章：工作方法論 (Working Methodology)**

#### **§1 代碼完整性 (Code Integrity)**
*   **尊重現狀**: 修改前必須理解既有邏輯。
*   **優先順序**: **可讀性** > **維護性** > **效能**。
*   **原子化提交 (Atomic Commits)**: 
    *   將變更分解為最小邏輯單元。
    *   Commit Message 遵循 Conventional Commits (e.g., `feat:`, `fix:`, `chore:`).

#### **§2 增量交付 (Incremental Delivery)**
*   避免一次性提交數百行的巨大變更。
*   每個階段性成果（如：完成 UI 佈局、完成資料層連接）都應可被獨立驗證。

---

### **第三章：通用架構規範 (Universal Architecture Standards)**

本規範基於 **Clean Architecture** 與 **Modularization** 原則。

#### **§1 分層職責 (Layer Responsibilities)**
1.  **Domain Layer** (Kotlin Rules):
    *   **純粹性**: 嚴禁依賴 Android Framework。
    *   **職責**: 定義核心業務規則 (UseCases) 與抽象介面 (Repository Interfaces)。
2.  **Data Layer** (implementation):
    *   **職責**: 提供資料源實作 (API, DB, Preferences)。
    *   **封裝**: 對外隱藏具體資料來源細節，僅暴露 Domain 模型。
3.  **UI Layer** (Interaction):
    *   **職責**: 狀態呈現與使用者互動。
    *   **限制**: **嚴禁**直接依賴 Data Layer。必須透過 UseCases 與 Domain 互動。

#### **§2 依賴與數據流 (Dependency & Data Flow)**
*   **Dependency Rule**: `UI -> Domain <- Data`
*   **Data Flow**: `UI (Event) -> Domain -> Data` ... `Data (Stream/Flow) -> Domain -> UI (State)`

---

### **第四章：技術最佳實踐 (Technical Excellence)**

本章節總結了跨專案通用的技術陷阱與解決方案，**必須**在設計階段納入考量。

#### **§1 狀態韌性 (State Resilience)**
*   **Process Death 防護**: 所有關鍵 UI 狀態（尤其是 **Bottom Sheets**, **Expanded Views**, **Complex Navigation State**）**必須**使用 `rememberSaveable` 而非 `remember`。確保應用在後台被系統回收後，用戶返回時能看到一致的畫面。
*   **動態佈局適配**: 對於依賴容器尺寸計算的狀態（如 `AnchoredDraggableState` 的 anchors），應在 `LaunchedEffect` 或 `onSizeChanged` 中更新配置，**嚴禁**因尺寸變化而重新創建 (Re-create) 狀態物件，這會導致互動中斷或狀態重置。

#### **§2 極致使用者體驗 (UX Excellence)**
*   **智慧列表定位 (Smart List Positioning)**: 
    *   當進入一個長列表且有明確「活躍項目」時，應自動滾動至該項目 (Auto-scroll to active item)。
*   **容錯互動設計 (Forgiving Interactions)**:
    *   **手勢導航**: 自定義滑動控件（如側邊索引欄）應具備「吸附」或「智慧查找」功能。若用戶手指滑到無效區域，應自動定位至最近的有效內容，而非無反應。
    *   **視覺反饋**: 任何拖動、長按操作都必須提供即時的視覺提示（如氣泡、高亮、震動回饋）。
*   **誠實 UI (Honest UI)**: 
    *   若某 UI 元素看似可互動（如 Drag Handle），則必須具備相應功能。若功能未實作，應隱藏該元素或替換為資訊性組件（如 Metadata 標籤），避免欺騙用戶預期。

#### **§3 系統兼容性 (System Compatibility)**
*   **權限策略 (Permission Strategy)**: 
    *   針對 Android 13+ (API 33+) 的 `POST_NOTIFICATIONS` 與細分媒體權限 (`READ_MEDIA_AUDIO` 等)，**必須**在相關功能啟動前 (如 `MainActivity.onCreate` 或播放前) 進行檢查與請求。
    *   永遠不要假設權限已被授予。

---
---

## Gemini Added Memories
- User prefers Traditional Chinese (繁體中文) for interaction.
