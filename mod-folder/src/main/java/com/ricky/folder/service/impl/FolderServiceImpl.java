package com.ricky.folder.service.impl;

import com.google.common.collect.ImmutableSet;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderDomainService;
import com.ricky.folder.domain.FolderFactory;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folder.domain.dto.cmd.CreateFolderCommand;
import com.ricky.folder.domain.dto.cmd.RenameFolderCommand;
import com.ricky.folder.service.FolderService;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.utils.ValidationUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final RateLimiter rateLimiter;
    private final FolderFactory folderFactory;
    private final FolderDomainService folderDomainService;
    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FileRepository fileRepository;

    @Override
    @Transactional
    public String createFolder(CreateFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:CreateFolder", 10);

        FolderHierarchy folderHierarchy = folderHierarchyRepository.byUserId(userContext.getUid());
        Folder folder = folderFactory.create(
                command.getFolderName(),
                userContext.getUid(),
                command.getParentId(),
                folderHierarchy,
                userContext
        );
        folderHierarchy.addFolder(command.getParentId(), folder.getId(), userContext);

        folderRepository.save(folder);
        folderHierarchyRepository.save(folderHierarchy);

        log.info("Created folder[{}].", folder.getId());
        return folder.getId();
    }

    @Override
    @Transactional
    public void renameFolder(String folderId, RenameFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:RenameFolder", 10);

        Folder folder = folderRepository.byIdAndCheckUserShip(folderId, userContext);
        folderDomainService.renameFolder(folder, command.getNewName(), userContext);
        folderRepository.save(folder);
        log.info("Renamed folder[{}].", folderId);
    }

    @Override
    @Transactional
    public void deleteFolder(String folderId, UserContext userContext) {
        rateLimiter.applyFor("Folder:DeleteFolder", 10);

        // 删除文件夹
        Folder folder = folderRepository.byIdAndCheckUserShip(folderId, userContext);
        folder.onDelete(userContext);
        folderRepository.delete(folder);

        // 删除文件夹下的文件
        List<File> files = fileRepository.byIds(folder.getFileIds());
        files.forEach(f -> f.onDelete(userContext));
        fileRepository.delete(files);

        // 级联删除所有子文件夹和其中的文件
        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(folder.getUserId());
        Set<String> subFolderIds = hierarchy.allSubFolderIdsOf(folderId);
        if (isNotEmpty(subFolderIds)) {
            List<Folder> folders = folderRepository.byIds(subFolderIds);
            folders.forEach(f -> f.onDelete(userContext));
            folderRepository.delete(folders);

            Set<String> subFileIds = folders.stream()
                    .flatMap(f -> f.getFileIds().stream())
                    .collect(toImmutableSet());
            List<File> subFiles = fileRepository.byIds(subFileIds);
            subFiles.forEach(f -> f.onDelete(userContext));
            fileRepository.delete(subFiles);
        }

        hierarchy.removeFolder(folderId, userContext);
        folderHierarchyRepository.save(hierarchy);
        log.info("Deleted folder[{}].", folderId);
    }
}
