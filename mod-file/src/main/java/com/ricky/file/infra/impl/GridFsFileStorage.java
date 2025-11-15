package com.ricky.file.infra.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.ricky.common.utils.UUIDGenerator;
import com.ricky.file.domain.StorageId;
import com.ricky.file.infra.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GridFsFileStorage implements FileStorage {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    @Override
    public StorageId store(MultipartFile multipartFile) {
        try {
            String filename = UUIDGenerator.newShortUUID();
            gridFsTemplate.store(
                    multipartFile.getInputStream(),
                    filename,
                    multipartFile.getContentType()
            );
            return new StorageId(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
