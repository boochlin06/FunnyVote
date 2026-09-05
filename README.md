# FunnyVote 趣投票 🗳️ (2017 MVP 架構重構版)

<p align="center">
  <img src="http://vinta.ws/booch/wp-content/uploads/2017/04/g294.png" alt="FunnyVote Classic Logo" width="260" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Java%207%2F8-orange.svg?style=flat" alt="Language" />
  <img src="https://img.shields.io/badge/Architecture-MVP%20(Google%20Blueprint)-red.svg?style=flat" alt="Architecture" />
  <img src="https://img.shields.io/badge/Android%20SDK-API%2025%20(Nougat)-green.svg?style=flat" alt="Android SDK" />
  <img src="https://img.shields.io/badge/Contract-View%20%2F%20Presenter-blue.svg?style=flat" alt="Contract" />
  <img src="https://img.shields.io/badge/View%20Binding-ButterKnife%208.4-blue.svg?style=flat" alt="ButterKnife" />
  <img src="https://img.shields.io/badge/Database-GreenDAO%203.0-yellowgreen.svg?style=flat" alt="GreenDAO" />
  <img src="https://img.shields.io/badge/Networking-Retrofit%202.0-blue.svg?style=flat" alt="Retrofit" />
  <img src="https://img.shields.io/badge/Branch-mvp%20(2017%20First%20Decoupling)-lightgrey.svg?style=flat" alt="Branch" />
</p>

---

## 📖 基本資料 (Basic Info)

* **專案定位**：2017 年為解決舊版「God Activity」肥大症所啟動的第一代架構解耦實驗分支。
* **分支角色 (`mvp`)**：**從 MVC 邁向架構規範化的第一步**。參考當年 Google 官方開源的 `android-architecture (todo-mvp)` 藍圖，將業務邏輯全面自 UI 層抽出。
* **當年解決之核心痛點**：
  * **告別千行上帝類 (God Activity)**：原版 `main` 分支中，Activity 既要綁定 View、手動計算動畫、又要處理 Retrofit Callback 與 SQLite 讀寫。本分支透過 `Presenter` 承接所有業務決策。
  * **單元測試 (Unit Test) 曙光**：Presenter 依賴抽象的 `Contract.View` 介面，業務邏輯終於能夠在純 JVM 環境下透過 Mockito 進行測試，擺脫每次驗證都要跑幾分鐘真機部署的惡夢。

---

## 🚀 技術亮點與規格矩陣 (Technical Highlights)

| 組件層級 | 採納技術 / 規格 | 詳細設計與特徵 |
| :--- | :--- | :--- |
| **核心架構** | Google Blueprint MVP | 定義 `BaseView<T>` 與 `BasePresenter`，藉由 `Contract` 介面嚴格規範交互契約 |
| **視圖契約** | `MainPageContract` 等契約類 | 將 `Presenter` 與 `View` 宣告於同一個介面檔內，雙向職責一目了然 |
| **UI 視圖層** | Fragment + ButterKnife 8.4 | Activity 降級為單純的 Fragment 容器，UI 渲染完全由 Presenter 驅動 |
| **非同步通訊** | Retrofit 2.0 + Callback | 網路回呼在背景執行緒接收，由 Presenter 切回主執行緒並通知 View 更新 |
| **本地資料庫** | GreenDAO 3.0 | 升級至 GreenDAO 註解版本，提供物件導向 SQLite 存取 |
| **跨組件解耦** | EventBus 2.4 / 3.0 | 跨頁面大型事件（如全域登入狀態變更）維持 Pub/Sub 總線通知 |

---

## 🏗️ 系統架構與設計模式 (Architecture & Design Patterns)

本分支嚴格採用 **Contract-based Model-View-Presenter** 架構：

```mermaid
flowchart LR
    subgraph View_Layer ["View Layer (Passive View)"]
        Activity["MainActivity / Fragment\n(implements Contract.View)"]
    end

    subgraph Presenter_Layer ["Presenter Layer (Pure Logic)"]
        Presenter["MainPagePresenter\n(implements Contract.Presenter)"]
    end

    subgraph Model_Layer ["Model Layer (Data & Cache)"]
        Repo["Data Manager / GreenDAO\n& Retrofit 2.0 API"]
    end

    Activity -->|1. 使用者觸發動作\n(如 reloadHotList)| Presenter
    Presenter -->|2. 調用資料接口| Repo
    Repo -->|3. 非同步回傳資料實體| Presenter
    Presenter -->|4. 調用 View 介面渲染\n(如 renderHotVotes)| Activity
```

