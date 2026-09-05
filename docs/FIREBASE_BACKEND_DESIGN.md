# FunnyVote Firebase 後端架構設計與既有 API 反推規格書

## 1. 既有 API 反推與映射分析 (Legacy API Analysis)

根據 `main` 分支歷史代碼 (`Server.java`, `RemoteServiceApi.java`, `VoteData.java`, `Option.java`, `User.java`, `Promotion.java`)，既有後端架構與對應之 Firebase 方案如下：

### 1.1 用戶與認證體系 (UserService)
| 既有 API | 請求 / 參數 | 功能說明 | Firebase 取代方案 |
| :--- | :--- | :--- | :--- |
| `POST api/guest/{name}` | `guestName` (Path) | 訪客登入，產生臨時 guestCode | **Firebase Anonymous Auth** (`signInAnonymously()`)，直接取得唯一 `uid` |
| `POST api/social/member` | `type` (FB/Google/Twitter), `appid`, `id`, `name`, `imgurl`, `email`, `gender` | 第三方社群註冊與登入 | **Firebase Authentication** 支援 Google / Twitter 等 OAuth Provider |
| `PUT api/member` | `token`, `tokentype`, `nickname` | 修改個人暱稱 | 更新 Firebase User Profile (`updateProfile`) 及 Firestore `users/{uid}` |
| `PUT api/link/{otp}/{guest}` | `otp`, `guest` | 將訪客帳號與社群帳號合併綁定 | **Firebase Link Account** (`currentUser.linkWithCredential()`) |
| `GET api/member` | `token`, `tokentype` | 取得用戶個人資訊 | 讀取 Firestore `users/{uid}` |

---

### 1.2 投票核心服務 (VoteService)
| 既有 API | 請求 / 參數 | 功能說明 | Firebase 取代方案 |
| :--- | :--- | :--- | :--- |
| `POST api/poll` | Multipart: `t` (標題), `min`/`max`, `add` (可否自增選項), `res` (開票預覽), `sec` (00公/01私), `cat`, `on`/`off` (起迄), `pt[i]` (選項), `p` (密碼), `file` (封面圖) | 建立投票 | **Firebase Storage** 上傳封面圖 + **Firestore** 建立 `polls/{pollId}` 及子集合 `options` (批次寫入 `WriteBatch`) |
| `GET api/poll/{votecode}` | `votecode`, `token` | 取得單一投票詳情 | 讀取 `polls/{pollId}` 與 `polls/{pollId}/options`，並檢查 `polls/{pollId}/voters/{uid}` 是否已投票 |
| `POST api/vote/{votecode}` | `votecode`, `p` (密碼), `oc` (選項代碼列表), `token` | 投出選票 | **Firestore RunTransaction**：原子遞增 `options/{optionId}.voteCount` 與 `polls/{pollId}.totalVotes`，並寫入 `polls/{pollId}/voters/{uid}` 防重複投票 |
| `POST api/option` | `c` (votecode), `p` (密碼), `ot` (新選項列表) | 使用者自由新增選項 | 檢查 poll 的 `isUserCanAddOption` 規則後，向 `polls/{pollId}/options` 寫入新文檔，並原子更新 `optionCount` |
| `GET api/plist` | `p` (頁碼), `ps` (每頁筆數), `o` (hot / new) | 分頁拉取熱門/最新投票清單 | Firestore 分頁查詢：<br>• Hot: `polls.whereEqualTo("security", "00").orderBy("totalVotes", DESCENDING).limit(ps)`<br>• New: `polls.whereEqualTo("security", "00").orderBy("createTime", DESCENDING).limit(ps)` |
| `GET api/fav` | `p`, `ps`, `token` | 取得個人收藏清單 | 查詢子集合 `users/{uid}/favorites` 或反查 `polls` |
| `POST api/fav` | `c` (votecode), `action` (01/00) | 收藏 / 取消收藏 | 寫入或刪除 `users/{uid}/favorites/{pollId}`，同步更新 `polls/{pollId}.favoriteCount` |
| `GET api/search` | `keyword`, `p`, `ps` | 搜尋投票標題 | Firestore 關鍵字前綴查詢或 Algolia / Firebase Extension Search |
| `GET api/public/create` | `targetToken` | 取得特定用戶發起的公開投票 | 查詢 `polls.whereEqualTo("authorId", targetUid).whereEqualTo("security", "00")` |
| `GET api/public/fav` | `targetToken` | 取得特定用戶的公開收藏 | 查詢 `users/{targetUid}/favorites` |

