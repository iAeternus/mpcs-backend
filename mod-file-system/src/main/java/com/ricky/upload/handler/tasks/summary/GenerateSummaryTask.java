package com.ricky.upload.handler.tasks.summary;

import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.StorageId;
import com.ricky.upload.domain.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateSummaryTask {

    private final SummaryGenerator summaryGenerator;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    public void run(String fileId, StorageId storageId) {
        InputStream inputStream = fileStorage.getFileStream(storageId);
        String summary = summaryGenerator.generate(inputStream);

        File file = fileRepository.byId(fileId);
        file.setSummary(summary);
        fileRepository.save(file);
    }

}
