package com.ricky.upload.infra;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.properties.FileProperties;
import com.ricky.file.domain.StorageId;
import com.ricky.file.domain.StoredFile;
import com.ricky.upload.domain.FileStorage;
import com.ricky.upload.domain.UploadSession;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.nonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class GridFsFileStorage implements FileStorage {

    private final FileHasherFactory fileHasherFactory;
    private final GridFsTemplate gridFsTemplate;
    private final GridFSBucket gridFSBucket;
    private final FileProperties fileProperties;

    @Override
    public StorageId store(MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        try {
            ObjectId objectId = gridFsTemplate.store(
                    multipartFile.getInputStream(),
                    filename,
                    multipartFile.getContentType()
            );
            return new StorageId(objectId.toHexString());
        } catch (IOException e) {
            throw new MyException(STORE_FILE_FAILED, "Store file failed", "filename", filename);
        }
    }

    @Override
    public StoredFile mergeChunksAndStore(UploadSession session, Path chunkDir) {
        MessageDigest digest = fileHasherFactory.getFileHasher().newDigest();

        String filename = session.getFilename();
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(fileProperties.getUpload().getChunkSize())
                .metadata(new Document()
                        .append("ownerId", session.getOwnerId())
                        .append("filename", filename)
                        .append("totalSize", session.getTotalSize())
                );

        try (GridFSUploadStream uploadStream = gridFSBucket.openUploadStream(filename, options)) {
            long writtenBytes = 0;

            for (int i = 0; i < session.getTotalChunks(); i++) {
                Path chunk = chunkDir.resolve(String.valueOf(i));

                try (InputStream in = Files.newInputStream(chunk);
                     DigestInputStream dis = new DigestInputStream(in, digest)) {

                    long bytes = dis.transferTo(uploadStream);
                    writtenBytes += bytes;
                }

                Files.deleteIfExists(chunk);
            }

            return StoredFile.builder()
                    .storageId(new StorageId(uploadStream.getObjectId().toHexString()))
                    .hash(String.valueOf(Hex.encode(digest.digest())))
                    .size(writtenBytes)
                    .build();
        } catch (IOException e) {
            throw new MyException(MERGE_CHUNKS_FAILED, "Merge chunks failed", "uploadSession", session);
        }
    }

    @Override
    public GridFSFile findFile(StorageId storageId) {
        GridFSFile gridFSFile = findGridFSFile(storageId);
        if (isNull(gridFSFile)) {
            throw new MyException(FILE_NOT_FOUND, "File not found", "storageId", storageId);
        }
        return gridFSFile;
    }

    private GridFSFile findGridFSFile(StorageId storageId) {
        try {
            ObjectId objectId = new ObjectId(storageId.getValue());
            return gridFsTemplate.findOne(query(where("_id").is(objectId)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public InputStream getFileStream(StorageId storageId) {
        return gridFSBucket.openDownloadStream(new ObjectId(storageId.getValue()));
    }

    @Override
    public void delete(StorageId storageId) {
        ObjectId objectId = new ObjectId(storageId.getValue());
        Query query = query(where("_id").is(objectId));
        gridFsTemplate.delete(query);
    }

    @Override
    public void delete(List<StorageId> storageIds) {
        List<ObjectId> objectIds = storageIds.stream()
                .map(storageId -> new ObjectId(storageId.getValue()))
                .collect(toImmutableList());
        Query query = query(where("_id").in(objectIds));
        gridFsTemplate.delete(query);
    }

    @Override
    public boolean exists(StorageId storageId) {
        return nonNull(findGridFSFile(storageId));
    }

}
