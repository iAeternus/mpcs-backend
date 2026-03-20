package com.ricky.upload.infra;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.common.exception.MyException;
import com.ricky.file.domain.storage.GridFsStorageId;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.upload.domain.StorageService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.utils.ValidationUtils.nonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mpcs.config", name = "storage", havingValue = "grid_fs")
public class GridFsStorageService implements StorageService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;

    @Override
    public StorageId store(MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        try {
            ObjectId objectId = gridFsTemplate.store(
                    multipartFile.getInputStream(),
                    filename,
                    multipartFile.getContentType()
            );
            return GridFsStorageId.builder()
                    .value(objectId.toHexString())
                    .build();
        } catch (IOException e) {
            throw new MyException(STORE_FILE_FAILED, "Store file failed", "filename", filename);
        }
    }

    @Override
    public String initMultipartUpload(String filename) {
        throw new UnsupportedOperationException("GridFS does not support multipart upload");
    }

    @Override
    public String uploadPart(String uploadId, int partNumber, MultipartFile chunk) {
        throw new UnsupportedOperationException("GridFS does not support multipart upload");
    }

    @Override
    public StoredFile completeMultipartUpload(String uploadId, String filename, long totalSize, String expectedHash) {
        throw new UnsupportedOperationException("GridFS does not support multipart upload");
    }

    @Override
    public void abortMultipartUpload(String uploadId) {
        throw new UnsupportedOperationException("GridFS does not support multipart upload");
    }

    private GridFSFile findGridFSFile(GridFsStorageId storageId) {
        try {
            ObjectId objectId = storageId.toObjectId();
            return gridFsTemplate.findOne(query(where("_id").is(objectId)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public InputStream getFileStream(StorageId storageId) {
        GridFsStorageId gridFSStorageId = (GridFsStorageId) storageId;
        return gridFSBucket.openDownloadStream(gridFSStorageId.toObjectId());
    }

    @Override
    public void delete(StorageId storageId) {
        GridFsStorageId gridFSStorageId = (GridFsStorageId) storageId;
        Query query = query(where("_id").is(gridFSStorageId.toObjectId()));
        gridFsTemplate.delete(query);
    }

    @Override
    public void delete(List<StorageId> storageIds) {
        List<ObjectId> objectIds = storageIds.stream()
                .map(storageId -> ((GridFsStorageId) storageId).toObjectId())
                .collect(toImmutableList());
        Query query = query(where("_id").in(objectIds));
        gridFsTemplate.delete(query);
    }

    @Override
    public boolean exists(StorageId storageId) {
        GridFsStorageId gridFsStorageId = (GridFsStorageId) storageId;
        return nonNull(findGridFSFile(gridFsStorageId));
    }

}
