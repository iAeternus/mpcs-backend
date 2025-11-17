package com.ricky.file.infra.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.file.domain.StorageId;
import com.ricky.file.infra.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.ricky.common.exception.ErrorCodeEnum.STORE_FILE_FAILED;

@Component
@RequiredArgsConstructor
public class GridFsFileStorage implements FileStorage {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    @Override
    public StorageId store(MultipartFile multipartFile) {
        String filename = UuidGenerator.newShortUUID();
        try {
            gridFsTemplate.store(
                    multipartFile.getInputStream(),
                    filename,
                    multipartFile.getContentType()
            );
            return new StorageId(filename);
        } catch (IOException e) {
            throw new MyException(STORE_FILE_FAILED, "Store file failed", "filename", filename);
        }
    }
}
