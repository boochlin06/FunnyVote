# 現代 Android 開發 (Modern Android Development) 從入門到精通：深度架構重構實戰指南

## 導論：為什麼我們需要 Modern Android Development (MAD)？

在過去十多年的 Android 開發歷程中，Google 與開源社群不斷在探索如何寫出更好維護、更少 Bug、且更具擴充性的應用程式。早期的 Android 開發充滿了混亂：Activity 身兼數職（處理 UI 渲染、網路請求、資料庫讀寫）、Callback 地獄 (Callback Hell) 隨處可見、以及生命週期 (Lifecycle) 導致的各種 Memory Leak 與 NullPointerException。

為了解決這些問題，Android 開發架構經歷了數次重大演進：
1. **MVC (Model-View-Controller)**：早期的嘗試，但 Controller (通常是 Activity/Fragment) 過於肥大。
2. **MVP (Model-View-Presenter)**：為了解決 Activity 肥大問題，引入了 Presenter 介面，但導致了巨量的 Interface 與繁雜的 View 更新程式碼。
3. **MVVM (Model-View-ViewModel)**：隨著 Android Architecture Components (LiveData, ViewModel) 的推出，MVVM 成為了官方標準。UI 透過觀察 (Observe) 資料的變化來更新自己，大幅減少了手動操作 UI 元件的程式碼。

然而，隨著應用程式變得越來越複雜，傳統的 MVVM 搭配 XML 佈局（Imperative UI，指令式介面）也開始顯露疲態。當一個頁面有多個獨立的 LiveData 時，各種狀態的交錯（例如：載入中、同時發生網路錯誤、使用者又正在輸入文字）往往會導致「不一致的 UI 狀態」。

這正是 **Modern Android Development (MAD)** 誕生的背景。MAD 不是單一的函式庫，而是一整套現代化的開發哲學與工具鏈，其核心基石包含：
* **語言**：全面擁抱 **Kotlin** 及其進階特性 (Coroutines, Flow)。
* **UI 框架**：捨棄 XML，擁抱 **Jetpack Compose** (宣告式 UI)。
* **架構模式**：推崇 **單向資料流 (Unidirectional Data Flow, UDF)** 與 **MVI (Model-View-Intent)**。
* **基礎設施**：依賴注入 (Hilt)、在地資料庫 (Room)、網路層 (Retrofit/OkHttp)。

本指南將以 `FunnyVote` 專案的全面重構為例，帶領第一次接觸或是只有舊版 Android 經驗的開發者，一步步深入理解這些現代化技術的核心概念、為何這樣設計，以及如何在實戰中運用。

---

## 第一章：擁抱宣告式 UI 的典範轉移 —— Jetpack Compose

### 1.1 傳統 XML 與指令式 UI 的痛點

在過去，Android 開發者必須學習兩套語言：用 XML 來描述畫面的長相，再用 Java/Kotlin 來控制畫面的邏輯。
```kotlin
// 傳統 Imperative UI 寫法
val titleTextView = findViewById<TextView>(R.id.tvTitle)
val progressBar = findViewById<ProgressBar>(R.id.progressBar)

fun updateUI(isLoading: Boolean, title: String) {
    if (isLoading) {
        progressBar.visibility = View.VISIBLE
        titleTextView.visibility = View.GONE
    } else {
        progressBar.visibility = View.GONE
        titleTextView.visibility = View.VISIBLE
        titleTextView.text = title
    }
}
```
這種**指令式 (Imperative)** 的寫法要求開發者必須手動改變 UI 元件的狀態 (`setVisibility`, `setText`)。當狀態變數多達四五個時，這個 `updateUI` 函式會變得龐大且充滿 `if-else`，開發者極易忘記在某個分支下隱藏某個元件，導致 UI 出現 Bug。

### 1.2 宣告式 UI (Declarative UI) 的核心思維

