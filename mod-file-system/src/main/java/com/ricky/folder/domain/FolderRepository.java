package com.ricky.folder.domain;

import com.ricky.common.domain.hierarchy.HierarchyRepository;
import com.ricky.common.domain.user.UserContext;
import com.ricky.file.domain.File;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FolderRepository extends HierarchyRepository<Folder> {

    void save(Folder folder);

    void save(List<Folder> folders);

    Folder byIdAndCheckUserShip(String id, UserContext userContext);

    void delete(Folder folder);

    void delete(List<Folder> folders);

    List<Folder> byIds(Set<String> folderIds);

    Folder byId(String folderId);

    Folder getRoot(String customId);

    List<Folder> getAllByCustomId(String customId);

    boolean exists(String folderId);

    boolean existsRoot(String customId);

    Optional<Folder> byIdOptional(String folderId);

    Folder cachedById(String folderId);

    boolean allExists(List<String> folderIds);

    FolderHierarchy cachedByCustomId(String customId);

    boolean existsByParentIdAndName(String customId, String newParentId, String folderName);

    Folder byFileId(String fileId);

}
