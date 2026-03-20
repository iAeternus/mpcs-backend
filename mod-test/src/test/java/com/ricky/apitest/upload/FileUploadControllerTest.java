package com.ricky.apitest.upload;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.file.FileApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileStatus;
import com.ricky.upload.command.*;
import com.ricky.upload.domain.UploadSession;
import com.ricky.upload.domain.UploadStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.ricky.apitest.RandomTestFixture.rFolderName;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

class FileUploadControllerTest extends BaseApiTest {

    @Test
    void should_upload_file() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // When
        FileUploadResponse resp = FileUploadApi.upload(manager.getJwt(), file, parentId);

        // Then
        File dbFile = fileRepository.byId(resp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(file.length(), dbFile.getSize());
    }

    @Test
    void should_upload_file_if_hash_already_exist() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();

        String parentId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String parentId2 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        // When
        FileUploadResponse resp = FileUploadApi.upload(manager.getJwt(), file, parentId1);
        FileUploadResponse resp2 = FileUploadApi.upload(manager.getJwt(), file, parentId2);

        // Then
        File dbFile = fileRepository.byId(resp.getFileId());
        File dbFile2 = fileRepository.byId(resp2.getFileId());
        assertEquals(dbFile.getStorageId(), dbFile2.getStorageId());
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
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/BadApple.mp4");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        int chunkSize = fileProperties.getUpload().getChunkSize();
        long totalSize = file.length();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // When init upload
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

        // Then init response
        assertFalse(initResp.isUploaded());
        assertNotNull(initResp.getUploadId());
        assertNotNull(initResp.getUploadedChunks());
        assertTrue(initResp.getUploadedChunks().isEmpty());

        String uploadId = initResp.getUploadId();

        // Then init session state
        UploadSession initSession = uploadSessionRepository.byId(uploadId);
        assertEquals(UploadStatus.INIT, initSession.getStatus());
        assertEquals(totalChunks, initSession.getTotalChunks());
        assertEquals(totalSize, initSession.getTotalSize());
        assertEquals(file.getName(), initSession.getFilename());
        assertEquals(fileHash, initSession.getExpectedHash());
        assertEquals(manager.getUserId(), initSession.getOwnerId());
        assertTrue(initSession.getUploadedChunks().isEmpty());

