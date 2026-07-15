<h1 align="center">FunnyVote</h1>

## 📖 基本資料 (Basic Info)
*   **目的與解決痛點**：這是一個架構較為複雜的進階版投票系統。有別於 EasyVote，它解決了「網路延遲」、「多層級非同步請求」以及「離線資料快取」等痛點。讓使用者在網路不穩定的環境下依然能順暢瀏覽投票選項。
*   **專案定位**：進階實用工具類 APP (Advanced Utility Application)。

## 🚀 技術亮點 (Technical Highlights)
*   **GreenDAO 本地快取**：選用了以效能著稱的 `GreenDAO` 關聯式資料庫。將遠端拉取的投票資料映射至本地端，實現了斷網可用性與極快的冷啟動速度。
*   **Retrofit 高效網路層**：拋棄 `HttpURLConnection`，以 `Retrofit 2` 進行 RESTful API 呼叫，大幅提升了連線穩定度與 JSON 解析效率。

## 🏗️ 架構與 Design Pattern
*   **EventBus (發布/訂閱模式)**：這是在 RxJava 尚未完全普及前的頂級架構選擇。專案中的資料管理員 (如 `VoteDataManager`) 在背景執行緒透過 Retrofit 取得資料後，利用 `EventBus.getDefault().post()` 將事件廣播出去。而 UI 層 (Fragment/Activity) 只需透過 `@Subscribe` 標記即可接收更新，完美達成了 View 與 Model 的強制解耦。
*   **Repository Pattern (儲存庫模式)**：雖然未明說，但 `VoteDataManager` 實際上扮演了 Repository 的角色，負責判斷資料該從本地 GreenDAO 還是遠端 API 獲取。

## 🌿 各分支目的 (Branches Overview)
*   本專案為穩定版的 Master 快照 (`FunnyVote-master`)，專注於提供一套完整可運作的投票系統範例。

## 📦 How to Use (快速上手)
```bash
# 使用 Android Studio 匯入專案，編譯前請確認 GreenDAO 生成腳本是否執行成功
./gradlew build
```
