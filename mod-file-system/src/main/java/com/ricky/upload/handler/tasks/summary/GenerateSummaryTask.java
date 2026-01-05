package com.ricky.upload.handler.tasks.summary;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static com.ricky.common.exception.ErrorCodeEnum.GENERATE_SUMMARY_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateSummaryTask {

    private final SummaryGenerator summaryGenerator;
    private final FileExtraRepository fileExtraRepository;

    @Transactional
    public void run(String fileId, UserContext userContext) {
        FileExtra fileExtra = fileExtraRepository.byFileId(fileId);
        String textFilePath = fileExtra.getTextFilePath();
        if(isBlank(textFilePath)) {
            log.error("文本文件路径为空");
            return;
        }

        String summary = generateSummary(textFilePath);
        fileExtra.setSummary(summary, userContext);
        fileExtraRepository.save(fileExtra);
    }

    String generateSummary(String textFilePath) {
        try {
            return summaryGenerator.generate(textFilePath);
        } catch (IOException ex) {
            throw new MyException(GENERATE_SUMMARY_FAILED, "AI摘要生成失败", "textFilePath", textFilePath);
        }
    }

}
