package com.ricky.file.handler.tasks;

import com.ricky.common.domain.task.RetryableTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncFileToEsTask implements RetryableTask {

    public void run(String fileId, String filename, String hash, Long size) {
        // TODO 建立ES索引
    }
}
