package com.ricky.folder.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderHierarchy;
import com.ricky.folder.domain.FolderMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.constants.ConfigConstants.FOLDER_CACHE;
import static com.ricky.common.constants.ConfigConstants.FOLDER_HIERARCHY_CACHE;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Repository
public class MongoCachedFolderRepository extends MongoBaseRepository<Folder> {

    @Cacheable(value = FOLDER_HIERARCHY_CACHE, key = "#customId")
    public FolderHierarchy cachedByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("customId").is(customId));
        query.fields().include("_id", "folderName", "parentId", "path");
        List<Folder> folders = mongoTemplate.find(query, Folder.class);

        List<FolderMeta> folderMetas = folders.stream()
                .map(folder -> FolderMeta.builder()
                        .id(folder.getId())
                        .folderName(folder.getFolderName())
                        .parentId(folder.getParentId())
                        .path(folder.getPath())
                        .build())
                .collect(toImmutableList());

        return FolderHierarchy.builder()
                .customId(customId)
                .folders(folderMetas)
                .build();
    }

    @Caching(evict = {@CacheEvict(value = FOLDER_HIERARCHY_CACHE, key = "#customId")})
    public void evictFolderHierarchyCache(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        log.debug("Evicted folder hierarchy cache for customId[{}].", customId);
    }

//    @Cacheable(value = USER_FOLDERS_CACHE, key = "#userId")
//    public UserCachedFolders cachedUserAllFolders(String userId) {
//        requireNotBlank(userId, "User ID must not be blank.");
//
//        Query query = query(where("userId").is(userId));
//        query.fields().include("_id", "folderName");
//
//        List<UserCachedFolder> userCachedFolders = mongoTemplate.find(query, UserCachedFolder.class, FOLDER_COLLECTION);
//        return UserCachedFolders.builder()
//                .folders(emptyIfNull(userCachedFolders))
//                .build();
//    }
//
//    @Caching(evict = {@CacheEvict(value = USER_FOLDERS_CACHE, key = "#userId")})
//    public void evictUserAllFoldersCache(String userId) {
//        requireNotBlank(userId, "User ID must not be blank.");
//
//        log.debug("Evicted all folders cache for user[{}].", userId);
//    }

    @Cacheable(value = FOLDER_CACHE, key = "#folderId")
    public Folder cachedById(String folderId) {
        requireNotBlank(folderId, "Folder ID must not be blank.");

        Query query = query(where("_id").is(folderId));
        Folder folder = mongoTemplate.findOne(query, Folder.class);
        if (isNull(folder)) {
            throw new MyException(FOLDER_NOT_FOUND, "文件夹不存在", "folderId", folderId);
        }

        return folder;
    }

    @Caching(evict = {@CacheEvict(value = FOLDER_CACHE, key = "#folderId")})
    public void evictFolderCache(String folderId) {
        requireNotBlank(folderId, "Folder ID must not be blank.");

        log.debug("Evicted folder cache for Folder[{}].", folderId);
    }
}
