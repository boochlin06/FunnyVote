---
name: android-masvs-security
description: >-
  Audits Android codebases and APKs against OWASP MASVS v2.0 (Mobile Application Security Verification Standard).
  Performs static AST and pattern analysis on Manifest, Gradle, Kotlin/Java code, and Room/SQLite.
  Detects exported components, insecure storage, cleartext traffic, weak cryptography, insecure WebViews,
  mutable PendingIntents, and hardcoded secrets. Use whenever the user asks to "audit Android security",
  "MASVS check", "Android 資安掃描", "檢查 Android 安全性", or "OWASP MASVS 審查".
---

# Skill: Android OWASP MASVS Security Audit (安卓資安標準審查)

本 Skill 依據 **OWASP MASVS (Mobile Application Security Verification Standard) v2.0** 規範，針對 Android 專案進行全維度靜態資安審計，涵蓋 Storage、Crypto、Network、Platform IPC、Code Quality 等 7 大領域。

---

## ⚡️ 執行標準工作流

### Step 1: 執行自動化靜態掃描
在專案根目錄下執行 MASVS 掃描引擎：

```bash
python3 .agents/skills/android-masvs-security/scripts/masvs_scanner.py --project-path <ANDROID_PROJECT_ROOT>
```

- 掃描範圍包含：`AndroidManifest.xml`、`build.gradle(.kts)`、所有 `.kt` 與 `.java` 源碼。
- 產出檔案位於 `<PROJECT>/.masvs_reports/YYYYMMDD_HHMMSS/`：
  - `masvs_report.json`：結構化風險資料（供 Agent 或 CI/CD 解析）。
  - `index.html`：視覺化互動儀表板。

### Step 2: 依據報告進行漏洞研判與優先級排序
掃描器依據缺陷權重扣分（滿分 100）：
- **CRITICAL (-25分)**：如 `X509TrustManager` 空信任、`android:debuggable="true"`、DES/ECB 弱加密。
- **HIGH (-10分)**：如 Exported Service/Provider 無權限保護、Gradle 寫死金鑰密碼、SQL 注入、WebView 跨域存取。
- **MEDIUM (-4分)**：如 `android:allowBackup="true"`、Release 未啟用 R8 混淆、Mutable PendingIntent、MD5 雜湊。
- **LOW / INFO (-1分)**：如 Log 打印敏感詞彙、minSdkVersion 過低。

### Step 3: 提供具體代碼級修復 (Actionable Code Fixes)

請直接參考下方的標準防禦方案產出修正代碼：

---

## 🛠 常見漏洞代碼修正範例

### 1. [MASVS-STORAGE] 禁用 ADB 備份與保護私有資料
在 `AndroidManifest.xml` 中加入：
```xml
<application
    android:allowBackup="false"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="false" ...>
```

### 2. [MASVS-NETWORK] 強制 HTTPS 與禁用明文傳輸
在 `AndroidManifest.xml` 與 `res/xml/network_security_config.xml`：
```xml
<!-- AndroidManifest.xml -->
<application
    android:usesCleartextTraffic="false"
    android:networkSecurityConfig="@xml/network_security_config" ...>
```
```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### 3. [MASVS-PLATFORM] 四大組件最小暴露原則
非主入口 Activity 關閉 Export，若需接收外部廣播或深層連結，加入專屬 signature 權限：
```xml
<!-- 封閉內部組件 -->
<activity
    android:name=".ui.votedetail.VoteDetailContentActivity"
    android:exported="false" />

<!-- 對外暴露組件必須加上 permission -->
<receiver
    android:name=".receiver.MyCustomReceiver"
    android:exported="true"
    android:permission="com.heaton.funnyvote.permission.INTERNAL_EVENT" />
```

### 4. [MASVS-CRYPTO] 安全加密與隨機數
```kotlin
// ❌ 錯誤：不安全加密與隨機數
val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
val rand = java.util.Random().nextInt()

// ✅ 正確：AES-GCM 認證加密與 SecureRandom
val secureRandom = java.security.SecureRandom()
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
val iv = ByteArray(12).apply { secureRandom.nextBytes(this) }
val spec = GCMParameterSpec(128, iv)
cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
```

### 5. [MASVS-CODE] Release 啟用 R8 混淆與日誌剝離
在 `app/build.gradle`：
```groovy
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```
在 `proguard-rules.pro` 中自動剔除 Log 輸出：
```proguard
# 剝離 Logcat 敏感輸出
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}
```
