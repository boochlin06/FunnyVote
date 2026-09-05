# FunnyVote 趣投票 🗳️ (2017 MVP + RxJava 響應式進化版)

<p align="center">
  <img src="http://vinta.ws/booch/wp-content/uploads/2017/04/g294.png" alt="FunnyVote Classic Logo" width="260" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Java%207%2F8-orange.svg?style=flat" alt="Language" />
  <img src="https://img.shields.io/badge/Architecture-MVP%20%2B%20RxJava-red.svg?style=flat" alt="Architecture" />
  <img src="https://img.shields.io/badge/Reactive-RxJava%201.2-purple.svg?style=flat&logo=reactivex" alt="RxJava" />
  <img src="https://img.shields.io/badge/Threading-Schedulers-blue.svg?style=flat" alt="Schedulers" />
  <img src="https://img.shields.io/badge/Networking-Retrofit%202%20RxAdapter-blue.svg?style=flat" alt="Retrofit RxAdapter" />
  <img src="https://img.shields.io/badge/Database-GreenDAO%203.0-yellowgreen.svg?style=flat" alt="GreenDAO" />
  <img src="https://img.shields.io/badge/Branch-mvp__rxjava%20(Reactive%20Pioneer)-lightgrey.svg?style=flat" alt="Branch" />
</p>

---

## 📖 基本資料 (Basic Info)

* **專案定位**：2017 年探索響應式函數編程 (FRP, Functional Reactive Programming) 的里程碑分支。
* **分支角色 (`mvp_rxjava`)**：**非同步架構現代化關鍵節點**。將原本 MVP 中零散混亂的非同步回呼 (Callbacks) 與 Handler 執行緒切換，全盤改裝為以 `RxJava Observable` 為核心的數據事件串流。
* **當年解決之核心痛點**：
  * **消滅多層巢狀回呼 (Callback Hell)**：多個依賴請求（例如：先驗證登入 Token、再拉取使用者個人資料、最後查詢投票歷史）在傳統 Java 中會形成三層深度的 `Callback` 梯形代碼，改用 RxJava `flatMap` 鏈式調用後扁平化為單一清晰流程。
  * **宣告式執行緒調度**：淘汰傳統 `AsyncTask` 與 `Handler.sendMessage()`，利用 `.subscribeOn()` 與 `.observeOn()` 精準指定背景運算與 UI 執行緒。
  * **單元測試非同步抽換**：抽象出 `BaseSchedulerProvider`，在單元測試中將所有 `Schedulers.io()` 替換為 `Schedulers.immediate()`，讓異步測試能同步執行。

---

## 🚀 技術亮點與規格矩陣 (Technical Highlights)

| 組件層級 | 採納技術 / 規格 | 詳細設計與優勢 |
| :--- | :--- | :--- |
| **響應式核心** | Netflix RxJava (v1.x) | 以 `Observable`、`Subscriber` 為事件基礎，實現事件流式處理 |
| **執行緒調度** | `BaseSchedulerProvider` | 封裝 `io()`、`computation()` 與 `ui()`，提供生產與測試環境無縫切換 |
| **生命週期管理** | `CompositeSubscription` | Presenter 統一註冊所有請求，在 `unsubscribe()` 時一次釋放，防止 Memory Leak |
| **網路串接** | Retrofit 2 + RxJava Adapter | API 介面直接返回 `Observable<VoteData>`，無縫銜接各項 Rx 操作符 |
| **資料整合層** | Repository Pattern + Rx | 本地 GreenDAO 查詢與遠端 HTTP 請求均包裝為 Observable 流 |

---

## 🏗️ 系統架構與設計模式 (Architecture & Design Patterns)

本分支將資料傳遞全面串流化，形成標準的響應式 MVP 管線：

