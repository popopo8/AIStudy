package com.example.aichat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 模型调用的唯一入口。
 * 后续要加重试、埋点、限流、降级，都只改这里，Controller 不动。
 */
@Service
public class LlmService {

    private final ChatClient chatClient;

    public LlmService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 一次性返回完整结果，用来验证 key 和链路是否通。
     */
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式返回，阶段 0 的验收接口。
     */
    public Flux<String> stream(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
