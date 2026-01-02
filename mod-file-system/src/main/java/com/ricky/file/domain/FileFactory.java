package com.ricky.file.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileFactory {

    public File create(String parentId,
                       StorageId storageId,
                       MultipartFile file,
                       String hash,
                       UserContext userContext) {
        return File.create(
                parentId,
                storageId,
                file.getOriginalFilename(),
                file.getSize(),
                hash,
                userContext
        );
    }

    public File create(String parentId,
                       String filename,
                       StoredFile storedFile,
                       UserContext userContext) {
        return File.create(
                parentId,
                storedFile.getStorageId(),
                filename,
                storedFile.getSize(),
                storedFile.getHash(),
                userContext
        );
    }

    public File create(String parentId,
                       String filename,
                       StorageId storageId,
                       String fileHash,
                       long totalSize,
                       UserContext userContext) {
        return File.create(
                parentId,
                storageId,
                filename,
                totalSize,
                fileHash,
                userContext
        );
    }
}
