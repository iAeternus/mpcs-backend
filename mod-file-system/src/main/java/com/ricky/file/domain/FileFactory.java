package com.ricky.file.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileFactory {

    private final FolderHierarchyRepository folderHierarchyRepository;

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
                getPath(parentId, userContext),
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
                getPath(parentId, userContext),
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
                getPath(parentId, userContext),
                userContext
        );
    }

    private String getPath(String parentId, UserContext userContext) {
        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(userContext.getUid());
        return hierarchy.schemaOf(parentId);
    }
}
