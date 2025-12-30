package com.ricky.upload.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.properties.FileProperties;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.utils.FileUtils;
import com.ricky.file.domain.*;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
import com.ricky.upload.domain.UploadChunkCleaner;
import com.ricky.upload.domain.UploadSession;
import com.ricky.upload.domain.UploadSessionRepository;
import com.ricky.upload.domain.dto.cmd.CompleteUploadCommand;
import com.ricky.upload.domain.dto.cmd.InitUploadCommand;
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
    private final FileRepository fileRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final UploadChunkCleaner uploadChunkCleaner;

    @Override
    @Transactional
    public FileUploadResponse upload(MultipartFile multipartFile, String parentId, String path, UserContext userContext) {
        if (isBlank(multipartFile.getOriginalFilename())) {
            throw new MyException(FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK,
                    "文件原始名称不能为空", "filename", multipartFile.getName());
        }

        // 存储文件内容
        String hash = fileHasherFactory.getFileHasher().hash(multipartFile);
        StorageId storageId = fileRepository.listByFileHash(hash)
                .stream()
                .findFirst()
                .map(File::getStorageId)
                .orElseGet(() -> fileStorage.store(multipartFile));

        // 落库聚合根
        File file = fileFactory.create(parentId, path, storageId, multipartFile, hash, userContext);
        fileRepository.save(file);

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
        List<File> dbFiles = fileRepository.listByFileHash(command.getFileHash());
        if (isNotEmpty(dbFiles)) { // 文件已存在
            StorageId storageId = dbFiles.get(0).getStorageId();
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
                    command.getPath(),
                    command.getFilename(),
                    command.getStorageId(),
                    command.getFileHash(),
                    command.getTotalSize(),
                    userContext
            );
            fileRepository.save(file);
            return FileUploadResponse.builder().fileId(file.getId()).build();
        }

        // 分片路径
        UploadSession session = uploadSessionRepository.byId(command.getUploadId());
        session.checkAllChunksUploaded();

        Path chunkDir = Paths.get(fileProperties.getUpload().getChunkDir(), session.getId());
        StoredFile storedFile = fileStorage.mergeChunksAndStore(session, chunkDir);

        File file = fileFactory.create(
                command.getParentId(),
                command.getPath(),
                session.getFilename(),
                storedFile,
                userContext
        );
        fileRepository.save(file);

        session.complete(userContext);
        uploadSessionRepository.save(session);

        // 事务commit成功时，清理分片目录
        uploadChunkCleaner.cleanAfterCommit(uploadSession.getId());

        log.info("File [{}] upload completed via chunks", file.getId());
        return FileUploadResponse.builder().fileId(file.getId()).build();
    }
}
