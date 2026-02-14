package com.ricky.upload.domain.tasks.summary.impl;

import com.ricky.common.exception.MyException;
import com.ricky.common.profile.CiProfile;
import com.ricky.upload.domain.tasks.summary.SummaryGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ricky.common.constants.ConfigConstants.DEFAULT_CHARSET;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_READ_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@CiProfile
@Component
public class FakeSummaryGenerator implements SummaryGenerator {
    @Override
    public String generate(String textFilePath) {
        // 读取文本文件
        String rawText = readTextFile(textFilePath);

        if (isBlank(rawText)) {
            log.warn("文本文件内容为空，path={}", textFilePath);
            return "【无法生成摘要：文本内容为空】";
        }

        // 优先取第一段较完整内容
        String[] paragraphs = rawText.split("\\n\\s*\\n");
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.length() > 50) {
                return "【Fake摘要】" +
                        (trimmed.length() > 200
                                ? trimmed.substring(0, 200) + "..."
                                : trimmed);
            }
        }

        // 最终兜底：取开头
        String text = rawText.trim();
        return "【Fake内容预览】" +
                (text.length() > 200 ? text.substring(0, 200) + "..." : text);
    }

    private String readTextFile(String path) {
        try {
            return Files.readString(Path.of(path), DEFAULT_CHARSET);
        } catch (IOException ex) {
            throw new MyException(FILE_READ_FAILED, "读取文本文件失败", "path", path, "exception", ex);
        }
    }
}
