package com.ricky.upload.domain;

import java.util.Optional;

public interface UploadSessionRepository {
    Optional<UploadSession> byFileHashAndOwnerIdOptional(String fileHash, String ownerId);

    void save(UploadSession session);

    UploadSession cachedById(String uploadId);

    UploadSession byId(String uploadId);
}
