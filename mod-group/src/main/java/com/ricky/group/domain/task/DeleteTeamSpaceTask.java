package com.ricky.group.domain.task;

import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteTeamSpaceTask {

    private final FolderHierarchyRepository folderHierarchyRepository;

    public void run(String customId) {
        FolderHierarchy hierarchy = folderHierarchyRepository.byCustomId(customId);
        folderHierarchyRepository.delete(hierarchy);
    }

}
