package com.ricky.upload.domain.tasks.extracttext.impl;

import com.ricky.file.domain.FileCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
public class TxtExtractor extends AbstractTextExtractor {

    @Override
    public FileCategory getSupportedCategory() {
        return FileCategory.TEXT;
    }

    @Override
    protected void doExtract(InputStream inputStream, String textFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(textFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            log.debug("Text extracted to: {}", textFilePath);
        }
    }
}
