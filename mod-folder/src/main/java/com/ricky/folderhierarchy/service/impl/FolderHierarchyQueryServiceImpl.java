package com.ricky.folderhierarchy.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import com.ricky.folderhierarchy.domain.dto.resp.FolderHierarchyResponse;
import com.ricky.folderhierarchy.service.FolderHierarchyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.folderhierarchy.domain.dto.resp.FolderHierarchyResponse.HierarchyFolder;

@Service
@RequiredArgsConstructor
public class FolderHierarchyQueryServiceImpl implements FolderHierarchyQueryService {

    private final RateLimiter rateLimiter;
    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;

    @Override
    public FolderHierarchyResponse fetchFolderHierarchy(UserContext userContext) {
        rateLimiter.applyFor("FolderHierarchy:FetchFolderHierarchy", 50);

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(userContext.getUid());
        List<HierarchyFolder> allFolders = folderRepository.cachedUserAllFolders(userContext.getUid()).stream()
                .map(folder -> HierarchyFolder.builder()
                        .id(folder.getId())
                        .folderName(folder.getFolderName())
                        .build())
                .collect(toImmutableList());

        return FolderHierarchyResponse.builder()
                .idTree(hierarchy.getIdTree())
                .allFolders(allFolders)
                .build();
    }
}