---

### 1.3 焦點推薦服務 (PromotionService)
| 既有 API | 請求 / 參數 | 功能說明 | Firebase 取代方案 |
| :--- | :--- | :--- | :--- |
| `GET api/promotion` | `p`, `ps` | 首頁輪播橫幅橫向展示 | 查詢 Firestore `promotions` 集合，依 `displayOrder` 排序 |

---

## 2. Cloud Firestore 資料模型 (Data Schema)

### 2.1 `users` 集合：`users/{userId}`
```json
{
  "uid": "USER_FIREBASE_UID",
  "userName": "何小童",
  "email": "user@example.com",
  "userIcon": "https://storage.googleapis.com/.../avatar.jpg",
  "authProvider": "google.com", // anonymous, google.com, twitter.com
  "isAnonymous": false,
  "gender": "male",
  "createdVoteCount": 5,
  "participatedVoteCount": 24,
  "favoriteVoteCount": 12,
  "createdAt": 1725541200000,
  "updatedAt": 1725541200000
}
```

### 2.2 `polls` 集合：`polls/{pollId}`
```json
{
  "pollId": "poll_987123",
  "title": "午餐吃什麼？",
  "authorId": "USER_FIREBASE_UID",
  "authorName": "何小童",
  "authorIcon": "https://storage.googleapis.com/.../avatar.jpg",
  "imageUrl": "https://storage.googleapis.com/.../poll_cover.jpg",
  "category": "hot",
  "security": "00", // "00": Public, "01": Private (need voteCode to search/access)
  "isNeedPassword": false,
  "passwordHash": null, // SHA-256 (若有設密碼)
  "isCanPreviewResult": true,
  "isUserCanAddOption": true,
  "minOption": 1,
  "maxOption": 2,
  "optionCount": 4,
  "totalVotes": 142,
  "favoriteCount": 18,
  "startTime": 1725540000000,
  "endTime": 1726144800000,
  "createdAt": 1725540000000
}
```

#### 子集合 1：`polls/{pollId}/options/{optionId}`
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

#### 子集合 2：`polls/{pollId}/voters/{userId}` (防刷票與計票紀錄)
```json
{
  "userId": "USER_FIREBASE_UID",
  "selectedOptionIds": ["opt_01"],
  "votedAt": 1725541500000
}
```

### 2.3 `users/{userId}/favorites/{pollId}` (個人收藏子集合)
```json
{
  "pollId": "poll_987123",
  "createdAt": 1725542000000
}
```

### 2.4 `promotions` 集合：`promotions/{promotionId}`
```json
{
  "id": "promo_01",
  "title": "年度最佳人氣投票大賽",
  "imageUrl": "https://storage.googleapis.com/.../banner_1.jpg",
  "actionUrl": "funnyvote://poll/poll_987123",
  "displayOrder": 1,
  "isActive": true
}
```

---

## 3. 安全規則設計 (Firestore Security Rules)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 使用者認證
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // User Profile
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId);

      match /favorites/{pollId} {
        allow read, write: if isOwner(userId);
      }
    }

    // Polls
    match /polls/{pollId} {
      allow read: if true;
      allow create: if isAuthenticated() && request.resource.data.authorId == request.auth.uid;
      allow update: if isAuthenticated(); // 限由 transaction 遞增 totalVotes，或作者修改
      allow delete: if isAuthenticated() && resource.data.authorId == request.auth.uid;

      // Options
      match /options/{optionId} {
        allow read: if true;
        // 允許作者新增，或該投票設定 isUserCanAddOption == true 時由任何已登入使用者新增
        allow create: if isAuthenticated();
        allow update: if isAuthenticated(); // 計票遞增
      }

      // Voters: 一人一票防重複投票
      match /voters/{userId} {
        allow read: if isAuthenticated();
        // 只能寫入自己的紀錄，且不得覆蓋已存在的紀錄
        allow create: if isOwner(userId) && !exists(/databases/$(database)/documents/polls/$(pollId)/voters/$(userId));
        allow update, delete: if false; // 投完後不允許竄改或刪除
      }
    }

    // Promotions
    match /promotions/{promoId} {
      allow read: if true;
      allow write: if false; // 僅後端/管理員維護
    }
  }
}
```
