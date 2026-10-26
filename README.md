# AI Chat Demo

基于 Spring Boot 和 Spring AI 的 AI 聊天示例，通过 OpenAI 兼容协议接入 DeepSeek，支持普通响应和 SSE 流式响应。

仓库地址：https://github.com/popopo8/AIStudy

## 技术栈

- Java 17
- Spring Boot 3.4.5
- Spring AI 1.0.0
- DeepSeek（OpenAI 兼容接口）
- Maven

## 功能

- `GET /api/chat/simple?message=你好`：一次性返回完整回答
- `GET /api/chat/stream?message=你好`：SSE 流式返回回答
- `GET /`：内置聊天页面

## 配置

API Key 通过环境变量注入，不要写入代码或配置文件。

| 环境变量 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `LLM_API_KEY` | 是 | 无 | 模型服务 API Key |
| `LLM_BASE_URL` | 否 | `https://api.deepseek.com` | OpenAI 兼容接口地址 |
| `LLM_MODEL` | 否 | `deepseek-chat` | 模型名称 |

### Windows PowerShell

```powershell
$env:LLM_API_KEY="sk-你的真实Key"

# 可选：切换接口地址或模型
$env:LLM_BASE_URL="https://api.deepseek.com"
$env:LLM_MODEL="deepseek-chat"
```

环境变量只在当前 PowerShell 窗口有效。修改后需要重新启动应用，热重载不会生效。

### IDEA

打开 `Run -> Edit Configurations -> Environment variables`，添加：

```text
LLM_API_KEY=sk-你的真实Key
```

只填值，不要填 `${LLM_API_KEY}`。设置完成后重启应用。

## 启动

```powershell
mvn spring-boot:run
```

启动后访问 http://localhost:8080

也可以先打包再运行：

```powershell
mvn clean package
java -jar target/ai-chat-demo-0.0.1-SNAPSHOT.jar
```

## 项目结构

```text
src/main/java/com/example/aichat
├── AiChatApplication.java          # 启动类
├── config
│   └── LlmStartupCheck.java        # 启动时检查 API Key
├── controller
│   └── ChatController.java         # 普通和流式聊天接口
└── service
    └── LlmService.java             # 模型调用入口

src/main/resources
├── application.yml                 # 应用配置
└── static/index.html               # 聊天页面
```

## 安全说明

- 仓库中不保存任何 API Key。
- 如果 Key 曾经出现在提交历史或公开位置，请立即到模型服务控制台吊销并重新生成。
