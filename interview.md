# 🎯 FunnyVote 現代化架構面試攻略 (Kotlin Rewrite)

> 這是一份為您量身打造的**「終極版」**面試實戰指南。此專案已經過徹底的現代化重構，是您向面試官展示「跟隨 Google 官方最前沿技術」的最佳名片！

## 🗣️ 面試開場白 (Elevator Pitch)
「這是我為了解決傳統 Android 開發痛點，而進行徹底現代化重構的進階投票系統。我捨棄了老舊的 XML 與 EventBus，全面導入了 Google 官方推薦的 **Jetpack Compose、MVVM 搭配 StateFlow 的單向資料流 (UDF)、Hilt 依賴注入以及 Room 本地資料庫**。它不僅是一個 App，更是我對現代化架構實踐的火力展示。」

## 💡 核心亮點與硬核技術 (Core Highlights)
*   **單向資料流 (UDF) 實作**：精準使用 `StateFlow` 將 ViewModel 狀態推送至 Compose UI，徹底根絕 Callback 地獄與不一致的 UI 狀態。
*   **Single-Source-of-Truth (SSOT)**：在 Repository 層結合 Room (快取) 與 Retrofit (遠端)，實作 Offline-first 體驗，網路不穩依然秒開畫面。
*   **Preview-Driven Development (PDD)**：透過 State Hoisting 將 UI 元件拆分為 Stateless，讓 Android Studio 的 `@Preview` 能完美獨立預覽所有邊界狀態 (Loading/Success/Error)，極大化 UI 開發效率。
*   **Test-Driven Development (TDD)**：利用 `kotlinx-coroutines-test` 與 `mockk` 為 ViewModel 撰寫非同步單元測試，證明架構的高可測試性。

## ⚔️ 刁鑽問題攻防戰 (Q&A Defense)

1. **Q: 為什麼選擇從 EventBus 重構為 StateFlow？EventBus 有什麼不好？**
   **A:** EventBus 確實能解耦，但在大型專案中會導致「事件來源難以追蹤」與「生命週期崩潰」的問題。我改用 StateFlow，不僅具備型別安全 (Type-safe)，還能完美配合 Compose 的 `collectAsState()`，讓 UI 永遠只反映唯一的 Truth。

2. **Q: 在 Compose 中，你如何避免頻繁 Recomposition 造成的效能瓶頸？**
   **A:** 我會嚴格區分 Stateful 與 Stateless Composable。將會改變的狀態盡量「延遲讀取 (Defer reading state)」，並對集合資料使用 `key` 以優化 LazyColumn。

3. **Q: 非同步測試很常遇到 Flaky (不穩定) 的問題，你怎麼解決？**
   **A:** 在 ViewModel 單元測試中，我注入了 `StandardTestDispatcher` 並利用 `advanceUntilIdle()` 控制虛擬時間 (Virtual Time)。這讓非同步的 Coroutines 測試變得完全同步且 100% 可預期。

4. **Q: Dagger Hilt 確實好用，但它會拖慢編譯時間，你有想過替代方案嗎？**
   **A:** 我會向面試官說明，如果專案龐大到 KAPT/KSP 成為瓶頸，我會考慮使用更輕量級的 Service Locator（如 Koin）或是手動 DI。但在目前中型專案中，Hilt 的編譯開銷完全被其帶來的開發效率所彌補。

## 💰 商業價值與推銷策略 (How to Sell)
**推銷標籤：現代化架構師、擁抱新技術、高質量工程規範**
在面試中，這套組合拳（Compose + UDF + TDD + PDD）能完美證明您具備帶領團隊進行技術轉型的能力。面試官非常喜歡看見候選人具備寫 Test 與善用 Preview 的好習慣，這代表您產出的代碼不僅能跑，還極具維護性與高品質。
