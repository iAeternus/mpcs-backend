package com.ricky.fileextra.domain.task;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.exception.MyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_DELETE_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
public class DeleteTextFileTask implements RetryableTask {

    public void run(String textFilePath) {
        if (isBlank(textFilePath)) {
            log.warn("Cannot delete text file because textFilePath is blank");
            return;
        }
        Path path = Path.of(textFilePath);

        // 首先检查文件是否存在
        if (!Files.exists(path)) {
            log.warn("File not found: {}, skipping handle", textFilePath);
            return;
        }

        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new MyException(FILE_DELETE_FAILED, "删除文本文件失败",
                    "textFilePath=", textFilePath);
        }
    }
}
