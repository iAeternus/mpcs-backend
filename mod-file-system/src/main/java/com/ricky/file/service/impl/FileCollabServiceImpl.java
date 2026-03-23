package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileExtension;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.MimeTypeResolver;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.query.DownloadFileResponse;
import com.ricky.file.service.FileCollabService;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.upload.domain.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCollabServiceImpl implements FileCollabService {

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final MimeTypeResolver mimeTypeResolver;
    private final FileHasherFactory fileHasherFactory;

    @Override
    public DownloadFileResponse getFileContent(String fileId, UserContext userContext) {
        File file = fileRepository.cachedById(fileId);

        FileExtension extension = FileExtension.fromFilename(file.getFilename());
        String contentType = mimeTypeResolver.resolve(extension);

        StorageId storageId = file.getStorageId();
        InputStream inputStream = storageService.getFileStream(storageId);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return DownloadFileResponse.builder()
                .filename(file.getFilename())
                .contentType(contentType)
                .size(file.getSize())
                .resource(resource)
                .build();
    }

    @Override
    @Transactional
    public void saveFileContent(String fileId, String parentId, MultipartFile file, String filename, UserContext userContext) {
        File existingFile = fileRepository.byId(fileId);

        String newHash = fileHasherFactory.getFileHasher().hash(file);

        StorageId currentStorageId = existingFile.getStorageId();
        boolean hashChanged = !currentStorageId.getValue().equals(newHash);

        if (hashChanged) {
            StorageId newStorageId = storageService.store(file);

            existingFile.updateStorageId(newStorageId, file.getSize(), newHash, userContext);
            log.info("Collaborative edit: created new storage for file[{}], new hash[{}]", fileId, newHash);
        } else {
            log.info("Collaborative edit: hash unchanged for file[{}], skipping storage update", fileId);
        }

        fileRepository.save(existingFile);
        log.info("Collaborative edit: saved file[{}] content", fileId);
    }
}
