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
import static com.ricky.common.exception.ErrorCodeEnum.STORE_FILE_FAILED;
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
    public InputStream getFileStream(StorageId storageId, long offset, long length) {
        GridFsStorageId gridFSStorageId = (GridFsStorageId) storageId;
        InputStream is = gridFSBucket.openDownloadStream(gridFSStorageId.toObjectId());
        try {
            if (offset > 0) {
                long skipped = is.skip(offset);
                if (skipped != offset) {
                    throw new IOException("Failed to skip " + offset + " bytes");
                }
            }
        } catch (IOException e) {
            try {
                is.close();
            } catch (IOException ignored) {
            }
            throw new RuntimeException("Failed to skip bytes in GridFS stream", e);
        }
        return new BoundedInputStream(is, length);
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

    @Override
    public long getObjectSize(StorageId storageId) {
        GridFsStorageId gridFsStorageId = (GridFsStorageId) storageId;
        GridFSFile file = findGridFSFile(gridFsStorageId);
        if (file == null) {
            throw new IllegalStateException("GridFS file not found: " + storageId);
        }
        return file.getLength();
    }

    private static class BoundedInputStream extends InputStream {
        private final InputStream in;
        private long remaining;

        public BoundedInputStream(InputStream in, long length) {
            this.in = in;
            this.remaining = length;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) return -1;
            int b = in.read();
            if (b != -1) remaining--;
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) return -1;
            int toRead = (int) Math.min(len, remaining);
            int n = in.read(b, off, toRead);
            if (n != -1) remaining -= n;
            return n;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = in.skip(Math.min(n, remaining));
            if (skipped > 0) remaining -= skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(in.available(), remaining);
        }
    }
}
