package com.ricky.file;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.utils.ChecksumUtils;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileStatus;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
import com.ricky.folder.domain.Folder;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.ricky.common.exception.ErrorCodeEnum.REQUEST_VALIDATION_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileControllerTest extends BaseApiTest {

    @Test
    void should_upload_file() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = Folder.newFolderId();
        String path = "/test";

        // When
        FileUploadResponse resp = FileApi.upload(loginResponse.getJwt(), file, parentId, path);

        // Then
        File dbFile = fileRepository.byId(resp.getFileId());
        assertEquals(FileStatus.NORMAL, dbFile.getStatus());
        assertEquals(parentId, dbFile.getParentId());
        assertEquals(path, dbFile.getPath());

//        InputStream inputStream = Files.newInputStream(file.toPath());
//        assertEquals(fileHasherFactory.getFileHasher().hash(inputStream), dbFile.getMetadata().getHash());
//        assertEquals(ChecksumUtils.crc32(inputStream), dbFile.getMetadata().getChecksum());

        try (InputStream hashInputStream = Files.newInputStream(file.toPath())) {
            assertEquals(fileHasherFactory.getFileHasher().hash(hashInputStream), dbFile.getMetadata().getHash());
        }

        try (InputStream checksumInputStream = Files.newInputStream(file.toPath())) {
            assertEquals(ChecksumUtils.crc32(checksumInputStream), dbFile.getMetadata().getChecksum());
        }

        GridFSFile gridFSFile = gridFsFileStorage.findFile(dbFile.getStorageId());
        assertEquals(dbFile.getStorageId().getValue(), gridFSFile.getFilename());

        // Finally
        gridFsFileStorage.delete(dbFile.getStorageId());
    }

    @Test
    void should_upload_file_if_hash_already_exist() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();

        // When
        FileUploadResponse resp = FileApi.upload(loginResponse.getJwt(), file, Folder.newFolderId(), "/test"); // 先上传文件，抢占 storageId
        FileUploadResponse resp2 = FileApi.upload(loginResponse.getJwt(), file, Folder.newFolderId(), "/test");

        // Then
        File dbFile = fileRepository.byId(resp.getFileId());
        File dbFile2 = fileRepository.byId(resp2.getFileId());
        assertEquals(dbFile.getStorageId(), dbFile2.getStorageId()); // 两条记录指向同一个 storageId

        // Finally
        gridFsFileStorage.delete(dbFile.getStorageId());
        gridFsFileStorage.delete(dbFile2.getStorageId());
    }

    @Test
    void should_fail_to_upload_if_path_is_invalid() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = Folder.newFolderId();
        String path = "test"; // 非法的路径

        // When & Then
        assertError(() -> FileApi.uploadRaw(loginResponse.getJwt(), file, parentId, path), REQUEST_VALIDATION_FAILED);
    }

}