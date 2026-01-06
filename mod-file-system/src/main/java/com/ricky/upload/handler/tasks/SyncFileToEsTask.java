package com.ricky.upload.handler.tasks;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.es.FileElasticSearchService;
import com.ricky.file.domain.EsFile;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SyncFileToEsTask implements RetryableTask {

    private final FileRepository fileRepository;
    private final FileExtraRepository fileExtraRepository;
    private final FileElasticSearchService esService;

    @Transactional
    public void run(String fileId) {
        File file = fileRepository.cachedById(fileId);
        FileExtra fileExtra = fileExtraRepository.cachedByFileId(fileId);

        EsFile esFile = new EsFile(
                file.getId(),
                file.getFilename(),
                file.getCategory().getName(),
                fileExtra.getSummary(),
                fileExtra.getKeywords(),
                file.getSize(),
                Date.from(file.getUpdatedAt())
        );

        esService.index(esFile);
    }
}
