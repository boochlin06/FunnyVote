# FunnyVote 趣投票 🗳️ (2016 經典 Java 歷史原版)

<p align="center">
  <img src="http://vinta.ws/booch/wp-content/uploads/2017/04/g294.png" alt="FunnyVote Classic Logo" width="260" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Java%207%2F8-orange.svg?style=flat" alt="Language" />
  <img src="https://img.shields.io/badge/Android%20SDK-API%2023%20(Marshmallow)-green.svg?style=flat" alt="Android SDK" />
  <img src="https://img.shields.io/badge/View%20Binding-ButterKnife%207.0-blue.svg?style=flat" alt="ButterKnife" />
  <img src="https://img.shields.io/badge/Event%20Bus-EventBus%202.4-brightgreen.svg?style=flat" alt="EventBus" />
  <img src="https://img.shields.io/badge/ORM-ActiveAndroid%20%2F%20GreenDAO-yellowgreen.svg?style=flat" alt="ORM" />
  <img src="https://img.shields.io/badge/Networking-Retrofit%202.0-blue.svg?style=flat" alt="Retrofit" />
  <img src="https://img.shields.io/badge/Branch-main%20(Legacy%20Baseline)-lightgrey.svg?style=flat" alt="Branch" />
</p>

---

## 📖 基本資料 (Basic Info)

* **專案定位**：2016 年 Android 開發黃金時期的經典全功能趣味投票系統 (Legacy Baseline Repository)。
* **分支角色 (`main`)**：**歷史代碼基準庫與對照起點**。完整封存十年前以 Java、XML、事件總線與關聯式 ORM 打造的經典架構，作為整個 FunnyVote 現代化重構之路的初衷與起點。
* **當年解決之痛點**：
  * **非同步執行緒與回呼解耦**：在 RxJava 與 Coroutines 尚未普及的時代，藉由 `EventBus` 解決 Activity 與背景網路傳輸的強耦合。
  * **離線資料庫快取**：透過 `GreenDAO` / `ActiveAndroid` SQLite ORM 映射遠端 JSON，實現無網路時投票列表的秒開體驗。
  * **流暢滾動與視差動畫**：手刻 `ObservableScrollView` 與自訂九宮格 View，達成平滑頂部收合動畫效果。

---

## 🚀 技術亮點與規格矩陣 (Technical Highlights)

| 組件層級 | 採納技術 / 規格 | 詳細設計與特徵 |
| :--- | :--- | :--- |
| **開發語言** | Java 7 / 8 (Android SDK 23) | 面向對象經典設計，大量使用內部匿名類別 (Anonymous Classes) 與監聽器 |
| **UI 與視圖綁定** | XML Layout + ButterKnife 7.0 | 利用 `@Bind` 註解在編譯期生成 ViewBinding，取代手寫繁瑣的 `findViewById` |
| **解耦與事件** | greenrobot EventBus 2.4 | 發布/訂閱 (Pub/Sub) 模式，以 `@Subscribe` 標記非同步接收背景事件 |
| **本地資料庫** | ActiveAndroid / GreenDAO | 經典 SQLite ORM 框架，以 Java Bean 映射本地關聯式資料表 |
| **網路通訊層** | Retrofit 2.0 + OkHttp | 宣告式 RESTful API 介面，搭配 GSON 轉換器自動解析後端 JSON |
| **自訂動態控制** | Android-ObservableScrollView | 監聽滾動距離，手動動態計算 Alpha 透明度與 ToolBar 視差位移 |

---

## 🏗️ 系統架構與設計模式 (Architecture & Design Patterns)

本分支為典型的 **經典 MVC + EventBus 集中式管理架構**：

