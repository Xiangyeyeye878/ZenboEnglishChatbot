# ZenboEnglishChatbot
## 專案簡介
Zenbo English Chatbot 是一個專為語音互動設計的應用，結合語音識別與 Google AI 技術，讓用戶可以與 Zenbo 機器人進行英文對話學習。  
此專案專注於語音識別、與機器人表達互動，實現更智慧的"**教學**"體驗。
<br>
## 功能特性
🎙️ **語音識別**：透過內建的語音識別技術，即時轉換語音到文字  
⚙️ **AI 驅動的回答**：使用 Google Generative AI提供流暢、智慧的回答  
🎭 **不重複與指定情境創建**：使的話題不會重複，且根據使用者喜好調整  
🤖 **於 Zenbo 流暢交互**：在 Zenbo 平台上的交互，簡單地進行對話  


## 技術棧
- **語言：** Java + Kotlin
- **Android 平台：** API 26+
- **主要框架與工具：**
  - Zenbo junior II SDK 用於使用Zenbo內建功能
  - Google Gemini API 用於生成式 AI 支援

## 流程圖  
以下為 Zenbo English Chatbot 的主要運行流程示意圖，展現應用程序的工作原理與互動模式：  
[![image](https://github.com/user-attachments/assets/fd5a3c7e-7960-4569-b661-ee7afac85fec)](https://www.canva.com/design/DAGYJFNg-cU/GiFq3R9p1XqNnL4plaW7gw/view?utm_content=DAGYJFNg-cU&utm_campaign=designshare&utm_medium=link2&utm_source=uniquelinks&utlId=h46c44d481f)

## 實作影片  
點擊下方影片縮圖觀看應用實作展示：  
[![實作影片](https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHNhMtpD0N9umcfH2Mj8BqGavHrHhVQHIExg&s)](https://youtu.be/PJ-nb89-_IU)


## 檔案結構
  ZenboEnglishChatbot/  
├── app/  
│   ├── src/              # 程式碼與資源文件  
│   │   ├── main/  
│   │   │   ├── java/     # Java/Kotlin 原始碼  
│   │   │   └── res/      # 資源文件 (UI 設計、圖片等)  
│   └── build.gradle.kts  # 模組級 Gradle 配置  
├── build.gradle.kts       # 專案級 Gradle 配置  
└── README.md             # 此文件  



- ## 未來規劃
- **增強 Zenbo 的互動能力**  
  提供更豐富的表情與動作組合，實現更生動的教學效果  
- **針對學習階段的分級**  
  根據語言水平劃分初、中、高級對話內容，讓學習過程更加貼合用戶需求  
- **離線模式開發**  
  減少對 API 的依賴，確保在網絡連接不穩時，仍能提供核心功能支持

## 聯絡方式
如有任何問題或建議，歡迎透過以下方式聯絡我：

Email: yuxiangy87@gmail.com
