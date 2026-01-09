package com.ricky.upload.domain.tasks.summary.impl;

import cn.hutool.core.date.StopWatch;
import com.ricky.common.exception.MyException;
import com.ricky.common.llm.AICommand;
import com.ricky.common.llm.LLMService;
import com.ricky.upload.domain.tasks.summary.SummaryGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.constants.LLMPromptConstants.FILE_SUMMARY_PROMPT;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_READ_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class LLMSummaryGenerator implements SummaryGenerator {

    private final LLMService llmService;

    /**
     * 基于已生成的文本文件路径生成摘要
     *
     * @param textFilePath 已落盘的纯文本文件路径
     */
    @Override
    public String generate(String textFilePath) {
        StopWatch stopWatch = new StopWatch("LLMSummaryGenerator");
        stopWatch.start();

        // 读取文本文件（IO 失败直接抛异常，不做降级）
        String rawText = readTextFile(textFilePath);

        if (isBlank(rawText)) {
            log.warn("文本文件内容为空，path={}", textFilePath);
            return "【无法生成摘要：文本内容为空】";
        }

        // 文本预处理
        String processedText = preprocessText(rawText);

        // 调用 AI（仅 AI 失败才允许降级）
        try {
            String summary = generateSummaryWithAI(processedText);
            stopWatch.stop();

            log.info(
                    "摘要生成成功，path={}, 原文长度={}，摘要长度={}，耗时={}ms",
                    textFilePath,
                    rawText.length(),
                    summary.length(),
                    stopWatch.getTotalTimeMillis()
            );
            return summary;
        } catch (Exception ex) {
            log.error("AI 摘要生成失败，启用降级方案，path={}", textFilePath, ex);
            return createFallbackSummary(rawText);
        }
    }

    private String readTextFile(String path) {
        try {
            return Files.readString(Path.of(path), DEFAULT_CHARSET);
        } catch (IOException ex) {
            throw new MyException(FILE_READ_FAILED, "读取文本文件失败", "path", path, "exception", ex);
        }
    }

    /**
     * 文本预处理：
     * - 合并多余空白
     * - 控制最大输入长度
     */
    private String preprocessText(String rawText) {
        String cleaned = rawText.replaceAll("\\s+", " ").trim();

        if (cleaned.length() > MAX_INPUT_TEXT_LENGTH) {
            log.warn("文本超过最大长度限制，将被截断，limit={}", MAX_INPUT_TEXT_LENGTH);
            cleaned = cleaned.substring(0, MAX_INPUT_TEXT_LENGTH) + "...【文本已截断】";
        }

        return cleaned;
    }

    /**
     * 调用 LLM 生成摘要
     */
    private String generateSummaryWithAI(String text) {
        AICommand command = AICommand.builder()
                .systemPrompt(FILE_SUMMARY_PROMPT)
                .userPrompt("Please generate a concise summary for the following text:\n\n" + text)
                .build();

        return llmService.chat(command)
                .getContent()
                .trim();
    }

    /**
     * AI失败时的降级摘要方案
     */
    private String createFallbackSummary(String rawText) {
        // 优先取第一段较完整内容
        String[] paragraphs = rawText.split("\\n\\s*\\n");
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.length() > 50) {
                return "【基础摘要】" +
                        (trimmed.length() > 200
                                ? trimmed.substring(0, 200) + "..."
                                : trimmed);
            }
        }

        // 最终兜底：取开头
        String text = rawText.trim();
        return "【内容预览】" +
                (text.length() > 200 ? text.substring(0, 200) + "..." : text);
    }
}
