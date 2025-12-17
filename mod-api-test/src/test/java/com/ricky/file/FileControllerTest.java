package com.ricky.file;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.common.utils.ChecksumUtils;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileStatus;
import com.ricky.file.domain.dto.FileUploadCommand;
import com.ricky.folder.domain.Folder;
import com.ricky.testsuite.BaseApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
class FileControllerTest extends BaseApiTest {

    @Autowired
    private FileApi fileApi;

    @Test
    void should_upload_file() throws IOException {
        // Given
        MultipartFile multipartFile = setUpService.loadTestFile("test_file.txt", "file");
        FileUploadCommand command = FileUploadCommand.builder()
                .file(multipartFile)
                .parentId(Folder.newFolderId())
                .path("/test")
                .build();

        // When
        String fileId = fileApi.upload(mockMvc, "", command);

        // Then
        File file = fileRepository.byId(fileId);
        assertEquals(FileStatus.NORMAL, file.getStatus());
        assertEquals(command.getParentId(), file.getParentId());
        assertEquals(command.getPath(), file.getPath());

        assertEquals(command.getFile().getSize(), file.getMetadata().getSize());
        assertEquals(command.getFile().getContentType(), file.getMetadata().getMimeType());
        assertEquals(fileHasherFactory.getFileHasher().hash(multipartFile.getInputStream()), file.getMetadata().getHash());
        assertEquals(ChecksumUtils.crc32(multipartFile.getInputStream()), file.getMetadata().getChecksum());

        GridFSFile gridFSFile = gridFsFileStorage.findFile(file.getStorageId());
        assertEquals(file.getStorageId().getValue(), gridFSFile.getFilename());

        // Finally
        tearDownService.deleteFileFromGridFs(file.getStorageId());
    }

}