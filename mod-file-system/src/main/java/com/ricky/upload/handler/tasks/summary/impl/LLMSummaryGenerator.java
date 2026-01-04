package com.ricky.upload.handler.tasks.summary.impl;

import com.ricky.common.exception.MyException;
import com.ricky.common.llm.AICommand;
import com.ricky.common.llm.LLMService;
import com.ricky.upload.handler.tasks.summary.SummaryGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.constants.LLMPromptConstants.FILE_SUMMARY_PROMPT;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_READ_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.GENERATE_SUMMARY_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class LLMSummaryGenerator implements SummaryGenerator {

    private final LLMService llmService;

    @Override
    public String generate(InputStream inputStream) {
        StopWatch stopWatch = new StopWatch("LLMSummaryGenerator");
        stopWatch.start("total");

        String rawText = null;
        try {
            rawText = readInputStreamSafely(inputStream);

            if (isBlank(rawText)) {
                log.warn("输入文本为空，无法生成摘要");
                return "【无法生成摘要：文件内容为空】";
            }

            // 预处理文本
            String processedText = preprocessText(rawText);

            // 调用LLM服务生成摘要
            String summary = generateSummaryWithAI(processedText);

            stopWatch.stop();
            log.info("摘要生成成功。原始文本长度: {} 字符, 摘要长度: {} 字符, 耗时: {} ms",
                    rawText.length(), summary.length(), stopWatch.getLastTaskTimeMillis());
            return summary;
        } catch (IOException e) {
            log.error("临时文本文件读取失败");
            throw new MyException(FILE_READ_FAILED, "Read temp text file failed. ");
        } catch (Exception e) {
            log.error("摘要生成过程发生意外错误", e);
            return createFallbackSummary(rawText, e);
        }
    }

    private String readInputStreamSafely(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_CHARSET), BUFFER_SIZE)) {
            StringBuilder contentBuilder = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;

            while ((charsRead = reader.read(buffer)) != -1) {
                contentBuilder.append(buffer, 0, charsRead);

                if (contentBuilder.length() > MAX_INPUT_TEXT_LENGTH) {
                    log.warn("输入文本超过安全长度限制 ({}字符)，将被截断", MAX_INPUT_TEXT_LENGTH);
                    contentBuilder.setLength(MAX_INPUT_TEXT_LENGTH);
                    contentBuilder.append("...【文本已截断】");
                    break;
                }
            }

            return contentBuilder.toString();
        }
    }

    private String preprocessText(String rawText) {
        if (rawText == null) return "";

        String cleaned = rawText.replaceAll("\\s+", " ").trim();

        // 文本过长则截断
        if (cleaned.length() > MAX_INPUT_TEXT_LENGTH) {
            cleaned = cleaned.substring(0, MAX_INPUT_TEXT_LENGTH) + "...【文本已截断】";
        }

        // 记录预处理信息
        if (cleaned.length() < rawText.length()) {
            log.debug("文本预处理：从 {} 字符精简至 {} 字符",
                    rawText.length(), cleaned.length());
        }

        return cleaned;
    }

    private String generateSummaryWithAI(String text) {
        log.debug("调用AI服务生成摘要，输入文本长度: {} 字符", text.length());

        // 构建优化的提示词
        String userPrompt = String.format("请为以下文本生成摘要：\n\n%s", text);
        AICommand command = AICommand.builder()
                .systemPrompt(FILE_SUMMARY_PROMPT)
                .userPrompt(userPrompt)
                .options(Map.of(
                        "temperature", 0.3, // 较低随机性，确保摘要一致性
                        "max_tokens", SUMMARY_MAX_TOKENS,
                        "model", "deepseek-chat"
                ))
                .build();

        try {
            return llmService.chat(command)
                    .getContent()
                    .trim();
        } catch (Exception ex) {
            log.error("AI服务调用失败", ex);
            throw new MyException(GENERATE_SUMMARY_FAILED, "AI摘要服务暂时不可用。", "message", ex.getMessage());
        }
    }

    private String createFallbackSummary(String rawText, Exception error) {
        log.warn("启用摘要降级方案，原因: {}", error.getMessage());

        if (rawText == null || rawText.trim().isEmpty()) {
            return "【摘要生成失败：无法读取文本内容】";
        }

        // 规则1：取第一段非空内容
        String[] paragraphs = rawText.split("\\n\\s*\\n");
        for (String para : paragraphs) {
            if (para.trim().length() > 20) {
                String trimmed = para.trim();
                return "【基础摘要】" + (trimmed.length() > 200 ? trimmed.substring(0, 200) + "..." : trimmed);
            }
        }

        // 规则2：取开头部分
        String trimmedText = rawText.trim();
        if (trimmedText.length() > 200) {
            return "【内容预览】" + trimmedText.substring(0, 200) + "...";
        }

        return "【内容概要】" + trimmedText;
    }
}