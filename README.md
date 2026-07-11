# MnemoAI - 智能单词记忆助手

MnemoAI 是一款结合 AI 智能释义与科学记忆算法的背单词应用，帮助你更高效地掌握单词。应用采用现代 Android 开发架构和 Material Design 3 设计。

## ✨ 功能特性

- **AI 智能释义**：获取单词的核心意象、词根词缀拆解和上下文例句，加深理解
- **自定义单词分组**：自由创建和管理单词集（如“雅思词汇”、“四级词汇”、“工作术语”）
- **科学复习提醒**：基于间隔重复算法，在最佳遗忘节点安排复习
- **学习进度追踪**：按分组查看单词掌握情况与统计信息
- **深色/浅色模式**：完美适配系统主题切换
- **离线优先**：所有数据本地存储，无需网络即可使用
- **AI 深度集成**：接入 DeepSeek API，提供单词解析

## 📱 应用截图

*（截图）*

## 🚀 技术栈

- **语言**：Kotlin + Java
- **架构**：MVVM + Repository 模式
- **UI**：Material Design 3、ViewBinding、自定义 View
- **数据库**：Room（SQLite）
- **网络**：Retrofit + OkHttp
- **异步处理**：Java 线程池 + LiveData
- **导航**：Navigation Component
- **AI 服务**：DeepSeek API
- **依赖注入**：手动 DI（通过 Repository 模式）


## 🛠️ 快速开始

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 11+
- Android SDK 24+
- DeepSeek API Key（用于 AI 功能）

### 配置步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/mnemoai.git
   cd mnemoai
