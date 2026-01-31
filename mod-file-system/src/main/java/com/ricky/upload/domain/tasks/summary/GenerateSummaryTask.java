package com.ricky.upload.domain.tasks.summary;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.exception.MyException;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    public void run(String fileId) {
        FileExtra fileExtra = fileExtraRepository.byFileId(fileId);

        if (isNotBlank(fileExtra.getSummary())) {
            log.info("summary already exists for FileExtra[{}]", fileExtra.getId());
        }

        String textFilePath = fileExtra.getTextFilePath();
        if (isBlank(textFilePath)) {
            log.error("文本文件路径为空");
            return;
        }

        String summary = generateSummary(textFilePath);
        fileExtra.setSummary(summary);
        fileExtraRepository.save(fileExtra);
    }

    String generateSummary(String textFilePath) {
        try {
            return summaryGenerator.generate(textFilePath);
        } catch (IOException ex) {
            throw new MyException(GENERATE_SUMMARY_FAILED, "摘要生成失败", "textFilePath", textFilePath);
        }
    }

}
