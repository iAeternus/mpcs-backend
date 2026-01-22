package com.ricky.apitest.upload;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileStatus;
import com.ricky.apitest.folder.FolderApi;
import com.ricky.upload.command.*;
import com.ricky.upload.domain.event.FileUploadedEvent;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.ricky.apitest.RandomTestFixture.rFolderName;
import static com.ricky.common.event.DomainEventType.FILE_UPLOADED;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

class FileUploadControllerTest extends BaseApiTest {

    @Test
    void should_upload_file() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // When
        FileUploadResponse resp = FileUploadApi.upload(loginResponse.getJwt(), file, parentId);

        // Then
        File dbFile = fileRepository.byId(resp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(file.length(), dbFile.getSize());

        GridFSFile gridFSFile = storageService.findFile(dbFile.getStorageId());
        assertEquals(new ObjectId(dbFile.getStorageId().getValue()), gridFSFile.getObjectId());

        FileUploadedEvent evt = latestEventFor(resp.getFileId(), FILE_UPLOADED, FileUploadedEvent.class);
        assertEquals(resp.getFileId(), evt.getFileId());
    }

    @Test
    void should_upload_file_if_hash_already_exist() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();

        String parentId1 = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());
        String parentId2 = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        // When
        // 先上传文件，抢占 storageId
        FileUploadResponse resp = FileUploadApi.upload(loginResponse.getJwt(), file, parentId1);
        FileUploadResponse resp2 = FileUploadApi.upload(loginResponse.getJwt(), file, parentId2);

        // Then
        File dbFile = fileRepository.byId(resp.getFileId());
        File dbFile2 = fileRepository.byId(resp2.getFileId());
        assertEquals(dbFile.getStorageId(), dbFile2.getStorageId()); // 两条记录指向同一个 storageId
    }

    @Test
    void should_fail_to_upload_file_if_file_name_duplicates_at_same_folder() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        java.io.File originalFile = ctx.getOriginalFile();
        String parentId = ctx.getParentId();

        // When & Then
        assertError(() -> FileUploadApi.uploadRaw(manager.getJwt(), originalFile, parentId), FILE_NAME_DUPLICATES);
    }

    @Test
    void should_upload_large_file_by_chunks() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        int chunkSize = fileProperties.getUpload().getChunkSize();
        long totalSize = file.length();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // When init upload
        InitUploadResponse initResp = FileUploadApi.initUpload(
                loginResponse.getJwt(),
                InitUploadCommand.builder()
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(totalSize)
                        .chunkSize(chunkSize)
                        .totalChunks(totalChunks)
                        .build()
        );

        // Then
        assertFalse(initResp.isUploaded());
        assertNotNull(initResp.getUploadId());
        assertNotNull(initResp.getUploadedChunks());
        String uploadId = initResp.getUploadId();

        // When upload chunk
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];

            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                assertTrue(read > 0);

                byte[] chunkBytes = (read == chunkSize) ? buffer : Arrays.copyOf(buffer, read);

                UploadChunkResponse chunkResp = FileUploadApi.uploadChunk(
                        loginResponse.getJwt(),
                        uploadId,
                        chunkIndex,
                        chunkBytes
                );

                assertEquals(chunkIndex, chunkResp.getChunkIndex());
            }
        }

        // When complete upload
        FileUploadResponse completeResp = FileUploadApi.completeUpload(
                loginResponse.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(uploadId) // 分片路径
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(totalSize)
                        .build()
        );

        // Then
        File dbFile = fileRepository.byId(completeResp.getFileId());

        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(totalSize, dbFile.getSize());

        GridFSFile gridFSFile = storageService.findFile(dbFile.getStorageId());
        assertEquals(new ObjectId(dbFile.getStorageId().getValue()), gridFSFile.getObjectId());
    }

    @Test
    void should_fast_upload_when_hash_exists() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt"); // 先上传文件，抢占 storageId
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String fileHash = ctx.getFileHash();
        java.io.File originalFile = ctx.getOriginalFile();

        File dbFile2 = fileRepository.byId(fileId);
        String fileHash2 = dbFile2.getHash();
        assertEquals(fileHash, fileHash2);

        // When init upload
        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager.getJwt(),
                InitUploadCommand.builder()
                        .fileName(originalFile.getName())
                        .fileHash(fileHash)
                        .totalSize(originalFile.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(1)
                        .build()
        );

        // Then
        assertTrue(initResp.isUploaded());
        assertNull(initResp.getUploadId());
        assertNull(initResp.getUploadedChunks());

        File dbFile = fileRepository.byId(fileId);
        assertEquals(dbFile.getStorageId(), initResp.getStorageId());
    }

    @Test
    void should_fail_when_complete_upload_twice() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);
        InitUploadResponse initResp = FileUploadApi.initUpload(
                loginResponse.getJwt(),
                InitUploadCommand.builder()
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(1)
                        .build()
        );

        String uploadId = initResp.getUploadId();

        FileUploadApi.uploadChunk(loginResponse.getJwt(), uploadId, 0, Files.readAllBytes(file.toPath()));

        FileUploadApi.completeUpload(
                loginResponse.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(uploadId)
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .build()
        );

        // When & Then
        assertError(() -> FileUploadApi.completeUploadRaw(loginResponse.getJwt(), CompleteUploadCommand.builder()
                .uploadId(uploadId)
                .parentId(parentId)
                .fileHash(fileHash)
                .totalSize(file.length())
                .build()
        ), UPLOAD_ALREADY_COMPLETED);
    }

    @Test
    void should_fail_complete_when_chunks_missing() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        InitUploadResponse initResp = FileUploadApi.initUpload(
                loginResponse.getJwt(),
                InitUploadCommand.builder()
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(2)
                        .build()
        );

        // 只上传一个 chunk
        FileUploadApi.uploadChunk(
                loginResponse.getJwt(),
                initResp.getUploadId(),
                0,
                Files.readAllBytes(file.toPath())
        );

        // When & Then
        assertError(() -> FileUploadApi.completeUploadRaw(loginResponse.getJwt(), CompleteUploadCommand.builder()
                .uploadId(initResp.getUploadId())
                .parentId(parentId)
                .fileHash(fileHash)
                .totalSize(file.length())
                .build()
        ), MERGE_CHUNKS_FAILED);
    }

    @Test
    void should_fail_to_init_upload_if_filename_invalid() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();

        String fileHash = setupApi.deleteFileWithSameHash(file);

        InitUploadCommand command = InitUploadCommand.builder()
                .fileName("large/file.png")
                .fileHash(fileHash)
                .totalSize(file.length())
                .chunkSize(fileProperties.getUpload().getChunkSize())
                .totalChunks(1)
                .build();

        // When & Then
        assertError(() -> FileUploadApi.initUploadRaw(loginResponse.getJwt(), command), REQUEST_VALIDATION_FAILED);
    }

}