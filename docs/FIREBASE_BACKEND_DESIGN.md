# FunnyVote Firebase 後端架構設計與既有 API 反推規格書 (Claude 審查優化版)

## 1. 既有 API 反推與架構映射 (Legacy API Mapping)

本方案遵循 NoSQL 設計哲學：「**讀取次數即成本，反正規化即效能，純寫入即高並發**」，並完全採納 Claude 首席架構師之權威裁決。

### 1.1 用戶與認證體系 (UserService)
| 既有 API | 請求 / 參數 | 功能說明 | Firebase 現代化取代方案 |
| :--- | :--- | :--- | :--- |
| `POST api/guest/{name}` | `guestName` (Path) | 訪客登入，產生臨時 guestCode | **Firebase Anonymous Auth** (`signInAnonymously()`)，免密碼直接取得唯一 `uid` |
| `POST api/social/member` | `type`, `appid`, `id`, `name`, `imgurl`, `email`, `gender` | 第三方社群登入 | **Firebase Authentication** (Google / Twitter / OAuth Provider) |
| `PUT api/member` | `token`, `tokentype`, `nickname` | 修改個人暱稱 | 更新 Firebase User Profile (`updateProfile`) 及 Firestore `users/{uid}` |
| `PUT api/link/{otp}/{guest}` | `otp`, `guest` | 將訪客帳號與社群帳號合併綁定 | **Firebase Link Account** (`currentUser.linkWithCredential()`)，保留投票歷史 |
| `GET api/member` | `token`, `tokentype` | 取得用戶個人資訊 | 監聽 Firestore `users/{uid}` 文檔 |

---

### 1.2 投票核心服務 (VoteService)
| 既有 API | 參數與功能 | Firebase 解決方案 (Claude 裁決落實) |
| :--- | :--- | :--- |
| `POST api/poll` | 建立公開/私人投票，可含密碼保護與圖片 | **Firebase Storage** 上傳封面圖 + **Firestore** 批次寫入主文檔 `polls/{pollId}` 與子集合 `options`。含自動 Bi-gram 分詞 (`searchKeywords`)。若設定密碼，機密資料寫入 `secure_polls/{sha256(pollId+pw)}`。 |
| `GET api/poll/{votecode}` | 取得投票詳情 | 讀取 `polls/{pollId}`，同時透過 `voters/{uid}` 檢驗當前用戶是否已投票。 |
| `POST api/vote/{votecode}` | 投出選票 (防重複投票) | **Insert-only (純寫入)**：直接新增文檔到 `polls/{pollId}/voters/{uid}`。Security Rules 以 `!exists()` 強制保證一人一票，並避開頻繁寫入同一計數器的並發鎖問題。 |
| `POST api/option` | 使用者自行新增選項 | 若 `isUserCanAddOption == true`，新增至 `options` 子集合。 |
| `GET api/plist` | 分頁拉取熱門/最新投票 | Firestore 分頁查詢 (`limit` + `startAfter`)。**主文檔反正規化內嵌 `topOptions`**，單次讀取即完整呈現卡片，杜絕 N+1 查詢。 |
| `GET api/fav` / `POST api/fav` | 個人收藏管理 | 操作 `users/{uid}/favorites/{pollId}`，Firestore 本地持久化快取自動同步。 |
| `GET api/search` | 模糊搜尋投票標題 | 透過 `whereArrayContains("searchKeywords", keyword)` 進行 N-gram 索引查詢。 |

---

## 2. Cloud Firestore 資料模型 (Data Schema)

### 2.1 `users` 集合：`users/{userId}`
```json
{
  "uid": "USER_FIREBASE_UID",
  "userName": "何小童",
  "email": "user@example.com",
  "userIcon": "https://storage.googleapis.com/.../avatar.jpg",
  "authProvider": "google.com",
  "isAnonymous": false,
  "gender": "male",
  "createdVoteCount": 5,
  "participatedVoteCount": 24,
  "favoriteVoteCount": 12,
  "createdAt": 1725541200000,
  "updatedAt": 1725541200000
}
```

