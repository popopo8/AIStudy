package com.example.aichat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 启动时自检：确认 api-key 真的从环境变量注入进来了。
 * <p>
 * Spring AI 1.0 不会因为占位符没解析到而启动失败，而是把字面量 "${LLM_API_KEY}"
 * 当成 key 发出去，最终表现为一个看不懂的 401。这里提前拦住，变成一眼能看懂的启动错误。
 */
@Configuration
public class LlmStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(LlmStartupCheck.class);

    public LlmStartupCheck(Environment env) {
        // 环境变量缺失时 getProperty 会抛 PlaceholderResolutionException，
        // 这里接住它，换成能直接照做的提示
        String apiKey;
        try {
            apiKey = env.getProperty("spring.ai.openai.api-key");
        } catch (IllegalArgumentException e) {
            apiKey = null;
        }

        String baseUrl = env.getProperty("spring.ai.openai.base-url");
        String model = env.getProperty("spring.ai.openai.chat.options.model");

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("${")) {
            throw new IllegalStateException("""
                    LLM_API_KEY 没有生效，当前取到的值是：[%s]
                    原因：环境变量没有传到启动进程，Spring AI 会把字面量当 key 发出去，最后表现为 HTTP 401。
                    修复：
                      1) PowerShell：$env:LLM_API_KEY="sk-你的真实key"，且必须在同一个窗口启动应用
                      2) IDEA：Run -> Edit Configurations -> Environment variables 填 LLM_API_KEY=sk-你的真实key
                         注意只填值，不要填 ${LLM_API_KEY}；改完必须重启，热重载不生效
                    """.formatted(apiKey));
        }

        log.info("LLM 配置就绪：baseUrl={}, model={}, key={}****{}",
                baseUrl, model, apiKey.substring(0, Math.min(3, apiKey.length())),
                apiKey.substring(Math.max(0, apiKey.length() - 4)));
    }
}
