package com.ricky.file.domain;

import com.ricky.common.domain.user.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileFactory {

    public File create(String parentId,
                       String path,
                       StorageId storageId,
                       MultipartFile file,
                       String hash,
                       UserContext userContext) {
        return File.create(
                userContext.getUid(),
                parentId,
                storageId,
                file.getOriginalFilename(),
                file.getSize(),
                hash,
                path,
                userContext
        );
    }

    public File create(String parentId,
                       String path,
                       String filename,
                       StoredFile storedFile,
                       UserContext userContext) {
        return File.create(
                userContext.getUid(),
                parentId,
                storedFile.getStorageId(),
                filename,
                storedFile.getSize(),
                storedFile.getHash(),
                path,
                userContext
        );
    }

    public File create(String parentId,
                       String path,
                       String filename,
                       StorageId storageId,
                       String fileHash,
                       long totalSize,
                       UserContext userContext) {
        return File.create(
                userContext.getUid(),
                parentId,
                storageId,
                filename,
                totalSize,
                fileHash,
                path,
                userContext
        );
    }
}
