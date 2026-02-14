package com.ricky.fileextra.domain.task;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.exception.MyException;
import com.ricky.common.properties.FileProperties;
import com.ricky.fileextra.domain.TextFileCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_DELETE_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteTextFileTask implements RetryableTask {

    private final FileProperties fileProperties;

    public void run(String textFileKey, String textFilePath) {
        String resolvedPath = resolvePath(textFileKey, textFilePath);
        if (isBlank(resolvedPath)) {
            log.warn("Cannot delete text file because textFilePath is blank");
            return;
        }
        Path path = Path.of(resolvedPath);

        // 首先检查文件是否存在
        if (!Files.exists(path)) {
            log.warn("File not found: {}, skipping handle", resolvedPath);
            return;
        }

        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new MyException(FILE_DELETE_FAILED, "删除文本文件失败",
                    "textFilePath=", resolvedPath);
        }
    }

    private String resolvePath(String textFileKey, String textFilePath) {
        if (!isBlank(textFileKey)) {
            return TextFileCache.buildPath(fileProperties.getTextFileDir(), textFileKey);
        }
        return textFilePath;
    }
}
