package com.ricky.apitest.upload;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileStatus;
import com.ricky.upload.command.CompleteUploadCommand;
import com.ricky.upload.command.FileUploadResponse;
import com.ricky.upload.command.InitUploadCommand;
import com.ricky.upload.command.InitUploadResponse;
import com.ricky.upload.command.UploadChunkResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.ricky.apitest.RandomTestFixture.rFolderName;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_NAME_DUPLICATES;
import static com.ricky.common.exception.ErrorCodeEnum.MERGE_CHUNKS_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.REQUEST_VALIDATION_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.UPLOAD_ALREADY_COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUploadControllerTest extends BaseApiTest {

    @Test
    void should_upload_file() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        FileUploadResponse resp = FileUploadApi.upload(manager.getJwt(), file, parentId);

        File dbFile = fileRepository.byId(resp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(file.length(), dbFile.getSize());
    }

    @Test
    void should_upload_file_if_hash_already_exist() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();

        String parentId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String parentId2 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        FileUploadResponse resp = FileUploadApi.upload(manager.getJwt(), file, parentId1);
        FileUploadResponse resp2 = FileUploadApi.upload(manager.getJwt(), file, parentId2);

        File dbFile = fileRepository.byId(resp.getFileId());
        File dbFile2 = fileRepository.byId(resp2.getFileId());
        assertEquals(dbFile.getStorageId(), dbFile2.getStorageId());
    }

    @Test
    void should_fail_to_upload_file_if_file_name_duplicates_at_same_folder() throws IOException {
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        java.io.File originalFile = ctx.getOriginalFile();
        String parentId = ctx.getParentId();

        assertError(() -> FileUploadApi.uploadRaw(manager.getJwt(), originalFile, parentId), FILE_NAME_DUPLICATES);
    }

    @Test
    void should_upload_large_file_by_chunks() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/BadApple.mp4");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        int chunkSize = fileProperties.getUpload().getChunkSize();
        long totalSize = file.length();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        String fileHash = setupApi.deleteFileWithSameHash(file);

        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager.getJwt(),
                InitUploadCommand.builder()
                        .parentId(parentId)
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(totalSize)
                        .chunkSize(chunkSize)
                        .totalChunks(totalChunks)
                        .build()
        );

        assertFalse(initResp.isUploaded());
        assertNotNull(initResp.getUploadId());
        assertNotNull(initResp.getUploadedChunks());
        String uploadId = initResp.getUploadId();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];

            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                assertTrue(read > 0);

                byte[] chunkBytes = (read == chunkSize) ? buffer : Arrays.copyOf(buffer, read);

                UploadChunkResponse chunkResp = FileUploadApi.uploadChunk(
                        manager.getJwt(),
                        uploadId,
                        chunkIndex,
                        chunkBytes
                );

                assertEquals(chunkIndex, chunkResp.getChunkIndex());
            }
        }

        FileUploadResponse completeResp = FileUploadApi.completeUpload(
                manager.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(uploadId)
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(totalSize)
                        .build()
        );

        File dbFile = fileRepository.byId(completeResp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(totalSize, dbFile.getSize());
    }

    @Test
    void should_fast_upload_when_hash_exists() throws IOException {
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String fileHash = ctx.getFileHash();
        java.io.File originalFile = ctx.getOriginalFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), ctx.getCustomId(), rFolderName());

        File dbFile2 = fileRepository.byId(fileId);
        String fileHash2 = dbFile2.getHash();
        assertEquals(fileHash, fileHash2);

        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager.getJwt(),
                InitUploadCommand.builder()
                        .parentId(parentId)
                        .fileName(originalFile.getName())
                        .fileHash(fileHash)
                        .totalSize(originalFile.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(1)
                        .build()
        );

        assertTrue(initResp.isUploaded());
        assertNotNull(initResp.getFileId());
        assertNull(initResp.getUploadId());
        assertNull(initResp.getUploadedChunks());

        File dbFile = fileRepository.byId(fileId);
        assertEquals(dbFile.getStorageId().getValue(), initResp.getStorageId());

        File fastFile = fileRepository.byId(initResp.getFileId());
        assertEquals(parentId, fastFile.getParentId());
        assertEquals(fileHash, fastFile.getHash());
    }

    @Test
    void should_fail_when_complete_upload_twice() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);
        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager.getJwt(),
                InitUploadCommand.builder()
                        .parentId(parentId)
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(1)
                        .build()
        );

        String uploadId = initResp.getUploadId();

        FileUploadApi.uploadChunk(manager.getJwt(), uploadId, 0, Files.readAllBytes(file.toPath()));

        FileUploadApi.completeUpload(
                manager.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(uploadId)
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .build()
        );

        assertError(() -> FileUploadApi.completeUploadRaw(manager.getJwt(), CompleteUploadCommand.builder()
                .uploadId(uploadId)
                .parentId(parentId)
                .fileHash(fileHash)
                .totalSize(file.length())
                .build()
        ), UPLOAD_ALREADY_COMPLETED);
    }

    @Test
    void should_fail_complete_when_chunks_missing() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager.getJwt(),
                InitUploadCommand.builder()
                        .parentId(parentId)
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(2)
                        .build()
        );

        FileUploadApi.uploadChunk(
                manager.getJwt(),
                initResp.getUploadId(),
                0,
                Files.readAllBytes(file.toPath())
        );

        assertError(() -> FileUploadApi.completeUploadRaw(manager.getJwt(), CompleteUploadCommand.builder()
                .uploadId(initResp.getUploadId())
                .parentId(parentId)
                .fileHash(fileHash)
                .totalSize(file.length())
                .build()
        ), MERGE_CHUNKS_FAILED);
    }

    @Test
    void should_fail_to_init_upload_if_filename_invalid() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());
        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        InitUploadCommand command = InitUploadCommand.builder()
                .parentId(parentId)
                .fileName("large/file.png")
                .fileHash(fileHash)
                .totalSize(file.length())
                .chunkSize(fileProperties.getUpload().getChunkSize())
                .totalChunks(1)
                .build();

        assertError(() -> FileUploadApi.initUploadRaw(manager.getJwt(), command), REQUEST_VALIDATION_FAILED);
    }

}