```mermaid
flowchart TD
    subgraph UI_View_Layer ["View / Controller Layer (Activity & Fragment)"]
        Activity["VoteDetailActivity / MainActivity\n(ButterKnife @Bind View 綁定)"]
        Subscriber["@Subscribe Event Receiver\n(UI 更新與 ProgressDialog 關閉)"]
    end

    subgraph Event_Bus ["Decoupling Layer (EventBus 2.4)"]
        Bus["EventBus.getDefault()\n(廣播事件總線)"]
    end

    subgraph Data_Manager ["Manager Layer (VoteDataManager)"]
        Manager["VoteDataManager (Singleton)\n• 執行緒切換\n• 判斷走 Local 還是 Remote"]
    end

    subgraph Storage_Layer ["Data Storage & Network Layer"]
        DB["SQLite Database\n(ActiveAndroid / GreenDAO)"]
        Server["Remote REST API (Retrofit 2.0)\n(呼叫 VPS 自建 PHP/Node.js 後端)"]
    end

    Activity -->|1. 使用者操作 (如點擊投票)| Manager
    Manager -->|2. 背景執行緒發送 HTTP 請求| Server
    Server -->|3. 回傳 JSON 資料| Manager
    Manager -->|4. 寫入本地 SQLite 快取| DB
    Manager -->|5. post(VoteSuccessEvent)| Bus
    Bus -->|6. 分發事件至主執行緒| Subscriber
    Subscriber -->|7. 刷新 View 狀態與進度條| Activity
```

---

## 📁 舊版專案結構 (Classic Java Package Structure)

```text
funnyvote/app/src/main/
├── java/com/heaton/funnyvote/
│   ├── BaseActivity.java                   # 基礎 Activity (ActionBar 與沉浸式標題封裝)
│   ├── MainActivity.java                   # 主頁面 (ViewPager + Fragment 管理)
│   ├── VoteDetailActivity.java             # 投票詳情 (動態 AddView 渲染選項)
│   ├── CreateVoteActivity.java             # 發起投票頁面
│   ├── PersonalActivity.java               # 個人中心 (折疊佈局)
│   ├── data/                               # 實體類與 ORM Model (VoteData, User, Option)
│   ├── event/                              # EventBus 專用事件 POJO (VoteEvent, RefreshEvent)
│   ├── manager/                            # VoteDataManager (集中式業務經理)
│   └── retrofit/                           # Server API 介面定義與 Retrofit Client
└── res/
    ├── layout/                             # 經典 XML 佈局文件 (30+ 份複雜 ViewTree)
    └── values/                             # styles.xml, colors.xml (Holo / Material Design 1)
```

---

## 🌿 各分支演進地圖 (Branch Evolutionary Roadmap)

本分支作為整個專案十年間開枝散葉的**最根本原點**：

```text
[main] (★ Current) ──► 2016 經典 Java / ButterKnife / EventBus / SQLite 原版
   │
   ├─► [kotlin-rewrite] ──► 語法現代化：Java 轉 Kotlin、引進 Coroutines 與基礎 Compose
   │
   ├─► [mvi-rewrite] ────► 架構規範化：探索嚴格 MVI 單向資料流 (UDF) 基礎
   │
   ├─► [modern-android] ─► 全面現代化：Compose 100% 畫面補全、Room 本地快取、Hilt 注入
   │
   └─► [feature/firebase-backend]
                           └─► 雲原生躍遷：Firebase Serverless、Cloud Firestore、
                               離線持久化、實體 Android 16 真機驗收
```

---

## 📦 舊版建置環境備註 (Legacy Build Environment)

> ⚠️ **歷史構建提示**：
> 本分支之依賴與 Gradle 配置為 2016 年環境（Gradle 2.x ~ 3.x、JDK 7/8、Android SDK 23）。若在現代 Android Studio (如 Iguana/Ladybug) 或 JDK 17+ 環境下編譯，建議直接切換至 [`modern-android`](https://github.com/boochlin06/FunnyVote/tree/modern-android) 或 [`feature/firebase-backend`](https://github.com/boochlin06/FunnyVote/tree/feature/firebase-backend) 分支進行體驗。
