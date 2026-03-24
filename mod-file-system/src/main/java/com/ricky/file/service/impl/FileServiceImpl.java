package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.domain.*;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.query.DownloadFileResponse;
import com.ricky.file.service.FileService;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.upload.domain.StorageService;
import com.ricky.upload.domain.UploadSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final RateLimiter rateLimiter;
    private final MimeTypeResolver mimeTypeResolver;
    private final StorageService storageService;
    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final UploadSessionRepository userUploadSessionRepository;

    @Override
    @Transactional
    public void renameFile(String fileId, RenameFileCommand command, UserContext userContext) {
        rateLimiter.applyFor("File:RenameFile", 10);

        File file = fileRepository.byId(fileId);
        file.rename(command.getNewName(), userContext);
        fileRepository.save(file);

        log.info("Renamed file[{}]", fileId);
    }

    @Override
    @Transactional
    public void deleteFileForce(String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:DeleteFile", 10);

        File file = fileRepository.byId(fileId);
        String fileHash = file.getHash();
        String ownerId = file.getUserId();
        file.onDelete(userContext);
        fileRepository.delete(file);
        fileDomainService.deleteFileForce(file, userContext);

        Folder folder = folderRepository.byId(file.getParentId());
        folder.removeFile(file.getId(), userContext);
        folderRepository.save(folder);

        if (fileHash != null && ownerId != null) {
            userUploadSessionRepository.deleteByFileHashAndOwnerId(fileHash, ownerId);
        }

        log.info("Deleted File[{}] force", fileId);
    }

    @Override
    @Transactional
    public void moveFile(MoveFileCommand command, UserContext userContext) {
        rateLimiter.applyFor("File:MoveFile", 10);

        File file = fileRepository.byId(command.getFileId());
        fileDomainService.checkFileNameDuplicates(file, command.getNewParentId());

        Folder parentFolder = folderRepository.byId(file.getParentId());
        parentFolder.removeFile(file.getId(), userContext);
        folderRepository.save(parentFolder);

        file.updateParentId(command.getNewParentId(), userContext);
        fileRepository.save(file);

        Folder newParentFolder = folderRepository.byId(command.getNewParentId());
        newParentFolder.addFile(file.getId(), userContext);
        folderRepository.save(newParentFolder);
    }

    @Override
    public DownloadFileResponse download(String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:Download", 5);

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
    public DownloadFileResponse preview(String fileId, UserContext userContext, long rangeStart, long rangeEnd, long fileSize) {
        rateLimiter.applyFor("File:Preview", 5);

        File file = fileRepository.cachedById(fileId);

        FileExtension extension = FileExtension.fromFilename(file.getFilename());
        String contentType = mimeTypeResolver.resolve(extension);

        StorageId storageId = file.getStorageId();

        long actualSize = storageService.getObjectSize(storageId);
        if (actualSize != fileSize) {
            log.warn("File[{}] size mismatch: db={}, storage={}", fileId, fileSize, actualSize);
        }

        long effectiveRangeStart = Math.min(rangeStart, actualSize - 1);
        long effectiveRangeEnd = Math.min(rangeEnd, actualSize - 1);
        long length = effectiveRangeEnd - effectiveRangeStart + 1;

        InputStream inputStream = storageService.getFileStream(storageId, effectiveRangeStart, length);
        InputStreamResource resource = new InputStreamResource(inputStream);

        return DownloadFileResponse.builder()
                .filename(file.getFilename())
                .contentType(contentType)
                .size(length)
                .resource(resource)
                .build();
    }
}
