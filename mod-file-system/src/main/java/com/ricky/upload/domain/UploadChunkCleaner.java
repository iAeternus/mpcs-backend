package com.ricky.upload.domain;

import com.ricky.common.properties.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadChunkCleaner {

    private final FileProperties fileProperties;

    public void cleanAfterCommit(String uploadId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Path userChunkDir = Paths.get(fileProperties.getUpload().getChunkDir(), uploadId);
                deleteQuietly(userChunkDir);
            }
        });
    }

    private void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(dir)) {
            // 先删文件，再删目录
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("Failed to delete chunk file: {}", path, e);
                }
            });
        } catch (IOException e) {
            log.warn("Failed to clean chunk dir {}", dir, e);
        }
    }
}
