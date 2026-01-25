package com.ricky.publicfile.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.publicfile.domain.PublicFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.PUBLIC_FILE_CACHE;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedPublicFileRepository extends MongoBaseRepository<PublicFile> {

    @Cacheable(value = PUBLIC_FILE_CACHE, key = "#postId")
    public PublicFile cachedById(String postId) {
        requireNotBlank(postId, "PublicFile ID must not be blank.");
        return super.byId(postId);
    }

    @Caching(evict = {@CacheEvict(value = PUBLIC_FILE_CACHE, key = "#postId")})
    public void evictPublicFileCache(String postId) {
        requireNotBlank(postId, "PublicFile ID must not be blank.");
        log.debug("Evicted public file cache for PublicFile[{}].", postId);
    }

    @Caching(evict = {@CacheEvict(value = PUBLIC_FILE_CACHE, allEntries = true)})
    public void evictAll() {
        log.info("Evicted all caches for " + PUBLIC_FILE_CACHE);
    }

}
