#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android OWASP MASVS (Mobile Application Security Verification Standard) v2.0 Scanner
Designed for AI Agents (Antigravity / Claude Code) and Android Engineers.

Usage:
    python3 masvs_scanner.py --project-path /path/to/android/project [--output-dir /path/to/output]
"""

import os
import sys
import re
import json
import html
import argparse
import xml.etree.ElementTree as ET
from datetime import datetime

# ANSI Terminal Colors
RED = "\033[91m"
YELLOW = "\033[93m"
GREEN = "\033[92m"
BLUE = "\033[94m"
CYAN = "\033[96m"
BOLD = "\033[1m"
RESET = "\033[0m"

SEVERITY_WEIGHTS = {
    "CRITICAL": 25,
    "HIGH": 10,
    "MEDIUM": 4,
    "LOW": 1,
    "INFO": 0
}

MASVS_CATEGORIES = {
    "MASVS-STORAGE": "資料儲存與隱私保護 (Data Storage & Privacy)",
    "MASVS-CRYPTO": "密碼學安全 (Cryptography)",
    "MASVS-AUTH": "身分驗證與授權 (Authentication & Authorization)",
    "MASVS-NETWORK": "網路傳輸安全 (Network Communication)",
    "MASVS-PLATFORM": "Android 平台互動與 IPC (Platform Interaction)",
    "MASVS-CODE": "代碼品質與編譯加固 (Code Quality & Build Hardening)",
    "MASVS-RESILIENCE": "反編譯與環境彈性防禦 (Resilience Against Reverse Engineering)"
}

class MASVSScanner:
    def __init__(self, project_path, output_dir=None):
        self.project_path = os.path.abspath(project_path)
        self.output_dir = os.path.abspath(output_dir) if output_dir else os.path.join(self.project_path, ".masvs_reports", datetime.now().strftime("%Y%m%d_%H%M%S"))
        self.findings = []
        self.scanned_files = 0
        os.makedirs(self.output_dir, exist_ok=True)

    def add_finding(self, rule_id, category, severity, title, file_path, line_no, snippet, description, remediation):
        rel_path = os.path.relpath(file_path, self.project_path) if file_path else "Project Configuration"
        self.findings.append({
            "id": rule_id,
            "category": category,
            "severity": severity,
            "title": title,
            "file": rel_path,
            "line": line_no,
            "snippet": snippet.strip() if snippet else "",
            "description": description,
            "remediation": remediation
        })

    def scan(self):
        print(f"{CYAN}{BOLD}[*] 開始 OWASP MASVS v2.0 深度資安審查: {self.project_path}{RESET}")
        
        self.scan_manifests()
        self.scan_gradle_files()
        self.scan_source_code()
        
        print(f"{GREEN}[✓] 掃描完成！共檢測 {self.scanned_files} 個檔案，發現 {len(self.findings)} 項資安指標。{RESET}")
        return self.generate_reports()

    # -------------------------------------------------------------
    # 1. AndroidManifest.xml 審查 (MASVS-STORAGE / PLATFORM / CODE)
    # -------------------------------------------------------------
    def scan_manifests(self):
        for root, _, files in os.walk(self.project_path):
            if "build" in root.split(os.sep):
                continue
            for f in files:
                if f == "AndroidManifest.xml":
                    manifest_path = os.path.join(root, f)
                    self.scanned_files += 1
                    self._audit_manifest(manifest_path)

    def _audit_manifest(self, path):
        try:
            tree = ET.parse(path)
            root = tree.getroot()
            app = root.find("application")
            
            with open(path, "r", encoding="utf-8", errors="ignore") as fp:
                manifest_lines = fp.readlines()

            def find_line(pattern):
                for idx, line in enumerate(manifest_lines):
                    if pattern in line:
                        return idx + 1, line
                return 1, ""

            if app is not None:
                # Rule: MASVS-STORAGE-1: allowBackup
                backup = app.attrib.get("{http://schemas.android.com/apk/res/android}allowBackup")
                if backup == "true" or backup is None:
                    line_no, snip = find_line("allowBackup")
                    self.add_finding(
                        rule_id="MASVS-STORAGE-1.1",
                        category="MASVS-STORAGE",
                        severity="MEDIUM",
                        title="允許 ADB 備份導出應用私有資料 (allowBackup)",
                        file_path=path,
                        line_no=line_no,
                        snippet=snip if snip else '<application android:allowBackup="true" ...>',
                        description="當 android:allowBackup 未顯式設為 false 時，攻擊者可透過 USB 連線下達 `adb backup` 導出 App 內部資料庫與私密 Token。",
                        remediation='在 <application> 標籤中設定 android:allowBackup="false" 或配置自訂 <data-extraction-rules>。'
                    )

                # Rule: MASVS-CODE-1: debuggable
                debuggable = app.attrib.get("{http://schemas.android.com/apk/res/android}debuggable")
                if debuggable == "true":
                    line_no, snip = find_line("debuggable")
                    self.add_finding(
                        rule_id="MASVS-CODE-1.1",
                        category="MASVS-CODE",
                        severity="CRITICAL",
                        title="Manifest 硬編碼開啟除錯模式 (android:debuggable)",
                        file_path=path,
                        line_no=line_no,
                        snippet=snip,
                        description="硬編碼 debuggable=true 會允許任意使用者使用 JDWP 除錯器附加進程、記憶體提取並執行任意代碼。",
                        remediation="移除 Manifest 中的 android:debuggable 屬性，交由 Gradle buildType (debug/release) 自動控制。"
                    )

                # Rule: MASVS-NETWORK-1: cleartext traffic
                cleartext = app.attrib.get("{http://schemas.android.com/apk/res/android}usesCleartextTraffic")
                if cleartext == "true":
                    line_no, snip = find_line("usesCleartextTraffic")
                    self.add_finding(
                        rule_id="MASVS-NETWORK-1.1",
                        category="MASVS-NETWORK",
                        severity="HIGH",
                        title="全域允許明文 HTTP 網路連線 (usesCleartextTraffic)",
                        file_path=path,
                        line_no=line_no,
                        snippet=snip,
                        description="開啟 usesCleartextTraffic=true 會允許明文 HTTP 連線，容易遭受公共 Wi-Fi 中間人攻擊 (MITM) 竊聽或竄改內容。",
                        remediation='設定 android:usesCleartextTraffic="false" 並強制全面使用 HTTPS，或配置 res/xml/network_security_config.xml。'
                    )

                # Rule: MASVS-PLATFORM-1: Exported Components
                for tag in ["activity", "service", "receiver", "provider"]:
                    for comp in app.findall(tag):
                        name = comp.attrib.get("{http://schemas.android.com/apk/res/android}name", "Unknown")
                        exported = comp.attrib.get("{http://schemas.android.com/apk/res/android}exported")
                        permission = comp.attrib.get("{http://schemas.android.com/apk/res/android}permission")
                        has_intent_filter = comp.find("intent-filter") is not None

                        # Check if it's main launcher
                        is_launcher = False
                        is_deep_link = False
                        if has_intent_filter:
                            for ifilter in comp.findall("intent-filter"):
                                for action in ifilter.findall("action"):
                                    act_name = action.attrib.get("{http://schemas.android.com/apk/res/android}name", "")
                                    if "android.intent.action.MAIN" in act_name:
                                        is_launcher = True
                                for data in ifilter.findall("data"):
                                    if "{http://schemas.android.com/apk/res/android}scheme" in data.attrib:
                                        is_deep_link = True

                        # Evaluation
                        is_exported_val = (exported == "true") or (exported is None and has_intent_filter)
                        if is_exported_val and not is_launcher and not permission:
                            line_no, snip = find_line(name.split(".")[-1])
                            self.add_finding(
                                rule_id="MASVS-PLATFORM-1.1",
                                category="MASVS-PLATFORM",
                                severity="HIGH" if tag in ["service", "provider"] else "MEDIUM",
                                title=f"對外暴露之無權限保護組件 ({tag}: {name})",
                                file_path=path,
                                line_no=line_no,
                                snippet=snip if snip else f'<{tag} android:name="{name}" android:exported="true" />',
                                description=f"組件 {name} 設置為 exported=true 且無 custom permission 保護，同設備上的惡意 App 可發送隱式 Intent 喚醒或傳入惡意 Payload 觸發越權或崩潰。",
                                remediation=f'若非供外部呼叫，請設定 android:exported="false"；若需供特定應用呼叫，請配置 android:permission 保護。'
                            )
                        
                        # Rule: MASVS-PLATFORM-2: Deep Link without Validation
                        if is_deep_link:
                            line_no, snip = find_line("scheme")
                            self.add_finding(
                                rule_id="MASVS-PLATFORM-1.2",
                                category="MASVS-PLATFORM",
                                severity="INFO",
                                title=f"檢測到自訂 Deep Link 路由協議 ({name})",
                                file_path=path,
                                line_no=line_no,
                                snippet=snip if snip else f'<{tag} android:name="{name}"> <data android:scheme="..." />',
                                description="Deep Link 接收外部 URI 傳入之參數，若未在 Activity 內進行嚴格白名單校驗，可能導致 Intent 注入或誘導釣魚。",
                                remediation="建議升級為 Android App Links (需配置 assetlinks.json 與 autoVerify=true)，並對接收到的 Uri 參數進行正則白名單校驗。"
                            )
        except Exception as e:
            print(f"{YELLOW}[!] 解析 Manifest 異常: {path} ({e}){RESET}")

    # -------------------------------------------------------------
    # 2. Gradle 建構設定審查 (MASVS-CODE)
    # -------------------------------------------------------------
    def scan_gradle_files(self):
        for root, _, files in os.walk(self.project_path):
            if "build" in root.split(os.sep):
                continue
            for f in files:
                if f in ["build.gradle", "build.gradle.kts"]:
                    gradle_path = os.path.join(root, f)
                    self.scanned_files += 1
                    self._audit_gradle(gradle_path)

    def _audit_gradle(self, path):
        try:
            with open(path, "r", encoding="utf-8", errors="ignore") as fp:
                lines = fp.readlines()

            content = "".join(lines)

            # Check: release buildType without minifyEnabled
            if "buildTypes" in content and "release" in content:
                release_match = re.search(r"release\s*\{([^}]+)\}", content, re.DOTALL)
                if release_match:
                    release_body = release_match.group(1)
                    if "minifyEnabled false" in release_body or ("minifyEnabled true" not in release_body and "isMinifyEnabled = true" not in release_body):
                        line_no = 1
                        for idx, l in enumerate(lines):
                            if "release" in l:
                                line_no = idx + 1
                                break
                        self.add_finding(
                            rule_id="MASVS-CODE-1.2",
                            category="MASVS-CODE",
                            severity="MEDIUM",
                            title="Release 版本未啟用 R8 / ProGuard 代碼混淆與壓縮",
                            file_path=path,
                            line_no=line_no,
                            snippet="buildTypes { release { minifyEnabled false ... } }",
                            description="未啟用 minifyEnabled 會導致發布的 APK 保留原始類別名、方法名與變數名，使攻擊者極易透過 JADX 反編譯獲取業務邏輯與敏感演算法。",
                            remediation="在 release buildType 中配置 `minifyEnabled true` (或 `isMinifyEnabled = true`)，並提供有效的 `proguard-rules.pro`。"
                        )

            # Check: hardcoded keystore passwords
            for idx, line in enumerate(lines):
                if re.search(r"(storePassword|keyPassword)\s*(=|\s)\s*[\"'][^\"']+[\"']", line):
                    self.add_finding(
                        rule_id="MASVS-STORAGE-1.4",
                        category="MASVS-STORAGE",
                        severity="HIGH",
                        title="Gradle 腳本中硬編碼簽名金鑰密碼 (storePassword / keyPassword)",
                        file_path=path,
                        line_no=idx + 1,
                        snippet=line,
                        description="在版本控制的 build.gradle 中明文寫死金鑰密碼，一旦代碼開源或外洩，攻擊者即可偽造 App 簽名發布惡意更新包。",
                        remediation="將簽署密碼遷移至本機 `local.properties` 或 CI/CD 環境變數中讀取 (例如 `System.getenv('KEY_PASSWORD')`)。"
                    )

            # Check: outdated minSdkVersion
            min_sdk_match = re.search(r"minSdk(Version)?\s*(=|\s)\s*(\d+)", content)
            if min_sdk_match:
                min_sdk = int(min_sdk_match.group(3))
                if min_sdk < 24:
                    line_no = 1
                    for idx, l in enumerate(lines):
                        if min_sdk_match.group(0) in l:
                            line_no = idx + 1
                            break
                    self.add_finding(
                        rule_id="MASVS-CODE-1.4",
                        category="MASVS-CODE",
                        severity="LOW",
                        title=f"minSdkVersion ({min_sdk}) 低於 Android 7.0 (API 24)",
                        file_path=path,
                        line_no=line_no,
                        snippet=min_sdk_match.group(0),
                        description="低於 API 24 的系統不支援 Android 現代安全性機制（如安全 Network Security Config 與改進型 Keystore 權限隔離）。",
                        remediation="若商業需求允許，建議將 minSdkVersion 提升至 24 以上。"
                    )

        except Exception as e:
            print(f"{YELLOW}[!] 解析 Gradle 異常: {path} ({e}){RESET}")

    # -------------------------------------------------------------
    # 3. 原始碼靜態特徵與污點分析 (Kotlin & Java)
    # -------------------------------------------------------------
    def scan_source_code(self):
        code_rules = [
            # MASVS-CRYPTO
            {
                "id": "MASVS-CRYPTO-1.1",
                "category": "MASVS-CRYPTO",
                "severity": "CRITICAL",
                "title": "使用不安全之加密演算法 (DES / 3DES / RC4 / AES-ECB)",
                "regex": r'Cipher\.getInstance\s*\(\s*["\'](DES|DESede|RC4|AES/ECB|Blowfish)[\w/]*["\']\s*\)',
                "description": "ECB 模式未引入初始化向量 (IV)，相同明文塊將產生相同密文，存在模式洩漏；DES/RC4 則已被現代密碼學證實易遭破解。",
                "remediation": '建議改用 AES/GCM/NoPadding (authenticated encryption) 或 AndroidX Security-Crypto 庫封裝之 EncryptedSharedPreferences。'
            },
            {
                "id": "MASVS-CRYPTO-1.2",
                "category": "MASVS-CRYPTO",
                "severity": "MEDIUM",
                "title": "使用不安全之單向雜湊演算法 (MD5 / SHA-1)",
                "regex": r'MessageDigest\.getInstance\s*\(\s*["\'](MD5|SHA-?1)["\']\s*\)',
                "description": "MD5 與 SHA-1 存在嚴重的碰撞漏洞 (Collision Attack)，不可用於密碼存儲或敏感數位簽名校驗。",
                "remediation": '改用 MessageDigest.getInstance("SHA-256") 或 SHA-512，密碼存儲請使用 Argon2id 或 PBKDF2WithHmacSHA256 加鹽雜湊。'
            },
            {
                "id": "MASVS-CRYPTO-1.3",
                "category": "MASVS-CRYPTO",
                "severity": "MEDIUM",
                "title": "使用不安全之隨機數產生器 (java.util.Random)",
                "regex": r'import\s+java\.util\.Random|new\s+Random\s*\(|Random\s*\(\s*\)\.next',
                "description": "java.util.Random 為線性同餘偽隨機數產生器，其輸出序列完全可預測，嚴禁用於金鑰、Token 或重放防護 Nonce 之生成。",
                "remediation": '全面替換為 `java.security.SecureRandom()`，具備密碼學安全等級 (CSPRNG)。'
            },
            {
                "id": "MASVS-CRYPTO-1.4",
                "category": "MASVS-CRYPTO",
                "severity": "HIGH",
                "title": "硬編碼對稱加密金鑰 (SecretKeySpec)",
                "regex": r'SecretKeySpec\s*\(\s*["\'][^"\']+["\']\.toByteArray|SecretKeySpec\s*\(\s*new\s+byte\[\]\s*\{[^}]+\}',
                "description": "硬編碼在代碼庫內的對稱金鑰可輕易透過 JADX 或 Strings 指令提取，導致整個加密機制形同虛設。",
                "remediation": "使用 Android Keystore 系統動態生成並保護金鑰 (`KeyGenParameterSpec.Builder(..., KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)`)。"
            },

            # MASVS-STORAGE
            {
                "id": "MASVS-STORAGE-1.2",
                "category": "MASVS-STORAGE",
                "severity": "HIGH",
                "title": "不安全的檔案權限設置 (MODE_WORLD_READABLE / WRITEABLE)",
                "regex": r'MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE',
                "description": "以全域可讀/可寫模式創建的私有檔案，可被設備上任何其他 App 讀取或竄改，已被 Android 官方正式廢棄。",
                "remediation": "使用 `Context.MODE_PRIVATE`，跨進程共享請改用具備嚴格權限限制的 FileProvider。"
            },
            {
                "id": "MASVS-STORAGE-1.3",
                "category": "MASVS-STORAGE",
                "severity": "MEDIUM",
                "title": "使用外部公共儲存區存儲敏感資料 (External Storage)",
                "regex": r'Environment\.getExternalStorageDirectory|Environment\.getExternalStoragePublicDirectory',
                "description": "寫入外部共享存儲的檔案對具備存儲權限的其他應用可見，容易遭受資料監聽或檔案替換 (Content Spoofing)。",
                "remediation": "優先使用內部私有目錄 `context.filesDir` 或 `context.noBackupFilesDir`，必要時以 SQLCipher / Jetpack Security 進行端對端加密。"
            },
            {
                "id": "MASVS-STORAGE-1.5",
                "category": "MASVS-STORAGE",
                "severity": "LOW",
                "title": "潛在敏感資訊日誌輸出 (Log.d/i/v/e)",
                "regex": r'Log\.[vdiew]\s*\([^)]*(password|token|secret|apiKey|authorization|auth_token)',
                "description": "在 Logcat 中打印密碼、Token 或 API Key，若在 Release 版本未被移除，其他具備 READ_LOGS (或除錯連接) 的人員可直接竊取。",
                "remediation": "在 ProGuard/R8 規則中添加 `-assumenosideeffects class android.util.Log { *; }` 自動在 Release 中剝離所有 Logcat 調用。"
            },

            # MASVS-NETWORK
            {
                "id": "MASVS-NETWORK-1.2",
                "category": "MASVS-NETWORK",
                "severity": "CRITICAL",
                "title": "空實作 X509TrustManager 信任所有 SSL/TLS 憑證",
                "regex": r'class\s+\w+\s+implements\s+X509TrustManager|checkServerTrusted\s*\(.*?\)\s*\{\s*\}|getAcceptedIssuers\s*\(.*?\)\s*\{\s*return\s+(null|new\s+X509Certificate\[0\])\s*;\s*\}',
                "description": "覆寫 X509TrustManager 且內部清空校驗邏輯，會無條件信任任何自簽署憑證，導致所有 HTTPS 通訊在中間人攻擊 (MITM) 面前無遮無掩。",
                "remediation": "嚴禁在生產環境中自定義空 TrustManager。使用系統預設憑證體系，或在 `res/xml/network_security_config.xml` 中配置 Pinning。"
            },
            {
                "id": "MASVS-NETWORK-1.3",
                "category": "MASVS-NETWORK",
                "severity": "CRITICAL",
                "title": "HostnameVerifier 信任所有網域名稱",
                "regex": r'HostnameVerifier\s*\{\s*_,?\s*_?\s*->\s*true\s*\}|ALLOW_ALL_HOSTNAME_VERIFIER',
                "description": "無條件返回 true 的 HostnameVerifier 破壞了 SSL/TLS 的主機名校驗，任何有效憑證都可用於劫持任意網域名稱的流量。",
                "remediation": "移除自訂的 Allow-All HostnameVerifier，依賴 OkHttp / HttpsURLConnection 預設的嚴格主機名驗證。"
            },

            # MASVS-PLATFORM
            {
                "id": "MASVS-PLATFORM-1.3",
                "category": "MASVS-PLATFORM",
                "severity": "MEDIUM",
                "title": "WebView 啟用 JavaScript 執行 (setJavaScriptEnabled)",
                "regex": r'settings\.javaScriptEnabled\s*=\s*true|\.setJavaScriptEnabled\s*\(\s*true\s*\)',
                "description": "若 WebView 載入不受信任的第三方 URL 或允許使用者自訂網址，開啟 JavaScript 會大幅增加跨站腳本攻擊 (XSS) 風險。",
                "remediation": "嚴格過濾 WebView 載入 URL 的網域名稱白名單，非必要關閉 FileAccess 與 ContentAccess (`settings.allowFileAccess = false`)。"
            },
            {
                "id": "MASVS-PLATFORM-1.4",
                "category": "MASVS-PLATFORM",
                "severity": "HIGH",
                "title": "WebView 開啟跨域檔案訪問 (setAllowUniversalAccessFromFileURLs)",
                "regex": r'setAllowUniversalAccessFromFileURLs\s*\(\s*true\s*\)|setAllowFileAccessFromFileURLs\s*\(\s*true\s*\)',
                "description": "允許從本機 file:// URL 發起跨域請求，會導致攻擊者透過同源繞過漏洞竊取應用內部資料庫與私密檔案。",
                "remediation": "明確設置 `settings.allowUniversalAccessFromFileURLs = false` 與 `settings.allowFileAccessFromFileURLs = false`。"
            },
            {
                "id": "MASVS-PLATFORM-1.5",
                "category": "MASVS-PLATFORM",
                "severity": "MEDIUM",
                "title": "Mutable PendingIntent 未指定明確接收組件",
                "regex": r'PendingIntent\.(getActivity|getBroadcast|getService)\([^)]*PendingIntent\.FLAG_MUTABLE',
                "description": "在 Android 12+ (API 31+) 上，可變的 PendingIntent (FLAG_MUTABLE) 若包含隱式 Intent，接收方可竄改 Intent 的 Action 或 Extras 實施提權攻擊。",
                "remediation": "若無須外部修飾，優先使用 `PendingIntent.FLAG_IMMUTABLE`；若必須可變，確保 Intent 指明了明確的 ComponentName。"
            },

            # SQL Injection
            {
                "id": "MASVS-CODE-1.5",
                "category": "MASVS-CODE",
                "severity": "HIGH",
                "title": "潛在 SQL 注入漏洞 (rawQuery / execSQL 字串拼接)",
                "regex": r'(rawQuery|execSQL)\s*\(\s*["\'][^"\']*\s*\+\s*\w+',
                "description": "使用未經參數化處理的字串拼接構造 SQL 查詢，若拼入外部使用者輸入，將引發本地 SQLite 注入漏洞。",
                "remediation": "使用參數化查詢 `rawQuery(sql, arrayOf(param))` 或全面遷移至 Room ORM 享受編譯期 SQL 語法與防注入檢查。"
            }
        ]

        # 遍歷專案所有 source 檔案
        for root, _, files in os.walk(self.project_path):
            if any(skip in root.split(os.sep) for skip in ["build", ".gradle", ".git", ".masvs_reports"]):
                continue
            for f in files:
                if f.endswith((".kt", ".java")):
                    self.scanned_files += 1
                    file_path = os.path.join(root, f)
                    self._audit_code_file(file_path, code_rules)

    def _audit_code_file(self, path, rules):
        try:
            with open(path, "r", encoding="utf-8", errors="ignore") as fp:
                lines = fp.readlines()

            for idx, line in enumerate(lines):
                stripped = line.strip()
                if stripped.startswith("//") or stripped.startswith("/*") or stripped.startswith("*"):
                    continue

                for rule in rules:
                    if re.search(rule["regex"], line):
                        self.add_finding(
                            rule_id=rule["id"],
                            category=rule["category"],
                            severity=rule["severity"],
                            title=rule["title"],
                            file_path=path,
                            line_no=idx + 1,
                            snippet=line,
                            description=rule["description"],
                            remediation=rule["remediation"]
                        )
        except Exception as e:
            print(f"{YELLOW}[!] 讀取代碼檔案異常: {path} ({e}){RESET}")

    # -------------------------------------------------------------
    # 4. 報告產出 (Console, JSON, Interactive HTML)
    # -------------------------------------------------------------
    def calculate_score(self):
        deductions = 0
        counts = {"CRITICAL": 0, "HIGH": 0, "MEDIUM": 0, "LOW": 0, "INFO": 0}
        for f in self.findings:
            sev = f["severity"]
            counts[sev] = counts.get(sev, 0) + 1
            deductions += SEVERITY_WEIGHTS.get(sev, 0)

        score = max(0, 100 - deductions)
        
        if score >= 90:
            rating = "EXCELLENT (A+)"
            risk_level = "LOW"
        elif score >= 75:
            rating = "GOOD (B)"
            risk_level = "MODERATE"
        elif score >= 60:
            rating = "NEEDS_IMPROVEMENT (C)"
            risk_level = "ELEVATED"
        else:
            rating = "POOR (F)"
            risk_level = "CRITICAL"

        return score, rating, risk_level, counts

    def generate_reports(self):
        score, rating, risk_level, counts = self.calculate_score()

        # Terminal output
        print("\n" + "=" * 80)
        print(f"{BOLD}OWASP MASVS v2.0 審查總結 (Project: {os.path.basename(self.project_path)}){RESET}")
        print("=" * 80)
        print(f"綜合合規評分: {BOLD}{GREEN if score >= 80 else RED}{score} / 100{RESET} ({rating})")
        print(f"風險等級: {BOLD}{risk_level}{RESET}")
        print(f"問題統計: {RED}CRITICAL: {counts['CRITICAL']}{RESET} | {YELLOW}HIGH: {counts['HIGH']}{RESET} | MEDIUM: {counts['MEDIUM']} | LOW: {counts['LOW']} | INFO: {counts['INFO']}")
        print("-" * 80)

        for item in self.findings:
            sev_color = RED if item['severity'] in ['CRITICAL', 'HIGH'] else (YELLOW if item['severity'] == 'MEDIUM' else GREEN)
            print(f"{sev_color}[{item['severity']}]{RESET} {BOLD}{item['title']}{RESET} ({item['id']})")
            print(f"  檔案: {item['file']}:{item['line']}")
            if item['snippet']:
                print(f"  代碼: {CYAN}{item['snippet']}{RESET}")
            print()

        # Write JSON report
        json_path = os.path.join(self.output_dir, "masvs_report.json")
        report_data = {
            "project": os.path.basename(self.project_path),
            "timestamp": datetime.now().isoformat(),
            "score": score,
            "rating": rating,
            "risk_level": risk_level,
            "scanned_files_count": self.scanned_files,
            "counts": counts,
            "findings": self.findings
        }
        with open(json_path, "w", encoding="utf-8") as fp:
            json.dump(report_data, fp, ensure_ascii=False, indent=2)

        # Write Interactive HTML report
        html_path = os.path.join(self.output_dir, "index.html")
        self._generate_html_dashboard(html_path, report_data)

        print(f"{GREEN}[✓] JSON 報告已產出: {json_path}{RESET}")
        print(f"{GREEN}[✓] 視覺化儀表板已產出: {html_path}{RESET}")
        print("=" * 80 + "\n")

        return {
            "score": score,
            "rating": rating,
            "risk_level": risk_level,
            "json_path": json_path,
            "html_path": html_path,
            "counts": counts,
            "findings_count": len(self.findings)
        }

    def _generate_html_dashboard(self, path, data):
        score = data["score"]
        score_color = "#10b981" if score >= 85 else ("#f59e0b" if score >= 70 else "#ef4444")
        
        cards_html = ""
        for idx, f in enumerate(data["findings"]):
            badge_color = {
                "CRITICAL": "#dc2626",
                "HIGH": "#ea580c",
                "MEDIUM": "#d97706",
                "LOW": "#2563eb",
                "INFO": "#64748b"
            }.get(f["severity"], "#64748b")

            cards_html += f"""
            <div class="finding-card" data-severity="{f['severity']}" data-category="{f['category']}">
                <div class="card-header">
                    <span class="badge" style="background-color: {badge_color};">{f['severity']}</span>
                    <span class="rule-id">{f['id']}</span>
                    <span class="category-tag">{f['category']}</span>
                    <h3 class="finding-title">{html.escape(f['title'])}</h3>
                </div>
                <div class="card-body">
                    <div class="meta-location">
                        <strong>檔案路徑:</strong> <code>{html.escape(f['file'])}:{f['line']}</code>
                    </div>
                    {f'<pre class="code-snippet"><code>{html.escape(f["snippet"])}</code></pre>' if f["snippet"] else ''}
                    <div class="description">
                        <p>{html.escape(f['description'])}</p>
                    </div>
                    <div class="remediation-box">
                        <strong>💡 防禦與修復建議:</strong>
                        <p>{html.escape(f['remediation'])}</p>
                    </div>
                </div>
            </div>
            """

        html_content = f"""<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OWASP MASVS v2.0 Android 資安審查儀表板</title>
    <style>
        :root {{
            --bg-primary: #0f172a;
            --bg-secondary: #1e293b;
            --bg-card: #334155;
            --text-primary: #f8fafc;
            --text-secondary: #94a3b8;
            --accent: #38bdf8;
            --border: #475569;
        }}
        * {{ box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }}
        body {{ background-color: var(--bg-primary); color: var(--text-primary); padding: 24px; }}
        .container {{ max-width: 1200px; margin: 0 auto; }}
        header {{ display: flex; justify-content: space-between; align-items: center; padding-bottom: 24px; border-bottom: 1px solid var(--border); margin-bottom: 24px; }}
        h1 {{ font-size: 24px; color: var(--accent); }}
        .metrics-grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }}
        .metric-card {{ background-color: var(--bg-secondary); padding: 20px; border-radius: 8px; border: 1px solid var(--border); text-align: center; }}
        .metric-val {{ font-size: 36px; font-weight: bold; margin-top: 8px; }}
        .controls {{ display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }}
        .filter-btn {{ background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text-primary); padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 14px; transition: all 0.2s; }}
        .filter-btn.active, .filter-btn:hover {{ background: var(--accent); color: #000; font-weight: bold; }}
        .finding-card {{ background-color: var(--bg-secondary); border-radius: 8px; border: 1px solid var(--border); margin-bottom: 16px; overflow: hidden; }}
        .card-header {{ padding: 16px 20px; background-color: rgba(255,255,255,0.02); display: flex; align-items: center; gap: 12px; border-bottom: 1px solid rgba(255,255,255,0.05); }}
        .badge {{ padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; color: #fff; }}
        .rule-id {{ font-family: monospace; color: var(--accent); font-size: 13px; }}
        .category-tag {{ background: rgba(255,255,255,0.1); padding: 3px 8px; border-radius: 4px; font-size: 12px; color: var(--text-secondary); }}
        .finding-title {{ font-size: 16px; font-weight: 600; }}
        .card-body {{ padding: 20px; }}
        .meta-location {{ margin-bottom: 12px; font-size: 14px; color: var(--text-secondary); }}
        .meta-location code {{ color: var(--accent); font-family: monospace; }}
        .code-snippet {{ background-color: #0b1120; padding: 12px; border-radius: 6px; overflow-x: auto; margin-bottom: 12px; border-left: 3px solid var(--accent); }}
        .code-snippet code {{ color: #e2e8f0; font-family: "JetBrains Mono", Consolas, monospace; font-size: 13px; }}
        .description {{ margin-bottom: 16px; font-size: 14px; line-height: 1.6; color: #cbd5e1; }}
        .remediation-box {{ background-color: rgba(56, 189, 248, 0.08); border: 1px solid rgba(56, 189, 248, 0.2); padding: 14px; border-radius: 6px; font-size: 14px; line-height: 1.5; }}
        .remediation-box strong {{ color: var(--accent); }}
    </style>
</head>
<body>
    <div class="container">
        <header>
            <div>
                <h1>🛡️ OWASP MASVS v2.0 Android 資安審查報告</h1>
                <p style="color: var(--text-secondary); margin-top: 4px;">專案: {data['project']} | 審查時間: {data['timestamp']}</p>
            </div>
            <div style="text-align: right;">
                <span style="font-size: 13px; color: var(--text-secondary);">掃描檔案總數</span>
                <p style="font-size: 20px; font-weight: bold;">{data['scanned_files_count']} Files</p>
            </div>
        </header>

        <div class="metrics-grid">
            <div class="metric-card">
                <div>MASVS 合規分數</div>
                <div class="metric-val" style="color: {score_color};">{score} <span style="font-size: 16px;">/ 100</span></div>
            </div>
            <div class="metric-card">
                <div>風險評級</div>
                <div class="metric-val" style="color: {score_color};">{data['risk_level']}</div>
            </div>
            <div class="metric-card">
                <div>嚴重缺陷 (Critical)</div>
                <div class="metric-val" style="color: #dc2626;">{data['counts']['CRITICAL']}</div>
            </div>
            <div class="metric-card">
                <div>高危漏洞 (High)</div>
                <div class="metric-val" style="color: #ea580c;">{data['counts']['HIGH']}</div>
            </div>
            <div class="metric-card">
                <div>中低危與建議</div>
                <div class="metric-val" style="color: #d97706;">{data['counts']['MEDIUM'] + data['counts']['LOW'] + data['counts']['INFO']}</div>
            </div>
        </div>

        <div class="controls">
            <button class="filter-btn active" onclick="filterSev('ALL')">全部問題 ({len(data['findings'])})</button>
            <button class="filter-btn" onclick="filterSev('CRITICAL')">Critical ({data['counts']['CRITICAL']})</button>
            <button class="filter-btn" onclick="filterSev('HIGH')">High ({data['counts']['HIGH']})</button>
            <button class="filter-btn" onclick="filterSev('MEDIUM')">Medium ({data['counts']['MEDIUM']})</button>
            <button class="filter-btn" onclick="filterSev('LOW')">Low ({data['counts']['LOW']})</button>
            <button class="filter-btn" onclick="filterSev('INFO')">Info ({data['counts']['INFO']})</button>
        </div>

        <div class="findings-list">
            {cards_html if cards_html else '<p style="text-align:center; padding: 40px; color: var(--text-secondary);">恭喜！未檢測到任何違反 OWASP MASVS 之資安缺陷。</p>'}
        </div>
    </div>

    <script>
        function filterSev(sev) {{
            document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
            event.target.classList.add('active');
            document.querySelectorAll('.finding-card').forEach(card => {{
                if (sev === 'ALL' || card.dataset.severity === sev) {{
                    card.style.display = 'block';
                }} else {{
                    card.style.display = 'none';
                }}
            }});
        }}
    </script>
</body>
</html>
"""
        with open(path, "w", encoding="utf-8") as fp:
            fp.write(html_content)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Android OWASP MASVS v2.0 Security Scanner")
    parser.add_argument("--project-path", "-p", default=".", help="Path to Android project root")
    parser.add_argument("--output-dir", "-o", default=None, help="Output directory for reports")
    args = parser.parse_args()

    scanner = MASVSScanner(args.project_path, args.output_dir)
    scanner.scan()
