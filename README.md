# FunnyVote 趣投票 🗳️ (2018 MVP + Kotlin 1.2 語言先行重構版)

<p align="center">
  <img src="http://vinta.ws/booch/wp-content/uploads/2017/04/g294.png" alt="FunnyVote Classic Logo" width="260" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Kotlin%201.2.61-purple.svg?style=flat&logo=kotlin" alt="Kotlin 1.2" />
  <img src="https://img.shields.io/badge/Architecture-MVP%20in%20Kotlin-red.svg?style=flat" alt="Architecture" />
  <img src="https://img.shields.io/badge/View%20Binding-Kotlin%20Synthetics-blue.svg?style=flat" alt="Kotlin Synthetics" />
  <img src="https://img.shields.io/badge/Null%20Safety-Strict%20Types-brightgreen.svg?style=flat" alt="Null Safety" />
  <img src="https://img.shields.io/badge/Database-GreenDAO%203.0-yellowgreen.svg?style=flat" alt="GreenDAO" />
  <img src="https://img.shields.io/badge/Networking-Retrofit%202.0-blue.svg?style=flat" alt="Retrofit" />
  <img src="https://img.shields.io/badge/Branch-mvp__kotlin%20(Kotlin%201.2%20Pioneer)-lightgrey.svg?style=flat" alt="Branch" />
</p>

---

## 📖 基本資料 (Basic Info)

* **專案定位**：在 Google I/O 2017 正式宣布 Kotlin 成為 Android 一級開發語言 (First-class Citizen) 之後，FunnyVote 第一個「純 Kotlin 語法現代化」先鋒分支。
* **分支角色 (`mvp_kotlin`)**：**語言革命先行者**。歷經 2016 Java 7/8、2017 MVP 與 RxJava 的探索後，本分支將全專案超過 80 個 Java 類別全數轉寫為 Kotlin，開啟了現代化語言的新紀元。
* **當年解決之核心痛點**：
  * **消滅 NullPointerException (NPE)**：以 Kotlin 編譯期型別系統強制標註可空性 (`String?` vs `String`)，徹底撲滅 Java 時代最常見的線上崩潰問題。
  * **淘汰 ButterKnife 與 findViewById**：引進當年最炙手可熱的 `kotlin-android-extensions` (Kotlin Synthetics)，在 Activity/Fragment 中直接透過 XML ID 操作 View。
  * **POJO 樣板代碼消減 80%**：過去為了定義一個 `VoteData` 實體，需要手寫上百行 Getter/Setter、`equals()`、`hashCode()` 與 `toString()`，改用 `data class` 後濃縮成幾行。

---

## 🚀 技術亮點與規格矩陣 (Technical Highlights)

| 組件層級 | 採納技術 / 規格 | 詳細設計與優勢 |
| :--- | :--- | :--- |
| **開發語言** | JetBrains Kotlin 1.2.61 | 導入空安全、擴充函數 (`Extension Functions`)、高階函數與 `when` 表達式 |
| **核心架構** | Kotlin-style MVP | 將 `Contract`、`Presenter`、`View` 全面以 Kotlin 介面與委託改寫 |
| **視圖綁定** | Kotlin Android Extensions | 直接利用 synthetic view access 取代繁瑣的視圖注入註解 |
| **非同步任務** | AppExecutors (ThreadPool) | 封裝磁碟 I/O 與網路線程池，搭配 Kotlin Lambda 實現簡潔回呼 |
| **資料持久化** | GreenDAO 3.2 | 整合 Kapt 註解處理器生成 SQLite ORM 映射代碼 |
| **單元測試** | JUnit 4 + Mockito-Kotlin | 藉助 `nhaarman/mockito-kotlin` 語法糖編寫簡潔流暢的單元測試 |

---

## 🏗️ 系統架構與設計模式 (Architecture & Design Patterns)

本分支將經典 MVP 注入了現代語言的表達力：

```mermaid
flowchart LR
    subgraph UI_Layer ["View Layer (Kotlin Activity & Fragment)"]
        View["VoteDetailContentActivity.kt\n(kotlinx.android.synthetic.*)"]
    end

    subgraph Presenter_Layer ["Presenter Layer (Kotlin Presenter)"]
        Presenter["VoteDetailPresenter.kt\n(Null-safe Business Logic)"]
    end

    subgraph Data_Layer ["Model Layer (Kotlin Data Classes & Repository)"]
        Repo["VoteDataRepository.kt\n(Thread Pool Callback)"]
        DB["GreenDAO Local DB"]
        Remote["Retrofit 2 Remote API"]
    end

    View -->|1. 使用者點擊 (Lambda 監聽)| Presenter
    Presenter -->|2. 調用 Repository| Repo
    Repo -->|3. 執行緒池發送請求| Remote
    Repo -->|3. 查詢本地 SQLite| DB
    Repo -->|4. 回傳 data class 實體| Presenter
    Presenter -->|5. 視圖狀態更新| View
```

### 經典 Kotlin 1.2 MVP 寫法範例 (源自 `UserPresenter.kt`)
```kotlin
class UserPresenter(
    private val view: PersonalContract.UserView,
    private val userDataRepository: UserDataRepository,
    private val voteDataRepository: VoteDataRepository,
    private val appExecutors: AppExecutors
) : PersonalContract.UserPresenter {

    override fun start() {
        getUserData()
    }

    override fun getUserData() {
        view.showProgress(true)
        appExecutors.networkIO().execute {
            val user = userDataRepository.getUser()
            appExecutors.mainThread().execute {
                view.showProgress(false)
                user?.let { view.showUser(it) } ?: view.showError("User not found")
            }
        }
    }
}
```

---

## 💡 當年時空背景與工程師決策復盤 (Retrospective)

### 為什麼在 2018 年切換到 Kotlin？
2017 年中 Google 宣布 Kotlin 正式支援後，全 Android 圈颳起了大遷移風暴。Java 7/8 的語法陳舊、無處不在的 NPE 恐懼、以及冗長繁複的模板代碼，早已讓前線工程師不堪重負。切換到 Kotlin 讓團隊感受到了「寫代碼能如此輕快」的愉悅。

### 當年留下的工程遺憾與時代眼淚：
1. **時代的眼淚：Kotlin Synthetics (合成視圖)**：
   本分支重度依賴的 `kotlinx.android.synthetic`，雖然在 2018 年驚為天人，但因其在 Fragment 中存在全域 View 快取命中與洩漏隱患，加上無法保證類型安全，幾年後 Google 宣布完全棄用（Deprecated），並由 ViewBinding 與宣告式 Jetpack Compose 接棒。
2. **尚未引進 Coroutines 與 Flow**：
   在 Kotlin 1.2 時代，Coroutines 仍處於實驗性 (Experimental) 階段，因此非同步處理依然退回到自建的 `AppExecutors` 執行緒池或 RxJava，尚未享受到結構化並發的威力。
3. **架構依然受到 MVP 的桎梏**：
   換了語言，但本質依然是 Presenter 與 View 雙向綁定的 MVP，生命週期同步與介面爆炸的本質痛點仍未完全解決。

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
   ├─► [mvp_kotlin] (★ Current)
   │                       └─► 2018 初探 Kotlin：Java 全盤轉 Kotlin 1.2、
   │                           消滅 NPE、使用 Kotlin Synthetics 告別 ButterKnife
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
> 本分支見證了 Android 開發者從 Java 橫渡至 Kotlin 的第一波浪潮，封存了 Kotlin 1.2.61 與 Kotlin Synthetics 的黃金記憶。
