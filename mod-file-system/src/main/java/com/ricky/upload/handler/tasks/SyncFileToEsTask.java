package com.ricky.upload.handler.tasks;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.es.FileElasticSearchService;
import com.ricky.file.domain.EsFile;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SyncFileToEsTask implements RetryableTask {

    private final FileRepository fileRepository;
    private final FileElasticSearchService esService;

    public void run(String fileId) {
        File file = fileRepository.cachedById(fileId);

        EsFile esFile = new EsFile(
                file.getId(),
                file.getFilename(),
                file.getCategory().getName(),
                file.getSummary(),
                List.of(), // TODO 关键词列表
                file.getSize(),
                Date.from(file.getUpdatedAt())
        );

        esService.upload(esFile);
    }
}
