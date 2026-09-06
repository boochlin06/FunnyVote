---
name: skills-security-check
description: >-
  Audits AI agent skills and code directories for security risks, hardcoded credentials,
  arbitrary execution, obfuscation, and supply chain threats using static analysis and AI reasoning.
  Generates interactive visual security dashboards and actionable audit prompts.
  Use whenever the user asks to "check skills security", "scan skills", "audit security",
  "資安審查", "檢查技能安全性", or "掃描資安漏洞".
---

# Skill: Skills-Security-Check (AI Agent 技能資安審查)

**CRITICAL INSTRUCTION FOR AI AGENTS:**
You are NOT just a command-runner. You are the **Lead Security Analyst**.
This tool provides the static regex *data*, but YOU provide the contextual *intelligence* to eliminate false positives and evaluate true threat vectors.

---

## ⚡️ 核心 3 步工作流 (The 3-Step Agentic Workflow)

當使用者請求進行技能或專案資安掃描時，請務必遵循以下標準流程：

### Step 1: 執行靜態掃描 (Data Collection)
執行掃描器收集原始指標並生成各技能的 Audit Prompts：
```bash
python3 /Users/heaton/.gemini/config/plugins/skills-security-check/skills/skills-security-check/scripts/scan_skills.py --root <TARGET_DIR>
```

常見掃描目標位置：
* **全部全局外掛技能**：`/Users/heaton/.gemini/config/plugins`
* **Antigravity 內建技能**：`/Users/heaton/.gemini/antigravity/builtin/skills`
* **當前專案工作區**：`./.agents/skills` 或當前專案根目錄

*執行後將在 `reports/YYYYMMDD_HHMMSS/` 目錄下生成 `index.html`、`data.json` 與 `prompts/*_audit_prompt.txt`。*

### Step 2: AI 深度研判 (MANDATORY AI ANALYSIS)
靜態正則檢測存在不可避免的誤報（例如：正常的 API 客戶端範例、合法的 curl 下載文檔）。
**作為資安分析專家，你必須親自檢驗生成的提示詞：**

1. 檢查 `reports/YYYYMMDD_HHMMSS/prompts/` 內生成的 `*_audit_prompt.txt`。
2. 評估上下文意圖（區分「惡意後門」與「正常業務代碼」）。
3. 針對具體技能目錄建立 `audit.json`，格式如下：
```json
{
  "summary": "模組核心功能與資安摘要",
  "risk_level": "low", // 可選: "low" | "medium" | "high"
  "reasoning": "為什麼調整評級的技術論據（如：此處 eval 僅用於數學運算，非遠端代碼執行）",
  "recommendation": "具體的安全加固建議（如：建議將金鑰抽離至環境變數）"
}
```

### Step 3: 重新整合並發布視覺化儀表板 (Integrate & Present)
再次執行掃描器，腳本將自動讀取 `audit.json`，將 AI 的專業裁決融合成最終的金標儀表板：
```bash
python3 /Users/heaton/.gemini/config/plugins/skills-security-check/skills/skills-security-check/scripts/scan_skills.py --root <TARGET_DIR>
```
將生成的 `file:///.../reports/YYYYMMDD_HHMMSS/index.html` 連結提供給使用者點擊查閱。

---

## 偵測維度與威脅模型

| 類別 (Category) | 偵測指標 (Signatures) | 潛在攻擊情境 |
| :--- | :--- | :--- |
| 🔑 **敏感憑證與金鑰** | `api_key`, `private_key`, `token`, `bearer`, `.aws/credentials`, `~/.ssh` | 憑證洩漏、雲端資源被盜刷 |
| ⚠️ **危險指令與代碼執行** | `curl \| sh`, `subprocess.Popen`, `os.system`, `child_process.exec`, `eval` | 遠端代碼執行 (RCE)、反向 Shell 後門 |
| 🎭 **混淆與防禦規避** | `base64`, `rot13`, `fromCharCode`, `anti_analysis` (sleep/delay) | 隱藏惡意 Payload、規避沙盒檢測 |
| 📦 **供應鏈依賴安裝** | `npm install`, `pip install`, `brew install`, `go get` | 惡意第三方依賴、Typosquatting 投毒 |
| 🌐 **網路外聯活動** | 外部 HTTP/HTTPS URLs、硬編碼 IP 位址 | 資料外洩 (Data Exfiltration)、C2 外部通訊 |

---

## 命令列參數

* `--root`：待掃描之根目錄（預設為當前目錄）。
* `--out`：自訂 HTML 報告輸出路徑（選填，預設將自動於 `reports/時間戳記/` 生成）。
