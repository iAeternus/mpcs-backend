package com.ricky.upload.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.properties.FileProperties;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.*;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.upload.domain.*;
import com.ricky.upload.domain.dto.cmd.CompleteUploadCommand;
import com.ricky.upload.domain.dto.cmd.InitUploadCommand;
import com.ricky.upload.domain.dto.resp.FileUploadResponse;
import com.ricky.upload.domain.dto.resp.InitUploadResponse;
import com.ricky.upload.domain.dto.resp.UploadChunkResponse;
import com.ricky.upload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK;
import static com.ricky.common.exception.ErrorCodeEnum.UPLOAD_ALREADY_COMPLETED;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.utils.ValidationUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final RateLimiter rateLimiter;
    private final FileProperties fileProperties;
    private final FileHasherFactory fileHasherFactory;
    private final FileFactory fileFactory;
    private final FileStorage fileStorage;
    private final UploadChunkCleaner uploadChunkCleaner;
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
                    "文件原始名称不能为空。", "filename", multipartFile.getName());
        }

        // 校验重名
        fileUploadDomainService.checkFilenameDuplicates(parentId, multipartFile.getOriginalFilename());

        // 存储文件内容
        String hash = fileHasherFactory.getFileHasher().hash(multipartFile);
        StorageId storageId = fileRepository.cachedByFileHash(hash).getStorageIds().stream()
                .findFirst()
                .orElseGet(() -> fileStorage.store(multipartFile));

        // 落库聚合根
        File file = fileFactory.create(parentId, storageId, multipartFile, hash, userContext);
        fileRepository.save(file);

        Folder parentFolder = folderRepository.cachedById(parentId);
        parentFolder.addFile(file.getId());
        folderRepository.save(parentFolder);

        FileExtra fileExtra = new FileExtra(file.getId(), userContext);
        fileExtraRepository.save(fileExtra);

        log.info("File[{}] upload complete", file.getId());
        return FileUploadResponse.builder()
                .fileId(file.getId())
                .build();
    }

    @Override
    @Transactional
    public InitUploadResponse initUpload(InitUploadCommand command, UserContext userContext) {
        rateLimiter.applyFor("Upload:InitUpload", 10);

        // 秒传判断
        List<StorageId> storageIds = fileRepository.cachedByFileHash(command.getFileHash()).getStorageIds();
        if (isNotEmpty(storageIds)) { // 文件已存在
            StorageId storageId = storageIds.get(0);
            return InitUploadResponse.fastUploaded(storageId);
        }

        // 查找UploadSession，不存在则创建
        UploadSession uploadSession = uploadSessionRepository.byFileHashAndOwnerIdOptional(command.getFileHash(), userContext.getUid())
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

        return InitUploadResponse.notFastUploaded(uploadSession.getId(), uploadSession.getUploadedChunks());
    }

    @Override
    @Transactional
    public UploadChunkResponse uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunk, UserContext userContext) {
        rateLimiter.applyFor("Upload:UploadChunk", 50);

        UploadSession uploadSession = uploadSessionRepository.byId(uploadId);
        if (uploadSession.isCompleted()) {
            throw new MyException(UPLOAD_ALREADY_COMPLETED, "Chunk upload has been completed.");
        }

        // 保证幂等性
        if (!uploadSession.containsUploadedChunk(chunkIndex)) {
            uploadSession.saveChunk(chunkIndex, chunk, fileProperties.getUpload().getChunkDir());
            uploadSessionRepository.save(uploadSession);
        }

        return UploadChunkResponse.builder().chunkIndex(chunkIndex).build();
    }

    @Override
    @Transactional
    public FileUploadResponse completeUpload(CompleteUploadCommand command, UserContext userContext) {
        rateLimiter.applyFor("Upload:UploadChunk", 10);

        UploadSession uploadSession = uploadSessionRepository.byId(command.getUploadId());
        if (uploadSession.isCompleted()) {
            throw new MyException(UPLOAD_ALREADY_COMPLETED, "Chunk upload has been completed.");
        }

        // 秒传路径
        if (command.isFastUpload()) {
            File file = fileFactory.create(
                    command.getParentId(),
                    uploadSession.getFilename(),
                    command.getStorageId(),
                    command.getFileHash(),
                    command.getTotalSize(),
                    userContext
            );
            fileRepository.save(file);
            return FileUploadResponse.builder().fileId(file.getId()).build();
        }

        // 分片路径
        uploadSession.checkAllChunksUploaded();

        Path chunkDir = Paths.get(fileProperties.getUpload().getChunkDir(), uploadSession.getId());
        StoredFile storedFile = fileStorage.mergeChunksAndStore(uploadSession, chunkDir);

        File file = fileFactory.create(
                command.getParentId(),
                uploadSession.getFilename(),
                storedFile,
                userContext
        );
        fileRepository.save(file);

        Folder parentFolder = folderRepository.cachedById(command.getParentId());
        parentFolder.addFile(file.getId());
        folderRepository.save(parentFolder);

        FileExtra fileExtra = new FileExtra(file.getId(), userContext);
        fileExtraRepository.save(fileExtra);

        uploadSession.complete(userContext);
        uploadSessionRepository.save(uploadSession);

        // 事务commit成功时，清理分片目录
        uploadChunkCleaner.cleanAfterCommit(uploadSession.getId());

        log.info("File [{}] upload completed via chunks", file.getId());
        return FileUploadResponse.builder().fileId(file.getId()).build();
    }
}
