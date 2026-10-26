package com.example.aichat.controller;

import com.example.aichat.service.LlmService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final long SSE_TIMEOUT_MS = 0L;

    private final LlmService llmService;

    public ChatController(LlmService llmService) {
        this.llmService = llmService;
    }

    /**
     * 非流式：先用最少的代码确认「key 生效 + 网络通」。
     * GET /api/chat/simple?message=你好
     */
    @GetMapping("/simple")
    public String simple(@RequestParam String message) {
        return llmService.chat(message);
    }

    /**
     * 流式：阶段 0 的验收接口。
     * GET /api/chat/stream?message=你好
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter stream(@RequestParam String message) {
        // 学习阶段先不设超时，W1 周五再收紧
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        AtomicBoolean closed = new AtomicBoolean(false);

        Disposable subscription = llmService.stream(message).subscribe(
                chunk -> {
                    if (closed.get()) {
                        return;
                    }
                    try {
                        emitter.send(chunk);
                    } catch (IOException e) {
                        // 浏览器断开时不要让它把日志刷爆
                        closed.set(true);
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    closed.set(true);
                    try {
                        emitter.send("[出错了] " + error.getMessage());
                    } catch (IOException ignored) {
                        // 连接已断开，无需处理
                    }
                    emitter.complete();
                },
                () -> {
                    // 发一个具名事件，前端收到后主动关闭连接，
                    // 否则 EventSource 会自动重连并重复请求
                    try {
                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    } catch (IOException ignored) {
                        // 连接已断开，无需处理
                    }
                    emitter.complete();
                }
        );

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(subscription::dispose);
        emitter.onError(e -> subscription.dispose());

        return emitter;
    }
}
