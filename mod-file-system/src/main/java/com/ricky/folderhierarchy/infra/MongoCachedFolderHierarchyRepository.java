package com.ricky.folderhierarchy.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.UserCachedFolderHierarchies;
import com.ricky.folderhierarchy.domain.UserCachedFolderHierarchy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Repository
public class MongoCachedFolderHierarchyRepository extends MongoBaseRepository<FolderHierarchy> {

    @Cacheable(value = USER_FOLDER_HIERARCHIES_CACHE, key = "#userId")
    public UserCachedFolderHierarchies cachedUserAllFolderHierarchies(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        Query query = query(where("userId").is(userId));
        query.fields().include("customId", "idTree", "hierarchy");
        var hierarchies = mongoTemplate.find(query, UserCachedFolderHierarchy.class, FOLDER_HIERARCHY_COLLECTION);
        return UserCachedFolderHierarchies.builder()
                .hierarchies(hierarchies)
                .build();
    }

    @Caching(evict = {@CacheEvict(value = USER_FOLDER_HIERARCHIES_CACHE, key = "#userId")})
    public void evictUserFolderHierarchiesCache(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        log.debug("Evicted user folder hierarchy cache for userId[{}].", userId);
    }

    @Cacheable(value = FOLDER_HIERARCHY_CACHE, key = "#customId")
    public FolderHierarchy cachedByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("customId").is(customId));
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
