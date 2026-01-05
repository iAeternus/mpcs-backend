package com.ricky.fileextra.domain;

public interface FileExtraRepository {
    void save(FileExtra fileExtra);

    FileExtra byFileId(String fileId);

    FileExtra cachedByFileId(String fileId);
}
