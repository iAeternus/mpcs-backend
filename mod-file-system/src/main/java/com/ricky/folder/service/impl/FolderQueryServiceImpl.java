package com.ricky.folder.service.impl;

import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.folder.domain.*;
import com.ricky.folder.query.FolderContentResponse;
import com.ricky.folder.query.FolderHierarchyResponse;
import com.ricky.folder.service.FolderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.ricky.common.utils.CommonUtils.instantToLocalDateTime;

@Service
@RequiredArgsConstructor
public class FolderQueryServiceImpl implements FolderQueryService {

    private final RateLimiter rateLimiter;
    private final FolderDomainService folderDomainService;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    @Override
    public FolderContentResponse fetchFolderContent(String customId, String folderId, UserContext userContext) {
        rateLimiter.applyFor("Folder:FetchFolderContent", 50);

        Set<String> directChildFolderIds = folderDomainService.directChildIdsUnder(customId, folderId);
        List<Folder> directChildFolders = folderRepository.byIds(directChildFolderIds);

        List<FolderContentResponse.Folder> folders = directChildFolders.stream()
                .map(folder -> FolderContentResponse.Folder.builder()
                        .id(folder.getId())
                        .folderName(folder.getFolderName())
                        .updatedTime(instantToLocalDateTime(folder.getUpdatedAt()))
                        .build())
                .collect(toImmutableList());

        Folder folder = folderRepository.byId(folderId);
        List<File> directFiles = fileRepository.byIds(folder.getFileIds());

        List<FolderContentResponse.File> files = directFiles.stream()
                .map(file -> FolderContentResponse.File.builder()
                        .id(file.getId())
                        .filename(file.getFilename())
                        .updateTime(instantToLocalDateTime(file.getUpdatedAt()))
                        .size(file.getSize())
                        .build())
                .collect(toImmutableList());

        return FolderContentResponse.builder()
                .folders(folders)
                .files(files)
                .build();
    }

    @Override
    public FolderHierarchyResponse fetchFolderHierarchy(String customId, UserContext userContext) {
        rateLimiter.applyFor("Folder:FetchFolderHierarchy", 50);

        FolderHierarchy hierarchy = folderRepository.cachedByCustomId(customId);

        Map<String, List<FolderHierarchyResponse.HierarchyFile>> folderFilesMap = hierarchy.getFolders().stream()
                .collect(toImmutableMap(FolderMeta::getId,
                        folder -> fileRepository.byIds(folder.getFileIds()).stream()
                                .map(file -> FolderHierarchyResponse.HierarchyFile.builder()
                                        .id(file.getId())
                                        .filename(file.getFilename())
                                        .size(file.getSize())
                                        .status(file.getStatus())
                                        .build())
                                .collect(toImmutableList())
                ));

        IdTree idTree = hierarchy.buildIdTree();
        var allFolders = hierarchy.getFolders().stream()
                .map(cachedFolder -> FolderHierarchyResponse.HierarchyFolder.builder()
                        .id(cachedFolder.getId())
                        .folderName(cachedFolder.getFolderName())
                        .parentId(cachedFolder.getParentId())
                        .path(cachedFolder.getPath())
                        .files(folderFilesMap.get(cachedFolder.getId()))
                        .build())
                .collect(toImmutableList());

        return FolderHierarchyResponse.builder()
                .idTree(idTree)
                .allFolders(allFolders)
                .build();
    }
}
