package com.ricky.upload.eventhandler;

import com.ricky.common.event.local.AbstractLocalDomainEventHandler;
import com.ricky.common.properties.FileProperties;
import com.ricky.upload.domain.event.UploadSessionCompletedLocalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadSessionCompletedLocalEventHandler extends AbstractLocalDomainEventHandler<UploadSessionCompletedLocalEvent> {

    private final FileProperties fileProperties;

    @Override
    protected void doHandle(UploadSessionCompletedLocalEvent event) {
        Path userChunkDir = Paths.get(fileProperties.getUpload().getChunkDir(), event.getUploadId());
        deleteQuietly(userChunkDir);
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