### 2.2 `polls` 集合：`polls/{pollId}` (主文檔：反正規化設計)
```json
{
  "pollId": "poll_987123",
  "title": "午餐吃什麼？",
  "authorId": "USER_FIREBASE_UID",
  "authorName": "何小童",
  "authorIcon": "https://storage.googleapis.com/.../avatar.jpg",
  "imageUrl": "https://storage.googleapis.com/.../poll_cover.jpg",
  "category": "hot",
  "security": "00", // "00": Public, "01": Private (僅限透過 ID 存取)
  "isNeedPassword": false,
  "isCanPreviewResult": true,
  "isUserCanAddOption": true,
  "minOption": 1,
  "maxOption": 2,
  "optionCount": 4,
  "totalVotes": 142,
  "searchKeywords": ["午餐", "餐吃", "吃什", "什麼"], // Bi-gram 分詞支援模糊搜尋
  "topOptions": [ // 反正規化快取：解決首頁 N+1 查詢問題
    { "optionId": "opt_01", "title": "日式拉麵", "voteCount": 68 },
    { "optionId": "opt_02", "title": "排骨便當", "voteCount": 45 }
  ],
  "startTime": 1725540000000,
  "endTime": 1726144800000,
  "createdAt": 1725540000000
}
```

#### 子集合 1：`polls/{pollId}/options/{optionId}` (完整選項清單)
```json
{
  "optionId": "opt_01",
  "title": "日式拉麵",
  "voteCount": 68,
  "displayOrder": 1,
  "creatorId": "USER_FIREBASE_UID",
  "createdAt": 1725540000000
}
```

#### 子集合 2：`polls/{pollId}/voters/{userId}` (投票記錄：純寫入與一人一票)
```json
{
  "userId": "USER_FIREBASE_UID",
  "selectedOptionIds": ["opt_01"],
  "votedAt": 1725541500000
}
```

### 2.3 零信任密碼保護集合：`secure_polls/{sha256(pollId + password)}`
```json
{
  "pollId": "poll_987123",
  "unlockedData": {
    "fullDescription": "這是一個機密投票，請勿外洩",
    "secretOptions": ["選項A", "選項B"]
  }
}
```
*優勢*：密碼 Hash 絕不上傳公開文檔，客戶端算好 Hash 直接請求該路徑，找不到即密碼錯誤。

---

## 3. Firestore 安全規則 (Security Rules)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId);

      match /favorites/{pollId} {
        allow read, write: if isOwner(userId);
      }
    }

    match /polls/{pollId} {
      allow read: if resource.data.security == "00" || isAuthenticated();
      allow create: if isAuthenticated() && request.resource.data.authorId == request.auth.uid;
      allow update: if isAuthenticated(); // 供計票或作者修改
      allow delete: if isAuthenticated() && resource.data.authorId == request.auth.uid;

      match /options/{optionId} {
        allow read: if true;
        allow create: if isAuthenticated();
        allow update: if isAuthenticated();
      }

      // 一人一票純寫入防刷票規則
      match /voters/{userId} {
        allow read: if isAuthenticated();
        allow create: if isOwner(userId) && !exists(/databases/$(database)/documents/polls/$(pollId)/voters/$(userId));
        allow update, delete: if false;
      }
    }

    // 密碼保護零信任集合：只允許精確路徑讀取，禁止 list 查詢
    match /secure_polls/{secretHash} {
      allow get: if isAuthenticated();
      allow list: if false;
      allow create: if isAuthenticated();
    }
  }
}
```

---

## 4. 本地測試與模擬器規範 (Firebase Local Emulator Suite)

- 支援零成本、無 Google 帳號依賴的離線測試模式：
  - Auth Emulator: `10.0.2.2:9099`
  - Firestore Emulator: `10.0.2.2:8080`
  - Storage Emulator: `10.0.2.2:9199`
- 透過 `IVoteDataSource` 介面切換：
  - `MockVoteDataSource`：離線預設 Mock 資料
  - `FirestoreVoteDataSource`：真正連接 Firebase (或 Emulator)
