package com.ricky.file.infra.impl;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.file.domain.StorageId;
import com.ricky.file.infra.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_NOT_FOUND;
import static com.ricky.common.exception.ErrorCodeEnum.STORE_FILE_FAILED;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class GridFsFileStorage implements FileStorage {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    @Override
    public StorageId store(MultipartFile multipartFile) {
        String filename = UuidGenerator.newShortUuid();
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

    @Override
    public GridFSFile findFile(StorageId storageId) {
        String filename = storageId.getValue();
        GridFSFile gridFSFile = gridFsTemplate.findOne(query(where("filename").is(filename)));
        if (gridFSFile == null) {
            throw new MyException(FILE_NOT_FOUND, "File not found", "filename", filename);
        }
        return gridFSFile;
    }

    @Override
    public InputStream getFileStream(StorageId storageId) {
        GridFSFile gridFSFile = findFile(storageId);
        return gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
    }

    @Override
    public void delete(StorageId storageId) {
        String filename = storageId.getValue();
        gridFsTemplate.delete(query(where("filename").is(filename)));
    }

}
