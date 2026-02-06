package com.ricky.folder.infra;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoHierarchyRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderHierarchy;
import com.ricky.folder.domain.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoFolderRepository extends MongoHierarchyRepository<Folder> implements FolderRepository {

    private final MongoCachedFolderRepository cachedFolderRepository;

    @Override
    public void save(Folder folder) {
        super.save(folder);
        cachedFolderRepository.evictFolderHierarchyCache(folder.getCustomId());
        cachedFolderRepository.evictFolderCache(folder.getId());
    }

    @Override
    public Folder byIdAndCheckUserShip(String id, UserContext userContext) {
        return super.byIdAndCheckUserShip(id, userContext);
    }

    @Override
    public void delete(Folder folder) {
        super.delete(folder);
        cachedFolderRepository.evictFolderHierarchyCache(folder.getCustomId());
        cachedFolderRepository.evictFolderCache(folder.getId());
    }

    @Override
    public void delete(List<Folder> folders) {
        if (isEmpty(folders)) {
            return;
        }

        super.delete(folders);
        folders.stream()
                .findAny()
                .ifPresent(folder -> {
                    cachedFolderRepository.evictFolderHierarchyCache(folder.getCustomId());
                    cachedFolderRepository.evictFolderCache(folder.getId());
                });
    }

    @Override
    public List<Folder> byIds(Set<String> ids) {
        return super.byIds(ids);
    }

    @Override
    public Folder byId(String id) {
        return super.byId(id);
    }

    @Override
    public Folder getRoot(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(
                where("customId").is(customId)
                        .and("parentId").isNull()
                        .and("folderName").is(customId)
        );

        Folder folder = mongoTemplate.findOne(query, Folder.class);
        if (isNull(folder)) {
            throw new MyException(FOLDER_NOT_FOUND, "根目录不存在", "customId", customId);
        }

        return folder;
    }

    @Override
    public List<Folder> getAllByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("customId").is(customId));
        List<Folder> folders = mongoTemplate.find(query, Folder.class);
        if (isEmpty(folders)) {
            return Collections.emptyList();
        }

        return folders;
    }

    @Override
    public boolean exists(String folderId) {
        return super.exists(folderId);
    }

    @Override
    public boolean existsRoot(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");

        Query query = query(
                where("customId").is(customId)
                        .and("parentId").isNull()
                        .and("folderName").is(customId)
        );
        return mongoTemplate.exists(query, Folder.class);
    }

    @Override
    public Optional<Folder> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Folder cachedById(String folderId) {
        requireNotBlank(folderId, "Folder ID must not be blank.");
        return cachedFolderRepository.cachedById(folderId);
    }

    @Override
    public boolean allExists(List<String> folderIds) {
        if (isEmpty(folderIds)) {
            return true;
        }
        Query query = query(where("_id").in(folderIds));
        return mongoTemplate.count(query, Folder.class) == folderIds.size();
    }

    @Override
    public FolderHierarchy cachedByCustomId(String customId) {
        requireNotBlank(customId, "Custom ID must not be blank.");
        return cachedFolderRepository.cachedByCustomId(customId);
    }

    @Override
    public boolean existsByParentIdAndName(String newParentId, String folderName) {
        requireNotBlank(newParentId, "New Parent ID must not be blank.");
        requireNotBlank(folderName, "Folder Name must not be blank.");

        Query query = query(where("parentId").is(newParentId).and("folderName").is(folderName));
        return mongoTemplate.exists(query, Folder.class);
    }
}
