package com.ricky.file.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.file.domain.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.FILE_CACHE;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

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

    @Caching(evict = {
            @CacheEvict(value = FILE_CACHE, allEntries = true),
    })
    public void evictAll() {
    }

}
