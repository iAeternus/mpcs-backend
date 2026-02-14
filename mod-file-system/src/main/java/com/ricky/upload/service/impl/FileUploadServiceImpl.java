package com.ricky.upload.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.properties.FileProperties;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileFactory;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.upload.command.*;
import com.ricky.upload.domain.FileUploadDomainService;
import com.ricky.upload.domain.StorageService;
import com.ricky.upload.domain.UploadSession;
import com.ricky.upload.domain.UploadSessionRepository;
import com.ricky.upload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final RateLimiter rateLimiter;
    private final FileProperties fileProperties;
    private final FileHasherFactory fileHasherFactory;
    private final FileFactory fileFactory;
    private final StorageService storageService;
    private final FileUploadDomainService fileUploadDomainService;
    private final FileRepository fileRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final FolderRepository folderRepository;
    private final FileExtraRepository fileExtraRepository;

    @Override
    @Transactional
    public FileUploadResponse upload(MultipartFile multipartFile, String parentId, UserContext userContext) {
        rateLimiter.applyFor("Upload:Upload", 10);

        if (isBlank(multipartFile.getOriginalFilename())) {
            throw new MyException(FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK,
                    "File original name must not be blank.", "filename", multipartFile.getName());
        }

        fileUploadDomainService.checkFilenameDuplicates(parentId, multipartFile.getOriginalFilename());

        String hash = fileHasherFactory.getFileHasher().hash(multipartFile);
        StorageId storageId = fileRepository.byFileHashOptional(hash)
                .orElseGet(() -> storageService.store(multipartFile));

        File file = fileFactory.create(parentId, storageId, multipartFile, hash, userContext);
        fileRepository.save(file);

        attachToFolder(file, parentId, userContext);
        fileExtraRepository.save(new FileExtra(file.getId(), userContext));

        log.info("File[{}] upload complete", file.getId());
        return FileUploadResponse.builder()
                .fileId(file.getId())
                .build();
    }

    @Override
    @Transactional
    public InitUploadResponse initUpload(InitUploadCommand command, UserContext userContext) {
        rateLimiter.applyFor("Upload:InitUpload", 10);

        Optional<File> existingFile = findExistingFile(command.getFileHash(), command.getParentId(), command.getFileName());
        if (existingFile.isPresent()) {
            return InitUploadResponse.fastUploaded(
                    existingFile.get().getId(),
                    existingFile.get().getStorageId().getValue()
            );
        }

        fileUploadDomainService.checkFilenameDuplicates(command.getParentId(), command.getFileName());

        Optional<StorageId> storageId = fileRepository.byFileHashOptional(command.getFileHash());
        if (storageId.isPresent()) {
            File file = fileFactory.create(
                    command.getParentId(),
                    command.getFileName(),
                    storageId.get(),
                    command.getFileHash(),
                    command.getTotalSize(),
                    userContext
            );
            fileRepository.save(file);
            attachToFolder(file, command.getParentId(), userContext);
            fileExtraRepository.save(new FileExtra(file.getId(), userContext));
            return InitUploadResponse.fastUploaded(file.getId(), storageId.get().getValue());
        }

        UploadSession uploadSession = uploadSessionRepository
                .byFileHashAndOwnerIdOptional(command.getFileHash(), userContext.getUid())
                .orElseGet(() -> {
                    UploadSession session = UploadSession.create(
                            userContext.getUid(),
                            command.getFileName(),
                            command.getFileHash(),
                            command.getTotalSize(),
                            command.getChunkSize(),
                            command.getTotalChunks(),
                            userContext
                    );
                    uploadSessionRepository.save(session);
                    return session;
                });

        log.info("Init file upload via chunks with UploadSession[{}]", uploadSession.getId());
        return InitUploadResponse.notFastUploaded(uploadSession.getId(), uploadSession.getUploadedChunks());
    }

    @Override
    @Transactional
    public UploadChunkResponse uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunk, UserContext userContext) {
        rateLimiter.applyFor("Upload:UploadChunk", 50);

        UploadSession uploadSession = uploadSessionRepository.byId(uploadId);
        if (!uploadSession.getOwnerId().equals(userContext.getUid())) {
            throw MyException.accessDeniedException();
        }
        if (uploadSession.isCompleted()) {
            throw new MyException(UPLOAD_ALREADY_COMPLETED, "Chunk upload has been completed.");
        }

        if (!uploadSession.containsUploadedChunk(chunkIndex)) {
            uploadSession.saveChunk(chunkIndex, chunk, fileProperties.getUpload().getChunkDir());
            uploadSessionRepository.save(uploadSession);
        }

        log.info("Chunk[{}] upload complete for UploadSession[{}]", chunkIndex, uploadId);
        return UploadChunkResponse.builder().chunkIndex(chunkIndex).build();
    }

    @Override
    @Transactional
    public FileUploadResponse completeUpload(CompleteUploadCommand command, UserContext userContext) {
        rateLimiter.applyFor("Upload:UploadChunk", 10);

        if (command.isChunkUpload()) {
            UploadSession uploadSession = uploadSessionRepository.byId(command.getUploadId());
            if (!uploadSession.getOwnerId().equals(userContext.getUid())) {
                throw MyException.accessDeniedException();
            }
            if (uploadSession.isCompleted()) {
                throw new MyException(UPLOAD_ALREADY_COMPLETED, "Chunk upload has been completed.");
            }

            fileUploadDomainService.checkFilenameDuplicates(command.getParentId(), uploadSession.getFilename());
            uploadSession.checkHash(command.getFileHash());
            uploadSession.checkAllChunksUploaded();

            Path chunkDir = Paths.get(fileProperties.getUpload().getChunkDir(), uploadSession.getId());
            StoredFile storedFile = storageService.mergeChunksAndStore(uploadSession, chunkDir);
            uploadSession.checkHash(storedFile.getHash());

            File file = fileFactory.create(
                    command.getParentId(),
                    uploadSession.getFilename(),
                    storedFile,
                    userContext
            );
            fileRepository.save(file);

            attachToFolder(file, command.getParentId(), userContext);
            fileExtraRepository.save(new FileExtra(file.getId(), userContext));

            uploadSession.complete(userContext);
            uploadSessionRepository.save(uploadSession);

            log.info("File [{}] upload completed via chunks", file.getId());
            return FileUploadResponse.builder().fileId(file.getId()).build();
        }

        if (isBlank(command.getFileName())) {
            throw MyException.requestValidationException("fileName", command.getFileName());
        }

        Optional<File> existingFile = findExistingFile(command.getFileHash(), command.getParentId(), command.getFileName());
        if (existingFile.isPresent()) {
            return FileUploadResponse.builder().fileId(existingFile.get().getId()).build();
        }

        fileUploadDomainService.checkFilenameDuplicates(command.getParentId(), command.getFileName());

        StorageId storageId = fileRepository.byFileHashOptional(command.getFileHash())
                .orElseThrow(() -> new MyException(FILE_NOT_FOUND, "File content not found", "fileHash", command.getFileHash()));

        File file = fileFactory.create(
                command.getParentId(),
                command.getFileName(),
                storageId,
                command.getFileHash(),
                command.getTotalSize(),
                userContext
        );
        fileRepository.save(file);
        attachToFolder(file, command.getParentId(), userContext);
        fileExtraRepository.save(new FileExtra(file.getId(), userContext));
        return FileUploadResponse.builder().fileId(file.getId()).build();
    }

    private Optional<File> findExistingFile(String fileHash, String parentId, String filename) {
        List<File> files = fileRepository.listByFileHash(fileHash);
        return files.stream()
                .filter(file -> parentId.equals(file.getParentId()) && filename.equals(file.getFilename()))
                .findFirst();
    }

    private void attachToFolder(File file, String parentId, UserContext userContext) {
        Folder parentFolder = folderRepository.byId(parentId);
        parentFolder.addFile(file.getId(), userContext);
        folderRepository.save(parentFolder);
    }
}
