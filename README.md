# Telegram Bot Java 项目文档

## 项目概述
基于 Telegram Bot API 开发的 Java 机器人应用，提供自动化服务和交互功能。

## 技术选型
- 开发语言：Java 17
- 构建工具：Maven/Gradle
- Bot API：[Telegram Bot API](https://core.telegram.org/bots/api)
- 主要依赖：
  - org.telegram:telegrambots-spring-boot-starter
  - org.springframework.boot:spring-boot-starter-web
  - org.springframework.boot:spring-boot-starter-data-jpa
  - org.springframework.boot:spring-boot-starter-cache
  - com.github.pengrad:java-telegram-bot-api
  - redis.clients:jedis

## 核心功能

### 1. 基础功能
- 命令处理 (Commands)
- 内联查询 (Inline Queries)
- 回调查询 (Callback Queries)
- 消息处理 (Message Handlers)

### 2. 交互功能
- 自定义键盘 (Custom Keyboards)
- 内联按钮 (Inline Buttons)
- 消息编辑 (Message Editing)
- 多媒体消息支持 (Media Messages)

### 3. 高级功能
- 支付集成 (Payments API)
- 网页应用 (Web Apps)
- 文件处理
- 群组管理

## 项目结构 