**Jetpack Compose** 徹底改變了這個遊戲規則。它採用了**宣告式 (Declarative)** 思維：**「畫面是資料狀態的函數」** (UI = f(State))。

你不再需要手動去 `setText` 或改變 `visibility`，你只需要「描述」在特定的狀態下，畫面「應該」長什麼樣子。當狀態改變時，Compose 框架會自動計算差異，並重新繪製 (Recomposition) 畫面。

```kotlin
// Compose Declarative UI 寫法
@Composable
fun VoteScreen(isLoading: Boolean, title: String) {
    if (isLoading) {
        CircularProgressIndicator() // 如果 isLoading 為 true，畫出圈圈
    } else {
        Text(text = title)          // 否則，畫出文字
    }
}
```
在 Compose 中，你看到的就是全部。沒有隱藏的 XML，沒有繁瑣的 `findViewById`，UI 邏輯與視覺結構完美地融合在同一個語言 (Kotlin) 中。

### 1.3 實戰重構分析：以 FunnyVote 為例
在我們的專案中，我們徹底刪除了所有的 `.xml` 佈局檔案。所有的頁面（如 `MainScreen`, `VoteDetailScreen`, `CreateVoteScreen`）全部被改寫為 `@Composable` 函式。這帶來了巨大的靈活性：我們可以輕鬆地使用 Kotlin 的 `if`, `for` 迴圈來控制畫面的生成，甚至將複雜的 UI 拆解成無數個微小且可重複使用的純函式 (Pure Functions)。

---

## 第二章：架構的躍進 —— 從 MVVM 邁向 MVI (Model-View-Intent)

### 2.1 為什麼傳統 MVVM 還不夠好？

在標準的 MVVM 中，ViewModel 通常會暴露多個 LiveData (或 StateFlow) 供 UI 觀察，同時也會暴露多個公開方法 (Public Methods) 供 UI 呼叫。

```kotlin
// 傳統 MVVM 的 ViewModel
class CreateVoteViewModel : ViewModel() {
    val title = MutableStateFlow("")
    val isLoading = MutableStateFlow(false)

    fun updateTitle(newTitle: String) { title.value = newTitle }
    fun submit() { ... }
}
```
**痛點分析**：
1. **狀態過於零散**：`title` 與 `isLoading` 是分離的。UI 有可能會讀到一個不一致的狀態（例如正在 loading 但 title 卻被清空了）。
2. **多入口點導致難以追蹤**：如果 Bug 發生，我們很難知道是 UI 的哪一個按鈕、哪一個生命週期觸發了哪一個 ViewModel 的函式，因為入口太多了。

### 2.2 單向資料流 (UDF) 與 MVI 的崛起

**MVI (Model-View-Intent)** 是基於**單向資料流 (Unidirectional Data Flow, UDF)** 概念而生的現代化架構。它的核心精神非常簡單且嚴格：

1. **State (Model)**：把畫面的「所有狀態」打包成一個唯一、不可變 (Immutable) 的 Data Class。
2. **View (UI)**：UI 只做一件事，就是觀察這個唯一的 State，並根據它把畫面畫出來。
3. **Intent (意圖)**：使用者在 UI 上的任何操作（點擊按鈕、輸入文字），都不允許直接呼叫 ViewModel 的業務邏輯函式。這些操作必須被包裝成一個 `Intent` 物件，並發送給 ViewModel。ViewModel 有一個單一入口 (通常叫 `onEvent` 或 `handleIntent`) 專門接收所有的 Intent。

### 2.3 實戰演練：重構 CreateVote 模組

讓我們看看在 `FunnyVote` 專案中，我們是如何將 `CreateVote` 模組從 MVVM 重構成 MVI 的。

#### 步驟一：定義單一狀態 (UiState)
我們將所有分散的變數打包到一個 `CreateVoteUiState` 中。因為它是 `data class` 加上 `val` 屬性，所以它是不可變的 (Immutable)。這保證了狀態的安全性，沒有人能偷偷竄改狀態。

