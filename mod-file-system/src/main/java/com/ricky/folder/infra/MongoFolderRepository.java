package com.ricky.folder.infra;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folder.domain.UserCachedFolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Repository
@RequiredArgsConstructor
public class MongoFolderRepository extends MongoBaseRepository<Folder> implements FolderRepository {

    private final MongoCachedFolderRepository cachedFolderRepository;

    @Override
    public List<UserCachedFolder> cachedUserAllFolders(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");
        return cachedFolderRepository.cachedUserAllFolders(userId).getFolders();
    }

    @Override
    public void save(Folder folder) {
        super.save(folder);
        cachedFolderRepository.evictUserAllFoldersCache(folder.getUserId());
        cachedFolderRepository.evictFolderCache(folder.getId());
    }

    @Override
    public Folder byIdAndCheckUserShip(String id, UserContext userContext) {
        return super.byIdAndCheckUserShip(id, userContext);
    }

    @Override
    public void delete(Folder folder) {
        super.delete(folder);
        cachedFolderRepository.evictUserAllFoldersCache(folder.getUserId());
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
                    cachedFolderRepository.evictUserAllFoldersCache(folder.getUserId());
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
    public boolean exists(String arId) {
        return super.exists(arId);
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
}
