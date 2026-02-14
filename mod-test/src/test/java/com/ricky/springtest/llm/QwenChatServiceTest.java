package com.ricky.springtest.llm;

import com.ricky.MpcsBackendApplication;
import com.ricky.common.llm.domain.LLMChatRequest;
import com.ricky.common.llm.domain.LLMChatResponse;
import com.ricky.common.llm.service.LLMChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringBootTest(classes = MpcsBackendApplication.class)
class QwenChatServiceTest {

    @Autowired
    private LLMChatService llmChatService;

    private static final String GENERATE_SUMMARY_PROMPT = "请为以下文档生成中文摘要，要求：总结核心内容，语言精炼专业，字数不超过100字。输出仅包含摘要正文，不加任何解释和标签。";

    @Test
//    @Disabled("暂时不稳定，后续修复")
    void chat() {
        // Given
        final String userMessage = """
                HAMLET:
                To be, or not to be, that is the question:
                Whether 'tis nobler in the mind to suffer
                The slings and arrows of outrageous fortune,
                Or to take arms against a sea of troubles
                And by opposing end them. To die-to sleep,
                No more; and by a sleep to say we end
                The heart-ache and the thousand natural shocks
                That flesh is heir to: 'tis a consummation
                Devoutly to be wish'd. To die, to sleep;
                To sleep, perchance to dream-ay, there's the rub:
                For in that sleep of death what dreams may come,
                When we have shuffled off this mortal coil,
                Must give us pause-there's the respect
                That makes calamity of so long life.
                """;

        LLMChatRequest req = LLMChatRequest.builder()
                .userMessage(userMessage)
                .systemPrompt(GENERATE_SUMMARY_PROMPT)
                .build();

        // When
        LLMChatResponse resp = llmChatService.chat(req);

        // Then
        log.info("Content: {}", resp.getText());
    }

    @Test
//    @Disabled("暂时不稳定，后续修复")
    void streamChat() {
        // Given
        final String userMessage = """
                HAMLET:
                To be, or not to be, that is the question:
                Whether 'tis nobler in the mind to suffer
                The slings and arrows of outrageous fortune,
                Or to take arms against a sea of troubles
                And by opposing end them. To die-to sleep,
                No more; and by a sleep to say we end
                The heart-ache and the thousand natural shocks
                That flesh is heir to: 'tis a consummation
                Devoutly to be wish'd. To die, to sleep;
                To sleep, perchance to dream-ay, there's the rub:
                For in that sleep of death what dreams may come,
                When we have shuffled off this mortal coil,
                Must give us pause-there's the respect
                That makes calamity of so long life.
                """;

        LLMChatRequest req = LLMChatRequest.builder()
                .userMessage(userMessage)
                .systemPrompt(GENERATE_SUMMARY_PROMPT)
                .build();

        // 用于拼接完整输出
        StringBuilder contentBuilder = new StringBuilder();
        AtomicBoolean finished = new AtomicBoolean(false);
        AtomicReference<String> model = new AtomicReference<>();

        // When
        llmChatService.streamChat(req)
                .timeout(Duration.ofSeconds(60)) // 防止测试卡死
                .doOnNext(chunk -> {
                    if (chunk.getModel() != null) {
                        model.set(chunk.getModel());
                    }
                    if (chunk.getDelta() != null) {
                        contentBuilder.append(chunk.getDelta());
                    }
                    if (chunk.isFinished()) {
                        finished.set(true);
                    }
                })
                .doOnError(e -> log.error("Stream error", e))
                .blockLast(); // 测试中必须阻塞

        // Then
        log.info("Stream chat model: {}", model.get());
        log.info("Stream chat content: {}", contentBuilder);

        // 基本断言（避免空跑）
        assert finished.get() : "Stream did not finish correctly";
        assert !contentBuilder.isEmpty() : "Stream content is empty";
    }

}