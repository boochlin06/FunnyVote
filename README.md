# FunnyVote 趣投票 🗳️ (2017 MVP + Dagger 2 依賴注入版)

<p align="center">
  <img src="http://vinta.ws/booch/wp-content/uploads/2017/04/g294.png" alt="FunnyVote Classic Logo" width="260" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Java%207%2F8-orange.svg?style=flat" alt="Language" />
  <img src="https://img.shields.io/badge/Architecture-MVP%20%2B%20Dagger%202-red.svg?style=flat" alt="Architecture" />
  <img src="https://img.shields.io/badge/DI-Dagger%202.11-green.svg?style=flat&logo=dagger" alt="Dagger 2" />
  <img src="https://img.shields.io/badge/Android%20Injection-dagger.android-blue.svg?style=flat" alt="dagger.android" />
  <img src="https://img.shields.io/badge/Annotation%20Processor-APT-lightgrey.svg?style=flat" alt="APT" />
  <img src="https://img.shields.io/badge/Database-GreenDAO%203.0-yellowgreen.svg?style=flat" alt="GreenDAO" />
  <img src="https://img.shields.io/badge/Branch-mvp__dagger%20(IoC%20Evolution)-lightgrey.svg?style=flat" alt="Branch" />
</p>

---

## 📖 基本資料 (Basic Info)

* **專案定位**：2017 年探索依賴注入 (DI, Dependency Injection) 與控制反轉 (IoC) 的架構進化分支。
* **分支角色 (`mvp_dagger`)**：**解耦工程的極致探索**。為了解決前兩代分支中手動 `new` 實例化 Presenter 與 Repository 所帶來的強耦合，全面引入當時 Google 官方力推的 **Dagger 2 (含 `dagger.android`)**。
* **當年解決之核心痛點**：
  * **消滅繁瑣的手工構造工廠**：在 `mvp` 分支中，Activity 為了初始化 Presenter，需要層層建構 `LocalDataSource`、`RemoteDataSource`、`Repository`，代碼冗長且極易出錯。Dagger 2 透過編譯期生成代碼自動組裝物件圖 (Object Graph)。
  * **單例與生命週期精準控制**：透過 `@Singleton` 與自訂 Scope 註解，確保全 App 共享同一份 `Retrofit`、`DaoSession` 與 `UserDataRepository`，避免資源浪費。
  * **解救單元測試**：在單元測試中，只需替換 `@Module` 即可注入 Mock 物件，無需修改任何業務代碼。

---

## 🚀 技術亮點與規格矩陣 (Technical Highlights)

| 組件層級 | 採納技術 / 規格 | 詳細設計與特徵 |
| :--- | :--- | :--- |
| **依賴注入核心** | Google Dagger 2.11 | 編譯期靜態生成代碼（APT / Annotation Processing），無反射損耗 |
| **Android 專用擴展** | `dagger.android.support` | 利用 `AndroidInjector` 與 `ActivityBindingModule` 簡化 Activity 注入 |
| **模組劃分** | 職責單一 Module | 拆分 `ApplicationModule`、`VoteDataRepositoryModule`、`UserRepositoryModule` |
| **Presenter 注入** | `@Inject` 構造函數注入 | Presenter 依賴的所有 Repository 與 Scheduler 均由 Dagger 自動注入 |
| **資料持久與網路** | GreenDAO + Retrofit 2 | 透過 `@Provides` 在編譯期建構 Singleton 實例並託管於 `AppComponent` |

---

## 🏗️ 系統架構與設計模式 (Architecture & Design Patterns)

Dagger 2 作為整個 App 的「心臟」，在編譯期靜態建構依賴拓撲圖：