        // When upload all chunks
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];

            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                assertTrue(read > 0);

                byte[] chunkBytes = Arrays.copyOf(buffer, read);

                UploadChunkResponse chunkResp = FileUploadApi.uploadChunk(
                        manager.getJwt(),
                        uploadId,
                        chunkIndex,
                        chunkBytes
                );

                assertEquals(chunkIndex, chunkResp.getChunkIndex());
            }
        }

        // Then session state after all chunks uploaded
        UploadSession uploadingSession = uploadSessionRepository.byId(uploadId);
        assertEquals(UploadStatus.UPLOADING, uploadingSession.getStatus());
        assertEquals(totalChunks, uploadingSession.getUploadedChunks().size());
        for (int i = 0; i < totalChunks; i++) {
            assertTrue(uploadingSession.containsUploadedChunk(i), "Chunk " + i + " should be uploaded");
        }

        // When complete upload
        FileUploadResponse completeResp = FileUploadApi.completeUpload(
                manager.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(uploadId)
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(totalSize)
                        .build()
        );

        // Then final file state
        File dbFile = fileRepository.byId(completeResp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(totalSize, dbFile.getSize());
        assertEquals(file.getName(), dbFile.getFilename());
    }

    @Test
    void should_fast_upload_when_hash_exists() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String fileHash = ctx.getFileHash();
        java.io.File originalFile = ctx.getOriginalFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), ctx.getCustomId(), rFolderName());

        File dbFile2 = fileRepository.byId(fileId);
        String fileHash2 = dbFile2.getHash();
        assertEquals(fileHash, fileHash2);

        // When
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

        // Then
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
        // Given
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

        // When & Then
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
        // Given
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

        // When & Then
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
        // Given
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

        // When & Then
        assertError(() -> FileUploadApi.initUploadRaw(manager.getJwt(), command), REQUEST_VALIDATION_FAILED);
    }

    @Test
    void should_upload_chunks_concurrently() throws IOException, ExecutionException, InterruptedException {
        // Given
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

        String uploadId = initResp.getUploadId();

        // When upload chunks concurrently
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<UploadChunkResponse>> futures = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];

            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                assertTrue(read > 0);
                byte[] chunkBytes = Arrays.copyOf(buffer, read);

                final int idx = chunkIndex;
                final byte[] chunk = chunkBytes;
                futures.add(executor.submit(() ->
                        FileUploadApi.uploadChunk(manager.getJwt(), uploadId, idx, chunk)
                ));
            }
        }

        List<UploadChunkResponse> responses = new ArrayList<>();
        for (Future<UploadChunkResponse> future : futures) {
            responses.add(future.get());
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        // Then
        assertEquals(totalChunks, responses.size());
        Set<Integer> uploadedIndices = new HashSet<>();
        for (UploadChunkResponse resp : responses) {
            uploadedIndices.add(resp.getChunkIndex());
        }
        assertEquals(totalChunks, uploadedIndices.size());

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
        assertEquals(fileHash, dbFile.getHash());
    }

    @Test
    void should_resume_upload_from_existing_chunks() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/BadApple.mp4");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        int chunkSize = fileProperties.getUpload().getChunkSize();
        long totalSize = file.length();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // Upload at least 1 chunk regardless of totalChunks
        int initialChunksToUpload = Math.min(1, totalChunks);

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

        String uploadId = initResp.getUploadId();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];

            for (int chunkIndex = 0; chunkIndex < initialChunksToUpload; chunkIndex++) {
                int read = raf.read(buffer);
                byte[] chunkBytes = Arrays.copyOf(buffer, read);
                FileUploadApi.uploadChunk(manager.getJwt(), uploadId, chunkIndex, chunkBytes);
            }
        }

        // When re-init upload (resume)
        InitUploadResponse resumeResp = FileUploadApi.initUpload(
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

        // Then resume response contains existing chunks
        assertFalse(resumeResp.isUploaded());
        assertEquals(uploadId, resumeResp.getUploadId());
        assertNotNull(resumeResp.getUploadedChunks());
        assertEquals(initialChunksToUpload, resumeResp.getUploadedChunks().size());

        for (int i = 0; i < initialChunksToUpload; i++) {
            assertTrue(resumeResp.getUploadedChunks().contains(i));
        }

        // When upload remaining chunks
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];

            for (int chunkIndex = initialChunksToUpload; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                assertTrue(read > 0);
                byte[] chunkBytes = Arrays.copyOf(buffer, read);
                FileUploadApi.uploadChunk(manager.getJwt(), uploadId, chunkIndex, chunkBytes);
            }
        }

        // When complete upload
        FileUploadResponse completeResp = FileUploadApi.completeUpload(
                manager.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(uploadId)
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(totalSize)
                        .build()
        );

        // Then
        File dbFile = fileRepository.byId(completeResp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(fileHash, dbFile.getHash());
    }

    @Test
    void should_fail_when_uploading_with_wrong_hash() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/BadApple.mp4");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        int chunkSize = fileProperties.getUpload().getChunkSize();
        long totalSize = file.length();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        String correctHash = setupApi.deleteFileWithSameHash(file);
        String wrongHash = "wronghash123456789012345678901234";

        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager.getJwt(),
                InitUploadCommand.builder()
                        .parentId(parentId)
                        .fileName(file.getName())
                        .fileHash(correctHash)
                        .totalSize(totalSize)
                        .chunkSize(chunkSize)
                        .totalChunks(totalChunks)
                        .build()
        );

        String uploadId = initResp.getUploadId();

        // Upload all chunks
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];
            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                byte[] chunkBytes = Arrays.copyOf(buffer, read);
                FileUploadApi.uploadChunk(manager.getJwt(), uploadId, chunkIndex, chunkBytes);
            }
        }

        // When & Then - pass wrong hash to trigger FILE_HASH_MISMATCH
        assertError(() -> FileUploadApi.completeUploadRaw(manager.getJwt(), CompleteUploadCommand.builder()
                .uploadId(uploadId)
                .parentId(parentId)
                .fileHash(wrongHash)
                .totalSize(totalSize)
                .build()
        ), FILE_HASH_MISMATCH);
    }

    @Test
    void should_fail_when_uploading_to_other_user_session() throws IOException {
        // Given
        LoginResponse manager1 = setupApi.registerWithLogin();
        LoginResponse manager2 = setupApi.registerWithLogin();
        String customId1 = personalCustomId(manager1.getUserId());
        String customId2 = personalCustomId(manager2.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File file = resource.getFile();
        String parentId1 = setupApi.createFolderUnderRoot(manager1.getJwt(), customId1, rFolderName());
        String parentId2 = setupApi.createFolderUnderRoot(manager2.getJwt(), customId2, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        InitUploadResponse initResp = FileUploadApi.initUpload(
                manager1.getJwt(),
                InitUploadCommand.builder()
                        .parentId(parentId1)
                        .fileName(file.getName())
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .chunkSize(fileProperties.getUpload().getChunkSize())
                        .totalChunks(1)
                        .build()
        );

        String uploadId = initResp.getUploadId();

        // When & Then - other user cannot upload chunk
        assertError(() -> {
                    try {
                        return FileUploadApi.uploadChunkRaw(manager2.getJwt(), uploadId, 0, Files.readAllBytes(file.toPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                ACCESS_DENIED);

        // When & Then - other user cannot complete upload
        assertError(() -> FileUploadApi.completeUploadRaw(manager2.getJwt(), CompleteUploadCommand.builder()
                        .uploadId(uploadId)
                        .parentId(parentId2)
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .build()),
                ACCESS_DENIED);
    }

    @Test
    void should_upload_single_chunk_file() throws IOException {
        // Given
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

        // Then init response
        assertFalse(initResp.isUploaded());
        assertNotNull(initResp.getUploadId());
        assertNotNull(initResp.getUploadedChunks());

        // When upload chunk
        FileUploadApi.uploadChunk(manager.getJwt(), initResp.getUploadId(), 0, Files.readAllBytes(file.toPath()));

        // When complete upload
        FileUploadResponse completeResp = FileUploadApi.completeUpload(
                manager.getJwt(),
                CompleteUploadCommand.builder()
                        .uploadId(initResp.getUploadId())
                        .parentId(parentId)
                        .fileHash(fileHash)
                        .totalSize(file.length())
                        .build()
        );

        // Then
        File dbFile = fileRepository.byId(completeResp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(fileHash, dbFile.getHash());
        assertEquals(file.length(), dbFile.getSize());
    }

    @Test
    void should_verify_file_content_after_upload() throws IOException, NoSuchAlgorithmException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // When
        FileUploadResponse resp = FileUploadApi.upload(manager.getJwt(), file, parentId);

        // Then download and verify content
        FileApi.DownloadedFile downloadedFile = FileApi.download(manager.getJwt(), resp.getFileId());
        byte[] downloadedBytes = downloadedFile.getContent();

        assertEquals(Files.readAllBytes(file.toPath()).length, downloadedBytes.length);

        MessageDigest md = MessageDigest.getInstance("MD5");
        String downloadedHash = bytesToHex(md.digest(downloadedBytes));
        assertEquals(fileHash, downloadedHash);
    }

    @Test
    void should_verify_large_file_content_after_chunk_upload() throws IOException, NoSuchAlgorithmException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource("testdata/BadApple.mp4");
        java.io.File file = resource.getFile();
        String parentId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        int chunkSize = fileProperties.getUpload().getChunkSize();
        long totalSize = file.length();
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

        String fileHash = setupApi.deleteFileWithSameHash(file);

        // When upload all chunks
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

        String uploadId = initResp.getUploadId();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[chunkSize];
            for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                int read = raf.read(buffer);
                byte[] chunkBytes = Arrays.copyOf(buffer, read);
                FileUploadApi.uploadChunk(manager.getJwt(), uploadId, chunkIndex, chunkBytes);
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

        // Then download and verify content
        File dbFile = fileRepository.byId(completeResp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(totalSize, dbFile.getSize());

        FileApi.DownloadedFile downloadedFile = FileApi.download(manager.getJwt(), dbFile.getId());
        byte[] downloadedBytes = downloadedFile.getContent();

        assertEquals(file.length(), downloadedBytes.length);

        MessageDigest md = MessageDigest.getInstance("MD5");
        String downloadedHash = bytesToHex(md.digest(downloadedBytes));
        assertEquals(fileHash, downloadedHash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
