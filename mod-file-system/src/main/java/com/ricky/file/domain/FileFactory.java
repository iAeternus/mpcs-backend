package com.ricky.file.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileFactory {

    public File create(String parentId,
                       StorageId storageId,
                       MultipartFile file,
                       String hash,
                       UserContext userContext) {
        FileCategory category = FileCategory.fromFilename(file.getOriginalFilename());
        return new File(
                parentId,
                storageId,
                file.getOriginalFilename(),
                file.getSize(),
                hash,
                category,
                userContext
        );
    }

    public File create(String parentId,
                       String filename,
                       StoredFile storedFile,
                       UserContext userContext) {
        FileCategory category = FileCategory.fromFilename(filename);
        return new File(
                parentId,
                storedFile.getStorageId(),
                filename,
                storedFile.getSize(),
                storedFile.getHash(),
                category,
                userContext
        );
    }

    public File create(String parentId,
                       String filename,
                       StorageId storageId,
                       String fileHash,
                       long totalSize,
                       UserContext userContext) {
        FileCategory category = FileCategory.fromFilename(filename);
        return new File(
                parentId,
                storageId,
                filename,
                totalSize,
                fileHash,
                category,
                userContext
        );
    }
}
