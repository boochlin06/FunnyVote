# FunnyVote 趣投票 🗳️ (Kotlin & Compose 現代化首發版)

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin" alt="Kotlin Version" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=android" alt="Compose Version" /></a>
  <a href="https://developer.android.com/topic/libraries/architecture/viewmodel"><img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20StateFlow-blue.svg?style=flat" alt="Architecture" /></a>
  <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Room-2.6.1-green.svg?style=flat" alt="Room DB" /></a>
  <a href="https://dagger.dev/hilt/"><img src="https://img.shields.io/badge/Hilt-2.51.1-green.svg?style=flat" alt="Hilt Version" /></a>
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License" /></a>
</p>

---

## 📖 基本資料 (Basic Info)

* **專案定位**：從舊版 Java 跨越至現代 Kotlin 技術棧的首發重構專案。
* **分支角色 (`kotlin-rewrite`)**：**語言與 UI 宣告式現代化里程碑分支**。將 2016 時代的 Java 7/8、肥大 XML 佈局與命令式 View 樹，全盤重塑為簡潔、型別安全且高度可讀的現代代碼庫。
* **核心解決痛點**：
  * **Null 安全性與樣板代碼**：透過 Kotlin 空安全系統消除 `NullPointerException`，以 Data Class 減少 70% Java POJO 樣板。
  * **宣告式取代 XML**：徹底淘汰 `setContentView()`、`findViewById()` 與 ButterKnife，啟用 Jetpack Compose 聲明式元件。
  * **異步回呼解耦**：使用 `Kotlin Coroutines` 與 `Flow` 徹底終結 Retrofit 舊版多層 Callback Hell。
  * **架構分層規範**：從昔日 Activity 包辦一切的肥大代碼，轉移為具備標準生命週期管理的 MVVM 架構。

---

## 🚀 技術亮點與規格矩陣 (Technical Highlights)

| 組件層級 | 採納技術 / 規格 | 詳細設計與優勢 |
| :--- | :--- | :--- |
| **開發語言** | Kotlin 2.0.21 | 啟用空安全、擴充函數 (Extension Functions) 與標準庫高階函數 |
| **UI 宣告層** | Jetpack Compose (Material 3) | 聲明式單元組合，搭配 Navigation Compose 管理跨頁面路由 |
| **架構模式** | MVVM + StateFlow | `ViewModel` 封裝業務資料，透過響應式 `StateFlow` 對外暴露狀態 |
| **本地快取** | AndroidX Room 2.6.1 | 淘汰舊版 GreenDAO / ActiveAndroid，支援安全編譯期 SQL 語法驗證 |
| **網路通訊** | Retrofit 2.9.0 + Coroutines | 原生支援掛起函數 (`suspend fun`)，免除執行緒切換維護心智負擔 |
| **依賴注入** | Google Hilt 2.51.1 | 簡化依賴關係，為資料來源與 ViewModel 提供標準生命週期範圍注入 |

---

## 🏗️ 系統架構與設計模式 (Architecture & Design Patterns)

本分支實作標準的 **MVVM (Model-View-ViewModel)** 架構：

```mermaid
flowchart TD
    subgraph UI_Layer ["UI Layer (Jetpack Compose)"]
        View["Compose Screens\n(HomeScreen, VoteDetailScreen)"]
    end

    subgraph ViewModel_Layer ["ViewModel Layer"]
        VM["Android ViewModel\n(HomeViewModel)"]
        State["StateFlow&lt;HomeUiState&gt;"]
    end

    subgraph Data_Layer ["Data Layer (Repository Pattern)"]
        Repo["VoteRepository (SSOT)"]
        Local["Room Database (AppDatabase)"]
        Remote["Retrofit Remote API (VoteApiService)"]
    end

    View -->|觸發事件| VM
    VM -->|請求資料| Repo
    Repo -->|優先查詢本地| Local
    Repo -->|遠端更新同步| Remote
    Remote -->|寫入快取| Local
    Local -->|響應式串流| Repo
    Repo -->|資料流| VM
    VM -->|更新| State
    State -->|重組渲染| View
```

---

## 💡 當年時空背景與工程師決策復盤 (Retrospective)

### 為什麼在 2024 年啟動全面重構？
時隔多年，Android 生態早已發生翻天覆地的演進：Jetpack Compose 成為 UI 新標準、Coroutines/Flow 成為非同步霸主、Room 與 Hilt 統一了資料庫與依賴注入。面對過去歷史分支中留下的 GreenDAO、ButterKnife 與複雜 MVP，重寫比修補更具工程價值。

### 當年解決之突破：
1. **宣告式 UI (Compose) 初體驗**：徹底告別繁瑣的 XML 佈局與 `findViewById`，元件代碼量縮減 60% 以上。
2. **協程 (Coroutines) 終結回呼地獄**：`suspend fun` 與 `viewModelScope` 讓非同步代碼宛如同步編程般直觀。
3. **Room + Hilt 取代 GreenDAO + Dagger 2**：編譯期檢查與官方標準架構讓工程維護成本驟降。

### 當年留下的工程遺憾（後續演進動機）：
1. **傳統 MVVM 的狀態撕裂 (State Inconsistency)**：ViewModel 內部開出多個獨立的 `StateFlow`（如 `loadingState`, `dataState`, `errorState`），在複雜並發下容易發生狀態不同步，催生了 `mvi-rewrite` 的嚴格單向資料流 (UDF)。
2. **頁面覆蓋度尚未完備**：此階段專注於核心主鏈路（首頁與投票詳情），原版的自訂分享彈窗、四個關於子頁面與個人頁面尚未全面復刻（留待 `modern-android` 全面補齊）。

---

## 🌿 各分支演進地圖 (Branch Evolutionary Roadmap)

```text
[main] ───────────────► 2016 經典 Java / ButterKnife / EventBus / SQLite
   │
   ├─► [mvp] ──────────► 2017 初次解耦：導入 Google MVP Blueprint、Contract 契約設計
   │
   ├─► [mvp_rxjava] ────► 2017 響應式進化：引入 RxJava 切換執行緒、統一數據串流管線
   │
   ├─► [mvp_dagger] ────► 2017 依賴注入：引入 Dagger 2，實現編譯期依賴拓撲圖
   │
   ├─► [mvp_kotlin] ────► 2018 初探 Kotlin：Java 全盤轉 Kotlin 1.2、消滅 NPE
   │
   ├─► [kotlin-rewrite] (★ Current)
   │                       └─► 2024 現代轉型：Kotlin 2.0 + Coroutines + 基礎 Compose、
   │                           確立 MVVM + Room + Hilt 現代工程基礎
   │
   ├─► [mvi-rewrite] ────► 2024 架構規範：嚴格 MVI (UDF) 單向資料流
   │
   ├─► [modern-android] ─► 2026 現代完備：Compose 100% 畫面補全、Room 快取、Hilt 注入
   │
   └─► [feature/firebase-backend]
                           └─► 2026 雲原生旗艦版：Firebase Serverless、Cloud Firestore、
                               離線持久化、實體 Android 16 真機驗收
```

---

## 📦 快速上手與運行指引 (How to Build & Run)

```bash
# 1. 進入 Android 專案目錄
cd funnyvote

# 2. 執行單元測試
./gradlew testDebugUnitTest

# 3. 編譯 Debug APK
./gradlew assembleDebug
```
