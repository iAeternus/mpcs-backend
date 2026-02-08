package com.ricky.file.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.storage.StorageId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.ricky.common.utils.ValidationUtils.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoFileRepository extends MongoBaseRepository<File> implements FileRepository {

    private final MongoCachedFileRepository cachedFileRepository;

    @Override
    public boolean existsByHash(String hash) {
        requireNotBlank(hash, "File hash must not be blank");
        Query query = query(where("hash").is(hash));
        return mongoTemplate.exists(query, File.class);
    }

    @Override
    public File cachedById(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");
        return cachedFileRepository.cachedById(fileId);
    }

    @Override
    public File byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<StorageId> byFileHashOptional(String hash) {
        requireNotBlank(hash, "File hash must not be blank");

        Query query = query(where("hash").is(hash));
        query.fields().include("storageId");

        File file = mongoTemplate.findOne(query, File.class);
        if (isNull(file)) {
            return Optional.empty();
        }
        return Optional.of(file.getStorageId());
    }

    @Override
    public List<File> listByFileHash(String hash) {
        requireNotBlank(hash, "File hash must not be blank");
        Query query = query(where("hash").is(hash));
        return mongoTemplate.find(query, File.class);
    }

    @Override
    public void delete(File file) {
        super.delete(file);
        cachedFileRepository.evictFileCache(file.getId());
    }

    @Override
    public void delete(List<File> files) {
        super.delete(files);
        cachedFileRepository.evictAll();
    }

    @Override
    public List<File> byIds(Set<String> ids) {
        return super.byIds(ids);
    }

    @Override
    public List<File> listByStorageId(StorageId storageId) {
        requireNonNull(storageId, "Storage ID must not be null");

        Query query = query(where("storageId").is(storageId.getValue()));
        return mongoTemplate.find(query, File.class);
    }

    @Override
    public Map<StorageId, List<File>> listByStorageIds(List<StorageId> storageIds) {
        if (isEmpty(storageIds)) return Collections.emptyMap();

        Query query = query(where("storageId").in(storageIds));
        return mongoTemplate.find(query, File.class)
                .stream()
                .collect(Collectors.groupingBy(File::getStorageId));
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }
}
