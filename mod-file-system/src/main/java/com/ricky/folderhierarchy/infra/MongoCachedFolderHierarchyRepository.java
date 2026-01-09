package com.ricky.folderhierarchy.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.FOLDER_HIERARCHY_CACHE;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Repository
public class MongoCachedFolderHierarchyRepository extends MongoBaseRepository<FolderHierarchy> {

//    @Cacheable(value = FOLDER_HIERARCHY_CACHE, key = "#userId")
//    public FolderHierarchy cachedByUserId(String userId) {
//        requireNotBlank(userId, "User ID must not be blank.");
//
//        Query query = Query.query(where("userId").is(userId));
//        FolderHierarchy hierarchy = mongoTemplate.findOne(query, FolderHierarchy.class);
//
//        if (isNull(hierarchy)) {
//            throw new MyException(FOLDER_HIERARCHY_NOT_FOUND, "未找到文件夹层级。", "userId", userId);
//        }
//
//        return hierarchy;
//    }
//
//    @Caching(evict = {@CacheEvict(value = FOLDER_HIERARCHY_CACHE, key = "#userId")})
//    public void evictFolderHierarchyCache(String userId) {
//        requireNotBlank(userId, "User ID must not be blank.");
//
//        log.debug("Evicted folder hierarchy cache for user[{}].", userId);
//    }

    @Cacheable(value = FOLDER_HIERARCHY_CACHE, key = "#customId")
    public FolderHierarchy cachedByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = Query.query(where("customId").is(customId));
        FolderHierarchy hierarchy = mongoTemplate.findOne(query, FolderHierarchy.class);

        if (isNull(hierarchy)) {
            throw new MyException(FOLDER_HIERARCHY_NOT_FOUND, "未找到文件夹层级。", "customId", customId);
        }

        return hierarchy;
    }

    @Caching(evict = {@CacheEvict(value = FOLDER_HIERARCHY_CACHE, key = "#customId")})
    public void evictFolderHierarchyCache(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        log.debug("Evicted folder hierarchy cache for customId[{}].", customId);
    }

}
