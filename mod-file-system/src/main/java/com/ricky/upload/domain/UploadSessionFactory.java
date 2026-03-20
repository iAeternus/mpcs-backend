package com.ricky.upload.domain;

import com.ricky.common.domain.user.UserContext;
import org.springframework.stereotype.Component;

@Component
public class UploadSessionFactory {

    public UploadSession create(String ownerId,
                                String filename,
                                String expectedHash,
                                long totalSize,
                                int chunkSize,
                                int totalChunks,
                                String ossUploadId,
                                UserContext userContext) {
        return new UploadSession(
                ownerId, filename, expectedHash,
                totalSize, chunkSize, totalChunks, ossUploadId, userContext
        );
    }

}
