package com.ricky.folderhierarchy.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import com.ricky.folderhierarchy.domain.dto.cmd.UpdateFolderHierarchyCommand;
import com.ricky.folderhierarchy.service.FolderHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderHierarchyServiceImpl implements FolderHierarchyService {

    private final RateLimiter rateLimiter;
    private final FolderHierarchyDomainService folderHierarchyDomainService;
    private final FolderHierarchyRepository folderHierarchyRepository;

    @Override
    @Transactional
    public void updateFolderHierarchy(UpdateFolderHierarchyCommand command, UserContext userContext) {
        rateLimiter.applyFor("FolderHierarchy:UpdateFolderHierarchy", 10);

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(userContext.getUid());
        folderHierarchyDomainService.updateFolderHierarchy(hierarchy, command.getIdTree(), userContext);

        folderHierarchyRepository.save(hierarchy);
        log.info("Updated folder hierarchy for user[{}].", userContext.getUid());
    }
}
