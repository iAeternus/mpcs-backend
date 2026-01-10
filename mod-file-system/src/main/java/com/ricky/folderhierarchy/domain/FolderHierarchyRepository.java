package com.ricky.folderhierarchy.domain;

import com.ricky.common.domain.SpaceType;

import java.util.List;

public interface FolderHierarchyRepository {

    FolderHierarchy byCustomId(String customId);

    List<FolderHierarchy> byUserIdAndSpaceType(String userId, SpaceType spaceType);

    void save(FolderHierarchy folderHierarchy);

    FolderHierarchy cachedByCustomId(String customId);

    boolean existsByCustomId(String customId);

    boolean cachedExistsByCustomId(String customId, String userId);
}