```kotlin
data class CreateVoteUiState(
    val title: String = "",
    val option1: String = "",
    val option2: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSuccess: Boolean = false
)
```

#### 步驟二：定義所有可能的意圖 (Intent)
我們使用 Kotlin 強大的 `sealed class` 來窮舉出這個畫面上使用者可以做的所有事情。這就像是這個畫面的「說明書」或「合約」。

```kotlin
sealed class CreateVoteIntent {
    data class UpdateTitle(val title: String) : CreateVoteIntent()
    data class UpdateOption1(val option1: String) : CreateVoteIntent()
    data class UpdateOption2(val option2: String) : CreateVoteIntent()
    object SubmitVote : CreateVoteIntent()
}
```

#### 步驟三：ViewModel 的單一入口 (`handleIntent`)
ViewModel 內部維護一個私有的 `MutableStateFlow`，並對外暴露出唯讀的 `StateFlow`。同時，我們將過去所有的 `public` 函式設為 `private`，並提供一個唯一的 `handleIntent` 函式供 UI 呼叫。

```kotlin
@HiltViewModel
class CreateVoteViewModel @Inject constructor(
    private val repository: VoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateVoteUiState())
    val uiState: StateFlow<CreateVoteUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: CreateVoteIntent) {
        when (intent) {
            is CreateVoteIntent.UpdateTitle -> {
                _uiState.update { it.copy(title = intent.title) }
            }
            is CreateVoteIntent.UpdateOption1 -> {
                _uiState.update { it.copy(option1 = intent.option1) }
            }
            is CreateVoteIntent.UpdateOption2 -> {
                _uiState.update { it.copy(option2 = intent.option2) }
            }
            is CreateVoteIntent.SubmitVote -> {
                submitVote()
            }
        }
    }
    
    private fun submitVote() {
        // ... 非同步網路請求與資料庫操作
    }
}
```

**架構優勢分析**：
* **單一真相來源 (Single Source of Truth)**：UI 的狀態永遠只有一個，絕對不可能出現互相矛盾的 UI 狀態。
* **極佳的可除錯性 (Debuggability)**：當你想要知道為什麼系統崩潰或狀態異常，你只需要在 `handleIntent` 中打個 Log，你就能看到整個使用者操作的歷史軌跡。
* **高內聚低耦合**：ViewModel 不再關心是哪個 Button 被按了，它只關心它收到了什麼「意圖」。

---

## 第三章：State Hoisting (狀態提昇) ── 打造可極致重用的 UI

在我們將架構升級為 MVI 後，我們遇到了下一個挑戰：我們的 Jetpack Compose 函式 (`@Composable`) 還是與 ViewModel 綁得很緊。

### 3.1 為什麼 UI 不能認識 ViewModel？
如果我們在 `CreateVoteScreen` 中直接呼叫 `val viewModel: CreateVoteViewModel = hiltViewModel()`，那麼這個 `CreateVoteScreen` 就**永遠只能在具有依賴注入環境 (Hilt) 且擁有該 ViewModel 的地方使用**。
這會帶來兩個嚴重的後果：
1. **無法預覽 (Preview)**：Android Studio 的 `@Preview` 功能沒有完整的 App 執行環境，無法生成 Hilt ViewModel，導致預覽必定崩潰。
2. **無法重用**：如果我們想在另一個 Dialog 或另一個 Activity 中重複使用這個精美的「建立投票表單」，我們做不到。

### 3.2 State Hoisting 的藝術：Stateful 與 Stateless 的完美分離

為了解決這個問題，Modern Android Development 強烈建議採用 **State Hoisting (狀態提昇)** 的模式。簡單來說，就是把 `@Composable` 拆成兩半：「有狀態的容器」與「無狀態的畫布」。