```mermaid
flowchart TD
    subgraph UI_Layer ["View Layer (Passive View)"]
        Activity["Activity / Fragment"]
    end

    subgraph Presenter_Layer ["Presenter Layer (Rx Subscription)"]
        Presenter["VoteDetailPresenter\n• CompositeSubscription 管理\n• Observable 操作鏈 (map, filter)"]
    end

    subgraph Schedulers_Engine ["Threading Engine (SchedulerProvider)"]
        Worker["subscribeOn(Schedulers.io())\n(背景 I/O 執行緒)"]
        MainThread["observeOn(AndroidSchedulers.mainThread())\n(Android 主執行緒)"]
    end

    subgraph Data_Layer ["Data Layer (Observable Source)"]
        Retrofit["Retrofit 2 RxCallAdapter\n(Observable&lt;VoteData&gt;)"]
        DB["GreenDAO Local DB\n(Observable&lt;List&gt;)"]
    end

    Activity -->|1. 使用者操作 (如投票)| Presenter
    Presenter -->|2. 發起請求串流| Retrofit
    Presenter -->|2. 併發讀取本地快取| DB
    Retrofit -->|3. 發出事件| Worker
    DB -->|3. 發出事件| Worker
    Worker -->|4. 數據轉換與合併| MainThread
    MainThread -->|5. onNext(data) / onError(e)| Presenter
    Presenter -->|6. 更新 View 狀態| Activity
```

### 典型 RxJava 呼叫範例 (源自 `VoteDetailPresenter.java`)
```java
subscriptions.add(
    voteDataRepository.getVoteData(voteCode)
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe(new Subscriber<VoteData>() {
            @Override
            public void onCompleted() {
                mView.hideProgress();
            }

            @Override
            public void onError(Throwable e) {
                mView.showErrorMessage(e.getMessage());
            }

            @Override
            public void onNext(VoteData voteData) {
                mView.showVoteDetail(voteData);
            }
        })
);
```

---

## 💡 當年時空背景與工程師決策復盤 (Retrospective)

### 為什麼在 2017 年引進 RxJava？
在 Kotlin Coroutines 尚未問世（甚至 Kotlin 本身在 Android 圈子都還未被 Google 正式扶正）的年代，RxJava 是 Android 工程界唯一能與複雜非同步抗衡的「降維打擊武器」。它讓工程師第一次體會到「所有事件皆可視為串流 (Everything is a Stream)」的優雅。

### 當年留下的工程遺憾與踩坑血淚：
1. **陡峭至極的學習曲線 (RxJava Hell)**：
   操作符多達數百個，新手往往分不清 `flatMap`、`concatMap` 與 `switchMap` 的差異；在未完全掌握背壓 (Backpressure) 機制前，頻繁遭遇 `MissingBackpressureException`。
2. **忘記取消訂閱的記憶體災難**：
   Presenter 如果忘記在銷毀時呼叫 `subscriptions.clear()`，背景的 Observable 依然會持有對象，繼續對已經銷毀的 View 發送 `onNext()`，引發著名的崩潰：`IllegalStateException: Activity has been destroyed`。
3. **過多樣板 Subscription 物件**：
   在 Java 7/8 時代缺少 Kotlin Lambda 的語法糖衣，每個訂閱都要寫 `new Subscriber<T>() { ... }` 包含三個重載方法，代碼行數依舊居高不下。

---

## 🌿 各分支演進地圖 (Branch Evolutionary Roadmap)

```text
[main] ───────────────► 2016 經典 Java / ButterKnife / EventBus / SQLite
   │
   ├─► [mvp] ──────────► 2017 初次解耦：導入 Google MVP Blueprint、Contract 契約設計
   │
   ├─► [mvp_rxjava] (★ Current)
   │                       └─► 2017 響應式進化：引入 RxJava 切換執行緒、
   │                           消滅 Callback Hell、統一數據串流管線
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
> 本分支採用 RxJava 1.2+ 與 Android SDK 25，封存了 Android 響應式編程初代的經典寫法。後續分支將由 Kotlin Coroutines Flow 全面接棒演進。
