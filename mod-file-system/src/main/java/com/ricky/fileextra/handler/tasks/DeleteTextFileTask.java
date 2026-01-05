package com.ricky.fileextra.handler.tasks;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.exception.MyException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_DELETE_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_NOT_FOUND;

@Component
public class DeleteTextFileTask implements RetryableTask {

    public void run(String textFilePath) {
        Path path = Path.of(textFilePath);

        // 首先检查文件是否存在
        if (!Files.exists(path)) {
            throw new MyException(FILE_NOT_FOUND, "文件不存在",
                    "textFilePath", textFilePath);
        }

        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new MyException(FILE_DELETE_FAILED, "删除文本文件失败",
                    "textFilePath=", textFilePath);
        }
    }
}