```mermaid
flowchart TD
    subgraph Dagger_Container ["Dagger 2 Container (@Singleton AppComponent)"]
        AppModule["ApplicationModule\n(Context)"]
        RepoModule["VoteDataRepositoryModule\n(Remote & Local)"]
        NetModule["Network Module\n(Retrofit, OkHttp)"]
        DBModule["Database Module\n(DaoSession)"]
    end

    subgraph Injector_Layer ["dagger.android Bridge (ActivityBindingModule)"]
        MainActivitySub["MainActivitySubcomponent\n(@ContributesAndroidInjector)"]
        DetailActivitySub["VoteDetailActivitySubcomponent\n(@ContributesAndroidInjector)"]
    end

    subgraph UI_Consumers ["Consumers (UI Layer)"]
        MainActivity["MainActivity\n(@Inject MainPagePresenter)"]
        DetailActivity["VoteDetailActivity\n(@Inject VoteDetailPresenter)"]
    end

    AppModule --> RepoModule
    NetModule --> RepoModule
    DBModule --> RepoModule

    RepoModule --> MainActivitySub
    RepoModule --> DetailActivitySub

    MainActivitySub -->|AndroidInjection.inject(this)| MainActivity
    DetailActivitySub -->|AndroidInjection.inject(this)| DetailActivity
```

### 經典 Dagger 2 Component 定義範例 (源自 `AppComponent.java`)
```java
@Singleton
@Component(modules = {
    VoteDataRepositoryModule.class,
    PromotionRepositoryModule.class,
    UserRepositoryModule.class,
    ApplicationModule.class,
    ActivityBindingModule.class,
    AndroidSupportInjectionModule.class
})
public interface AppComponent extends AndroidInjector<FunnyVoteApplication> {
    VoteDataRepository getVoteDataRepository();
    PromotionRepository getPromotionRepository();
    UserDataRepository getUserDataRepository();

    @Component.Builder
    interface Builder {
        @BindsInstance
        AppComponent.Builder application(Application application);
        AppComponent build();
    }
}
```

---

## 💡 當年時空背景與工程師決策復盤 (Retrospective)

### 為什麼在 2017 年引進 Dagger 2？
當專案規模從十幾個頁面成長到數十個頁面時，物件依賴就像滾雪球一樣。若沒有 IoC 容器，一個底層 DAO 構造函數加參數，會引發連鎖反應修改上百處代碼。Dagger 2 是當時業界唯一能在維持高運行效能（零反射）的前提下解決此問題的終極利器。

### 當年留下的工程遺憾與踩坑血淚：
1. **傳說中「天書級」的編譯報錯**：
   Dagger 2 的報錯訊息是所有 Android 工程師的惡夢。只要漏掉一個 `@Provides` 或 Scope 標記衝突，編譯器會噴出幾百行由 APT 產生的堆疊資訊，新手往往需要花上一整天才能排查出少了一個 `@Singleton`。
2. **`dagger.android` 的過度設計**：
   Google 當年為了減少 Activity 手動 `getComponent().inject(this)` 的樣板代碼，設計了 `dagger.android` 套件。然而引入了 `@ContributesAndroidInjector`、`AndroidInjector.Factory`、子組件 (Subcomponents) 之後，架構反而更加晦澀，學習門檻直接登頂。
3. **編譯時間大幅拉長**：
   APT 大量生成輔助類別（`DaggerAppComponent.java`, `*_MembersInjector.java`, `*_Factory.java`），導致冷編譯時間倍增。
   *（這也是為什麼 Google 多年後深刻反省，在 2020 年基於 Dagger 2 重新包裝推出簡潔許多的 **Hilt**）。*

---

## 🌿 各分支演進地圖 (Branch Evolutionary Roadmap)

```text
[main] ───────────────► 2016 經典 Java / ButterKnife / EventBus / SQLite
   │
   ├─► [mvp] ──────────► 2017 初次解耦：導入 Google MVP Blueprint、Contract 契約設計
   │
   ├─► [mvp_rxjava] ────► 2017 響應式進化：引入 RxJava 切換執行緒、統一數據串流管線
   │
   ├─► [mvp_dagger] (★ Current)
   │                       └─► 2017 依賴注入：引入 Dagger 2 與 dagger.android，
   │                           實現編譯期依賴拓撲圖與 IoC 控制反轉
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
> 本分支深度整合 Dagger 2.11 與 APT 註解處理器，記錄了 Android 依賴注入早期最硬派、最複雜的工程探索歷史。
