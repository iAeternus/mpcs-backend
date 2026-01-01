package com.ricky.file.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.file.domain.File;
import com.ricky.file.domain.HashCachedStorageIds;
import com.ricky.file.domain.StorageId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.constants.ConfigConstants.FILE_CACHE;
import static com.ricky.common.constants.ConfigConstants.HASH_STORAGES_CACHE;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Repository
public class MongoCachedFileRepository extends MongoBaseRepository<File> {

    @Cacheable(value = FILE_CACHE, key = "#fileId")
    public File cachedById(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank.");
        return super.byId(fileId);
    }

    @Caching(evict = {@CacheEvict(value = FILE_CACHE, key = "#fileId")})
    public void evictFileCache(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank.");
        log.info("Evicted cache for File[{}].", fileId);
    }

    // TODO 若增加文件聚合根逻辑删除，这个cache没有过滤逻辑删除掉的聚合根
    @Cacheable(value = HASH_STORAGES_CACHE, key = "#hash")
    public HashCachedStorageIds cachedByFileHash(String hash) {
        requireNotBlank(hash, "File hash must not be blank.");

        Query query = query(where("hash").is(hash));
        query.fields().include("storageId");

        List<File> files = mongoTemplate.find(query, File.class);
        List<StorageId> storageIds = files.stream()
                .map(File::getStorageId)
                .filter(ValidationUtils::nonNull)
                .collect(toImmutableList());
        return HashCachedStorageIds.builder()
                .storageIds(storageIds)
                .build();
    }

    @Caching(evict = {@CacheEvict(value = HASH_STORAGES_CACHE, key = "#hash")})
    public void evictFileHashCache(String hash) {
        requireNotBlank(hash, "File hash must not be blank.");
        log.info("Evicted cache for hash[{}].", hash);
    }

    @Caching(evict = {
            @CacheEvict(value = FILE_CACHE, allEntries = true),
            @CacheEvict(value = HASH_STORAGES_CACHE, allEntries = true),
    })
    public void evictAll() {
    }

}
