package com.ricky.fileextra.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.fileextra.domain.FileExtra;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.FILE_EXTRA_CACHE;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_EXTRA_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Repository
public class MongoCachedFileExtraRepository extends MongoBaseRepository<FileExtra> {

    @Cacheable(value = FILE_EXTRA_CACHE, key = "#fileId")
    public FileExtra cachedByFileId(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");

        Query query = query(where("fileId").is(fileId));
        FileExtra fileExtra = mongoTemplate.findOne(query, FileExtra.class);

        if (isNull(fileExtra)) {
            throw new MyException(FILE_EXTRA_NOT_FOUND, "文件额外信息不存在", "fileId", fileId);
        }

        return fileExtra;
    }

    @Caching(evict = {@CacheEvict(value = FILE_EXTRA_CACHE, key = "#fileId")})
    public void evictFileExtraCache(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");
        log.info("Evicted cache for FileExtra[{}].", fileId);
    }

    @Caching(evict = {@CacheEvict(value = FILE_EXTRA_CACHE, allEntries = true)})
    public void evictAll() {
        log.info("Evicted all caches for FileExtra");
    }
}
