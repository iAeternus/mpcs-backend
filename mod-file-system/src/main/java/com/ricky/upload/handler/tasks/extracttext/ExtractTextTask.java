package com.ricky.upload.handler.tasks.extracttext;

import com.ricky.common.properties.FileProperties;
import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.StorageId;
import com.ricky.upload.domain.FileStorage;
import com.ricky.upload.handler.tasks.extracttext.impl.TxtExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.ricky.file.domain.FileCategory.TEXT;

@Component
@RequiredArgsConstructor
public class ExtractTextTask {

    private final FileStorage fileStorage;
    private final FileProperties fileProperties;

    private static final Map<FileCategory, TextExtractor> EXTRACTORS = new HashMap<>();

    static {
        EXTRACTORS.put(TEXT, new TxtExtractor());
        // TODO add here...
    }

    public void run(StorageId storageId, FileCategory category) {
        InputStream inputStream = fileStorage.getFileStream(storageId);
        String textFilePath = fileProperties.getTextFilePath();
        extract(inputStream, textFilePath, category);
    }

    // 若没有提取器匹配，则不做任何事
    private void extract(InputStream inputStream, String textFilePath, FileCategory fileCategory) {
        EXTRACTORS.forEach((category, extractor) -> {
            try {
                if (fileCategory == category) {
                    EXTRACTORS.get(category).extract(inputStream, textFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
