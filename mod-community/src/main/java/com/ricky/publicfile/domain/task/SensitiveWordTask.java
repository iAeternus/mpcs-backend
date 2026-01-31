package com.ricky.publicfile.domain.task;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.sensitive.service.SensitiveWordService;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ricky.common.domain.user.UserContext.NOUSER;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordTask implements RetryableTask {

    private final SensitiveWordService sensitiveWordService;
    private final FileExtraRepository fileExtraRepository;
    private final PublicFileRepository publicFileRepository;

    public void run(String originalFileId, String postId) {
        FileExtra fileExtra = fileExtraRepository.byFileId(originalFileId);

        String textFilePath = fileExtra.getTextFilePath();
        if (isBlank(textFilePath)) {
            log.error("文本文件路径为空");
            return;
        }

        PublicFile publicFile = publicFileRepository.byId(postId);
        handleSensitiveWord(publicFile, textFilePath);
        publicFileRepository.save(publicFile);

        log.info("PublicFile[{}] 敏感词处理完毕", postId);
    }

    private void handleSensitiveWord(PublicFile file, String textFilePath) {
        try {
            Path path = Paths.get(textFilePath);
            if (sensitiveWordService.hasSensitiveWord(Files.newInputStream(path))) {
                file.reject(NOUSER);
            } else {
                file.approve(NOUSER);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