### 經典 Contract 契約類設計範例
```java
public interface MainPageContract {
    interface View extends BaseView<Presenter> {
        void showLoadingProgress();
        void hideLoadingProgress();
        void renderHotVotes(List<VoteData> votes);
        void showNetworkError(String message);
    }

    interface Presenter extends BasePresenter {
        void reloadHotList(int offset);
        void favoriteVote(VoteData voteData);
        void pollVote(VoteData voteData, String optionCode, String password);
    }
}
```

---

## 💡 當年時空背景與工程師決策復盤 (Retrospective)

### 為什麼在 2017 年選擇重構為 MVP？
在 2016~2017 年間，Android 社群正經歷一場「架構覺醒」。Google 官方在 GitHub 開源了著名的 `android-architecture` 專案，正式推薦 MVP 作為官方最佳實踐。面對舊版 `MainActivity.java` 超過千行、邏輯如義大利麵般纏繞的慘狀，將 UI 與邏輯徹底切開成了唯一救贖。

### 當年留下的工程遺憾與痛點：
1. **介面代碼爆炸 (Interface Explosion)**：
   每個小功能頁面都要手寫 `Contract`、`View`、`Presenter`、`PresenterImpl` 四件套，代碼量暴增 2~3 倍，改個按鈕要動好幾個檔案。
2. **記憶體洩漏與生命週期殭屍 (Memory Leaks & NPE)**：
   Presenter 必須持有 View 的參考。當網路請求還在走，而使用者突然按返回鍵或旋轉螢幕時，Presenter 若未及時呼叫 `detachView()`，會導致整顆 Activity 洩漏；若呼叫了，非同步回呼又會引發 `NullPointerException: view is null`。
3. **無依賴注入的繁瑣實例化**：
   在還沒有導入 Dagger 的情況下，Presenter 必須在 Activity 的 `onCreate` 中手動 `new MainPagePresenter(this, dataManager)`，導致 Presenter 與具體資料類依舊存在隱形耦合。

---

## 📁 2017 MVP 模組目錄結構 (Package Structure)

```text
funnyvote/app/src/main/java/com/heaton/funnyvote/
├── BaseActivity.java                       # Activity 抽象基類
├── BaseFragment.java                       # Fragment 抽象基類
├── BaseView.java                           # 基礎 View 契約介面
├── BasePresenter.java                      # 基礎 Presenter 契約介面 (定義 start())
├── ui/
│   ├── main/                               # 首頁核心 MVP 模組
│   │   ├── MainPageContract.java           # 首頁 View/Presenter 契約
│   │   ├── MainPagePresenter.java          # 首頁業務決策邏輯
│   │   └── MainPageFragment.java           # 被動視圖 (Passive View)
│   ├── votedetail/                         # 投票詳情 MVP 模組
│   ├── createvote/                         # 發起投票 MVP 模組
│   └── personal/                           # 個人中心 MVP 模組
├── data/                                   # 資料快照與實體類
├── database/                               # GreenDAO 生成之代碼與 SQLite 管理者
└── retrofit/                               # Retrofit RESTful 網路接口
```

---

## 🌿 各分支演進地圖 (Branch Evolutionary Roadmap)

```text
[main] ───────────────► 2016 經典 Java / ButterKnife / EventBus / SQLite
   │
   ├─► [mvp] (★ Current) ─► 2017 初次解耦：導入 Google MVP Blueprint、Contract 契約設計
   │
   ├─► [mvp_rxjava] ────► 2017 響應式進化：引入 RxJava 切換執行緒、淘汰 EventBus
   │
   ├─► [mvp_dagger] ────► 2017 依賴注入：引入 Dagger 2，自動化組裝 Presenter
   │
   ├─► [mvp_kotlin] ────► 2017 初探 Kotlin：Java 轉 Kotlin 第一波語法實驗
   │
   ├─► [kotlin-rewrite] ──► 2024 現代轉型：Kotlin 2.0 + Coroutines + 基礎 Compose
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

## 📦 建置與運行備註 (Build Notes)

> ⚠️ **歷史環境提示**：
> 本分支建置於 Android Studio 2.3 ~ 3.0 與 Gradle 3.x 時代。代碼完整封存 2017 年 MVP 的歷史樣貌，供架構愛好者與後續演進對照研讀。
