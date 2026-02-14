package com.ricky.upload.domain.tasks.summary;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.exception.MyException;
import com.ricky.common.properties.FileProperties;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.fileextra.domain.TextFileCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.ricky.common.exception.ErrorCodeEnum.GENERATE_SUMMARY_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.utils.ValidationUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateSummaryTask implements RetryableTask {

    private final SummaryGenerator summaryGenerator;
    private final FileExtraRepository fileExtraRepository;
    private final FileProperties fileProperties;

    public void run(String fileId) {
        FileExtra fileExtra = fileExtraRepository.byFileId(fileId);

        if (isNotBlank(fileExtra.getSummary())) {
            log.info("summary already exists for FileExtra[{}]", fileExtra.getId());
            return;
        }

        String textFilePath = resolveTextFilePath(fileExtra);
        if (isBlank(textFilePath)) {
            log.error("文本文件路径为空");
            return;
        }

        String summary = generateSummary(textFilePath);
        fileExtra.setSummary(summary);
        fileExtraRepository.save(fileExtra);
    }

    private String resolveTextFilePath(FileExtra fileExtra) {
        if (isNotBlank(fileExtra.getTextFileKey())) {
            return TextFileCache.buildPath(fileProperties.getTextFileDir(), fileExtra.getTextFileKey());
        }
        return fileExtra.getTextFilePath();
    }

    String generateSummary(String textFilePath) {
        try {
            return summaryGenerator.generate(textFilePath);
        } catch (IOException ex) {
            throw new MyException(GENERATE_SUMMARY_FAILED, "摘要生成失败", "textFilePath", textFilePath, "exception", ex);
        }
    }

}
