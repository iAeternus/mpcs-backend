package com.ricky.upload.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.upload.domain.UploadSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.UPLOAD_SESSION_CACHE;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Slf4j
@Repository
public class MongoCachedUploadSessionRepository extends MongoBaseRepository<UploadSession> {

    @Cacheable(value = UPLOAD_SESSION_CACHE, key = "#uploadId")
    public UploadSession cachedById(String uploadId) {
        requireNotBlank(uploadId, "UploadSession ID must not be blank.");
        return super.byId(uploadId);
    }

    @Caching(evict = {@CacheEvict(value = UPLOAD_SESSION_CACHE, key = "#uploadId")})
    public void evictUploadSessionCache(String uploadId) {
        requireNotBlank(uploadId, "UploadSession ID must not be blank.");
        log.debug("Evicted upload_session cache for UploadSession[{}].", uploadId);
    }

}
