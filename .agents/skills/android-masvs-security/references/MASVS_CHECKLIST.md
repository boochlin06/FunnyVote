# OWASP MASVS (Mobile Application Security Verification Standard) v2.0 對照清單

OWASP MASVS 是行動應用程式安全標準的業界黃金準則。MASVS v2.0 聚焦於以下 7 大核心領域：

---

## 1. MASVS-STORAGE (資料儲存與隱私保護)
> 確保機密資料（憑證、金鑰、個人隱私資料 PII）在本地端安全儲存。

- **MASVS-STORAGE-1**: 應用程式妥善保護機密資料免於未授權讀取。
  - 嚴禁 `android:allowBackup="true"`（除非明確提供自訂 extraction rules）。
  - 禁止在 SharedPreferences 中明文存儲 Token/密碼，應改用 `EncryptedSharedPreferences` (Jetpack Security)。
  - 本地 SQLite / Room 若含隱私資訊，應引入 SQLCipher 進行資料庫層級 AES-256 加密。
  - 避免將私密檔案寫入外部儲存區 (`Environment.getExternalStorageDirectory()`)。
- **MASVS-STORAGE-2**: 應用程式防止敏感資料透過系統機制外洩。
  - 截圖與 Recent Task 洩漏：敏感頁面配置 `FLAG_SECURE` (`window.setFlags(FLAG_SECURE, FLAG_SECURE)`)。
  - 鍵盤快取洩漏：密碼輸入欄位必須指定 `inputType="textPassword"`。
  - 剪貼簿保護：Android 13+ 呼叫 `setClipData` 時加入 `ClipDescription.EXTRA_IS_SENSITIVE`。

---

## 2. MASVS-CRYPTO (密碼學架構)
> 確保加密機制符合現代密碼學標準，並安全管理密鑰生命週期。

- **MASVS-CRYPTO-1**: 僅採用現代業界驗證之安全密碼學演算法與模式。
  - 對稱加密：使用 `AES/GCM/NoPadding` (禁止 `AES/ECB`、`DES`、`3DES`、`RC4`)。
  - 雜湊演算法：使用 `SHA-256` / `SHA-512`（禁止將 `MD5` / `SHA-1` 用於安全驗證）。
  - 密碼保存：採用 PBKDF2WithHmacSHA256、BCrypt 或 Argon2id 加鹽雜湊。
  - 隨機數：全面採用 `java.security.SecureRandom`，禁止 `java.util.Random`。
- **MASVS-CRYPTO-2**: 密鑰在 Android Keystore 內安全生成與儲存。
  - 禁止在代碼庫中硬編碼密鑰 (`SecretKeySpec(keyBytes, "AES")`)。
  - 密鑰應儲存於硬體安全模組 (TEE / StrongBox Keymaster)。

---

## 3. MASVS-AUTH (身分驗證與授權)
> 確保使用者身分驗證穩固，並在伺服器端實施最終授權決策。

- **MASVS-AUTH-1**: 生物辨識整合必須綁定密碼學物件。
  - 呼叫 `BiometricPrompt.authenticate(promptInfo, cryptoObject)`，確保驗證通過後才能解鎖 Keystore 密鑰，杜絕純 UI 層面的 Hook 繞過。
- **MASVS-AUTH-2**: 權限校驗必須以伺服器端為準。
  - 禁止僅在客戶端做密碼比對或權限過濾。

---

## 4. MASVS-NETWORK (網路傳輸安全)
> 確保所有網路通訊受到端對端 TLS 加密保護，抵禦中間人攻擊 (MITM)。

- **MASVS-NETWORK-1**: 全量強制採用 HTTPS (TLS 1.2+)。
  - 設定 `android:usesCleartextTraffic="false"`。
  - 禁止在 Release 環境中使用空實作的 `X509TrustManager` 或 `ALLOW_ALL_HOSTNAME_VERIFIER`。
- **MASVS-NETWORK-2**: 關鍵端點實施憑證綁定 (Certificate Pinning)。
  - 配置 `res/xml/network_security_config.xml` 指定 `<pin-set>` 或使用 OkHttp `CertificatePinner`。

---

## 5. MASVS-PLATFORM (Android 平台互動與 IPC)
> 確保四大組件、Intent、Deep Links 與 IPC 通訊受到妥善存取控制。

- **MASVS-PLATFORM-1**: IPC 組件嚴格遵循最小權限原則。
  - 非供外部呼叫之 Activity/Service/Receiver 設定 `android:exported="false"`。
  - 需暴露之組件必須配置自訂 `android:permission` (且 `protectionLevel="signature"`)。
- **MASVS-PLATFORM-2**: Deep Link 與 Web URL 安全處理。
  - 升級為 Android App Links (啟用 `android:autoVerify="true"` 與 `assetlinks.json`)。
  - 對收到的 Query 參數做嚴格型態與白名單過濾，防範 Intent Injection。
- **MASVS-PLATFORM-3**: PendingIntent 標記不變性。
  - 預設指定 `PendingIntent.FLAG_IMMUTABLE`。
- **MASVS-PLATFORM-4**: WebView 安全加固。
  - 關閉跨域檔案訪問 (`allowUniversalAccessFromFileURLs=false`)。
  - 若開啟 `javaScriptEnabled=true`，必須限制僅能載入特定網域名稱。

---

## 6. MASVS-CODE (代碼品質與編譯加固)
> 確保發布的二進位檔案經過最佳化、混淆與加固。

- **MASVS-CODE-1**: 關閉所有除錯功能。
  - 禁止在 Manifest 中保留 `android:debuggable="true"`。
- **MASVS-CODE-2**: 啟用 R8 / ProGuard 代碼混淆與資源壓縮。
  - `minifyEnabled true`, `shrinkResources true`。
  - 配置 `-assumenosideeffects` 移除所有 `Log.d` / `Log.v`。
- **MASVS-CODE-3**: 敏感建構憑證隔離。
  - 禁止在 `build.gradle` 中寫死簽名金鑰密碼。

---

## 7. MASVS-RESILIENCE (反逆向與環境防禦)
> 提供運行時環境完整性檢查與防禦。

- **MASVS-RESILIENCE-1**: Root 越獄檢測與模擬器檢測。
- **MASVS-RESILIENCE-2**: 動態除錯防護 (`Debug.isDebuggerConnected()`) 與 Frida / Xposed 注入偵測。
- **MASVS-RESILIENCE-3**: 應用程式簽名完整性防篡改校驗。
