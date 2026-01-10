package com.ricky.folderhierarchy.infra;

import com.ricky.common.domain.SpaceType;
import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoFolderHierarchyRepository extends MongoBaseRepository<FolderHierarchy> implements FolderHierarchyRepository {

    private final MongoCachedFolderHierarchyRepository cachedFolderHierarchyRepository;

//    @Override
//    public FolderHierarchy byUserId(String userId) {
//        requireNotBlank(userId, "User ID must not be blank.");
//
//        Query query = query(where("userId").is(userId));
//        FolderHierarchy hierarchy = mongoTemplate.findOne(query, FolderHierarchy.class);
//
//        if (isNull(hierarchy)) {
//            throw new MyException(FOLDER_HIERARCHY_NOT_FOUND, "未找到文件夹。", "userId", userId);
//        }
//
//        return hierarchy;
//    }

    @Override
    public FolderHierarchy byCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("customId").is(customId));
        FolderHierarchy hierarchy = mongoTemplate.findOne(query, FolderHierarchy.class);

        if (isNull(hierarchy)) {
            throw new MyException(FOLDER_HIERARCHY_NOT_FOUND, "未找到文件夹层次结构。", "customId", customId);
        }

        return hierarchy;
    }

    @Override
    public List<FolderHierarchy> byUserIdAndSpaceType(String userId, SpaceType spaceType) {
        requireNotBlank(userId, "User ID must not be blank.");
        requireNonNull(spaceType, "Space Type must not be null.");

        Query query = query(where("userId").is(userId)
                .and("customId").regex("^" + spaceType.getPrefix()));

        return mongoTemplate.find(query, FolderHierarchy.class);
    }


    @Override
    public void save(FolderHierarchy folderHierarchy) {
        super.save(folderHierarchy);
        cachedFolderHierarchyRepository.evictFolderHierarchyCache(folderHierarchy.getCustomId());
        cachedFolderHierarchyRepository.evictUserFolderHierarchiesCache(folderHierarchy.getUserId());
    }

    @Override
    public FolderHierarchy cachedByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");
        return cachedFolderHierarchyRepository.cachedByCustomId(customId);
    }

    @Override
    public boolean existsByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("customId").is(customId)); // 需要索引
        return mongoTemplate.exists(query, FolderHierarchy.class);
    }

    @Override
    public boolean cachedExistsByCustomId(String customId, String userId) {
        requireNotBlank(customId, "Custom ID must not be blank.");
        requireNotBlank(userId, "User ID must not be blank.");

        return cachedFolderHierarchyRepository.cachedUserAllFolderHierarchies(userId).stream()
                .anyMatch(fh -> ValidationUtils.equals(fh.getCustomId(), customId));
    }
}