#### 無狀態的純 UI：Stateless Composable
我們將實際畫圖的邏輯抽離出來，命名為 `CreateVoteScreenContent`。它**絕對不能**接收 ViewModel 作為參數。它只能接收兩樣東西：
1. **資料 (Data Down)**：純資料狀態 (`CreateVoteUiState`)。
2. **事件 (Events Up)**：使用者操作的 Callback 函式 (例如 `onIntent: (CreateVoteIntent) -> Unit`)。

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoteScreenContent(
    uiState: CreateVoteUiState,
    onNavigateBack: () -> Unit,
    onIntent: (CreateVoteIntent) -> Unit
) {
    // 這裡只負責排版與視覺渲染
    OutlinedTextField(
        value = uiState.title,
        onValueChange = { onIntent(CreateVoteIntent.UpdateTitle(it)) }, // 事件往上拋
        label = { Text("Vote Title") }
    )
    // ...
}
```
**為什麼這被稱為純函式 (Pure Function)？** 因為給定相同的 `uiState` 輸入，它保證每次都會畫出完全相同的畫面。它沒有任何副作用 (Side Effects)。

#### 有狀態的容器：Stateful Composable
原本的 `CreateVoteScreen` 就退居幕後，變成了一個單純的「接線生」。它負責向系統拿 ViewModel，負責收集最新的 State，然後把它們轉交給下面的 `Content` 去畫。

```kotlin
@Composable
fun CreateVoteScreen(
    viewModel: CreateVoteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // 1. 收集狀態
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. 轉交給 Stateless 元件
    CreateVoteScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::handleIntent // 方法參考 (Method Reference)，簡潔優雅
    )
}
```

### 3.3 享受重構的甜美果實：無縫預覽 (@Preview)
因為 `CreateVoteScreenContent` 已經是不沾鍋 (不依賴任何外部環境) 的元件了，我們現在可以恣意地在 Android Studio 中為它撰寫各種狀態的預覽畫面，甚至不需要編譯整支 App 到手機上！

```kotlin
@Preview(showBackground = true)
@Composable
fun CreateVoteScreenFilledPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(
            title = "今晚吃什麼？",
            option1 = "披薩",
            option2 = "漢堡"
        ),
        onNavigateBack = {},
        onIntent = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CreateVoteScreenLoadingPreview() {
    CreateVoteScreenContent(
        uiState = CreateVoteUiState(isLoading = true),
        onNavigateBack = {},
        onIntent = {}
    )
}
```
透過這種寫法，UI 設計師或前端工程師可以一次排開檢視該畫面的所有邊界狀態 (Loading, Error, Success, Empty)，這對於視覺走查 (Visual QA) 以及後續撰寫 Screenshot Testing 來說，是不可或缺的巨大優勢。

### 3.4 進階細節：為什麼要用 `collectAsStateWithLifecycle()`？
在 Stateful 元件中，您可能會注意到我們沒有使用預設的 `collectAsState()`，而是使用了 `collectAsStateWithLifecycle()`。這是一項非常重要的 MAD 細節：
當你的 App 退到背景 (Background) 時，如果只是用普通的 `collectAsState()`，Compose 仍然會保持活躍並繼續收集 StateFlow 的更新，這會浪費寶貴的 CPU 與電池資源。
透過引入 `androidx.lifecycle:lifecycle-runtime-compose`，`collectAsStateWithLifecycle()` 讓收集行為變得具有「生命週期感知 (Lifecycle-aware)」。當 Activity 處於不可見狀態 (如 `STOPPED`) 時，它會自動暫停收集資料流，直到使用者再次打開 App 時才恢復，這是確保 App 效能最佳化的專業實踐。

---

## 第四章：基盤建設 ── Hilt 依賴注入與 Coroutines 協程

為了支撐上面這套華麗的 Compose + MVI 架構，我們底層的地基同樣採用了最先進的技術。

### 4.1 Hilt：將依賴注入化繁為簡
過去我們可能使用原生的 Dagger 2 來處理依賴注入，這往往需要撰寫大量的 Component 與 Module，讓新手望之卻步。
Google 針對 Android 推出的 **Hilt** 徹底解決了這個問題。在 `FunnyVote` 專案中，我們只需在 Application 加上 `@HiltAndroidApp`，在 Activity 加上 `@AndroidEntryPoint`，並在 ViewModel 加上 `@HiltViewModel` 與 `@Inject constructor`，一切就大功告成了。
在 Compose 中，我們更是直接調用 `hiltViewModel()` 就能憑空拿到一個幫我們把 Repository 注入好的 ViewModel，極大地提昇了開發效率並降低了耦合度。

### 4.2 Kotlin Coroutines 與 Flow：優雅處理非同步
在 ViewModel 中，當使用者發出 `SubmitVote` 這個 Intent 時，我們必須發起網路請求與資料庫寫入，這些都是耗時任務 (Asynchronous Tasks)。
過去我們可能需要使用 RxJava 或是 Callbacks，導致程式碼艱澀難懂。現在我們全面採用 **Kotlin Coroutines**：

```kotlin
private fun submitVote() {
    // 透過 viewModelScope 啟動協程，當 ViewModel 銷毀時自動取消任務，避免 Memory Leak
    viewModelScope.launch {
        // 更新 UI 狀態為載入中
        _uiState.update { it.copy(isLoading = true, message = null) }
        try {
            // 使用 suspend function，程式碼看起來像同步執行般直覺
            repository.createVote(vote)
            // 成功後更新 UI
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        } catch (e: Exception) {
            // 失敗時的錯誤處理
            _uiState.update { it.copy(isLoading = false, message = e.message) }
        }
    }
}
```
`viewModelScope` 與 `suspend function` 的結合，讓非同步錯誤處理回歸到最純粹的 `try-catch`，這是語言級別帶來的架構紅利。

---

## 第五章：總結與未來展望

回顧整個 `FunnyVote` 的重構旅程，我們經歷了：
1. **UI 框架的革命**：從指令式的 XML 到宣告式的 Jetpack Compose。
2. **架構模式的昇華**：從混亂多入口的 MVVM 演化為嚴謹的單向資料流 MVI。
3. **UI 開發範式的進化**：透過 State Hoisting 拆分 Stateful 與 Stateless，獲得極致的可重用性與預覽能力。
4. **生命週期的優化**：引入 `collectAsStateWithLifecycle` 確保資源不浪費。

對於第一次開發 Android 的人來說，這套 **Modern Android Development (MAD)** 規範雖然初期學習曲線較高（需要理解 Compose 的重繪機制、Coroutines 的作用域、Flow 的行為），但它帶來的回報是豐厚的：
* **更少的 Bug**：單向資料流徹底杜絕了狀態不一致的 UI 錯誤。
* **更高的程式碼閱讀性**：邏輯被清晰地隔離在 Intent 與 ViewModel 之中。
* **無痛的測試環境**：因為業務邏輯與 UI 徹底解耦，您可以輕鬆地為 ViewModel 撰寫單元測試 (利用 `kotlinx-coroutines-test` 與 MockK)，也可以為 Stateless Compose 元件撰寫 UI 測試。

**下一步的學習建議**：
1. **深入理解 Recomposition (重繪)**：學習什麼情況下 Compose 會發生不必要的重繪，並學習使用 `remember` 與 `@Stable` / `@Immutable` 標籤來優化效能。
2. **Navigation Compose 的參數傳遞**：了解如何在不同的 Screen 之間優雅且安全地傳遞參數 (例如使用 Type-safe 的 Serialization 導航)。
3. **深入 Flow 的操作符**：探索 `map`, `combine`, `flatMapLatest` 等 Flow 運算子，它們能幫助你在 ViewModel 中組合出更複雜且強大的響應式資料流。

現代 Android 開發的世界非常精彩，這套 `FunnyVote` 的架構已經為你打下了最堅實的基礎，歡迎來到 Modern Android Development 的世界！
