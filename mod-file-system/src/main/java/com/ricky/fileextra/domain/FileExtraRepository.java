package com.ricky.fileextra.domain;

import java.util.List;

public interface FileExtraRepository {
    void save(FileExtra fileExtra);

    FileExtra byFileId(String fileId);

    FileExtra cachedByFileId(String fileId);

    void delete(FileExtra fileExtra);

    void delete(List<FileExtra> fileExtras);

    List<FileExtra> listByFileIds(List<String> fileIds);

    boolean existsByFileId(String fileId);
}
