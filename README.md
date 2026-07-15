<h1 align="center">FunnyVote</h1>

## 📖 基本資料 (Basic Info)
*   **目的與解決痛點**：這是一個架構較為複雜的進階版投票系統。有別於 EasyVote，它解決了「網路延遲」、「多層級非同步請求」以及「離線資料快取」等痛點。讓使用者在網路不穩定的環境下依然能順暢瀏覽投票選項。
*   **專案定位**：進階實用工具類 APP (Advanced Utility Application)。

## 🚀 技術亮點 (Modernized Tech Stack)
*   **Jetpack Compose**：全面導入宣告式 UI 架構，捨棄傳統 XML Layout，並以單一 Activity (`Single-Activity Architecture`) 搭配 `Navigation Compose` 來管理畫面路由。
*   **Room Database**：取代了原本的 GreenDAO。以更安全的型別檢查及更好的 Coroutines 整合，負責本地資料庫快取功能。
*   **Retrofit & Coroutines**：API 層全面升級，使用 `Kotlin Coroutines` 與 `Retrofit` 取代舊有的 Callback 寫法，讓非同步請求代碼更加簡潔易讀。
*   **Hilt (Dependency Injection)**：引進 Google 官方推薦的 Dagger Hilt 進行依賴注入，解除物件間的耦合，降低維護成本。

## 🏗️ 架構與 Design Pattern
*   **MVVM & StateFlow**：專案從原先的 EventBus 事件驅動架構，重構為標準的 MVVM 架構。使用 `ViewModel` 與 `StateFlow` 管理畫面狀態 (`UiState`)，達成單向資料流 (UDF, Unidirectional Data Flow)。
*   **Repository Pattern (儲存庫模式)**：利用 Repository 整合本地端 (Room) 與遠端 (Retrofit) 的資料來源，並提供單一資料來源 (SSOT) 給 ViewModel。

## 🌿 各分支目的 (Branches Overview)
*   **`master`**: 原始基底分支，主要使用傳統 Activity/EventBus 架構。
*   **`mvp`**, **`mvp_dagger`**, **`mvp_kotlin`**, **`mvp_rxjava`**: 原作者過去嘗試重構的各種架構實驗分支。
*   **`kotlin-rewrite`** (Current): 最新且最激進的全面現代化重構分支。我們直接跳過了 MVP、傳統 Dagger 甚至 RxJava，採用了目前 Google 官方最推薦的 **MVVM 搭配 Jetpack Compose (UI) + Coroutines/StateFlow (響應式) + Hilt (DI) + Room**，實現了架構的終極升級！

## 📦 How to Use (快速上手)
```bash
# 使用 Android Studio (Hedgehog 或更新版本) 匯入專案
./gradlew assembleDebug
```

## 🎯 面試與推銷指南 (Interview & Pitch Guide)
如果您即將在面試中展示此專案，請務必閱讀這份專屬的教戰手冊：
👉 **[點此查看面試推銷攻略 (interview.md)](./interview.md)**

