package com.ricky.folder.domain;

import com.ricky.common.domain.user.UserContext;

import java.util.List;
import java.util.Set;

public interface FolderRepository {
    List<UserCachedFolder> cachedUserAllFolders(String userId);

    void save(Folder folder);

    Folder byIdAndCheckUserShip(String id, UserContext userContext);

    void delete(Folder folder);

    void delete(List<Folder> folders);

    List<Folder> byIds(Set<String> folderIds);

    Folder byId(String folderId);

    boolean exists(String folderId);
}
