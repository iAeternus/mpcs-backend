package com.ricky.upload.domain.tasks.extracttext;

import com.ricky.common.domain.task.RetryableTask;
import com.ricky.common.exception.MyException;
import com.ricky.common.properties.FileProperties;
import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.StorageId;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.upload.domain.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;

import static com.ricky.common.exception.ErrorCodeEnum.EXTRACT_FILE_TEXT_FAILED;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractTextTask implements RetryableTask {

    private final TextExtractorFactory extractorFactory;
    private final StorageService storageService;
    private final FileProperties fileProperties;
    private final FileExtraRepository fileExtraRepository;

    @Transactional
    public void run(String fileId, StorageId storageId, FileCategory category) {
        if (!extractorFactory.supports(category)) {
            log.warn("No extractor supports category: {}, skipping", category);
            return;
        }

        TextExtractor extractor = extractorFactory.getExtractor(category)
                .orElseThrow(() ->
                        new UnsupportedOperationException(String.format("No extractor found for category: %s", category)));

        String filepath = extract(storageId, extractor);
        if (isBlank(filepath)) {
            return;
        }

        FileExtra fileExtra = fileExtraRepository.byFileId(fileId);
        fileExtra.setTextFilePath(filepath);
        fileExtraRepository.save(fileExtra);
    }

    private String extract(StorageId storageId, TextExtractor extractor) {
        try (InputStream inputStream = storageService.getFileStream(storageId)) {
            String textFileDir = fileProperties.getTextFileDir();
            return extractor.extract(storageId, inputStream, textFileDir);
        } catch (IOException ex) {
            throw new MyException(EXTRACT_FILE_TEXT_FAILED, "提取文件文本失败", "storageId", storageId);
        }
    }
}
