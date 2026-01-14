package com.ricky.folder.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.folder.command.*;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderDomainService;
import com.ricky.folder.domain.FolderFactory;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folder.service.FolderService;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final RateLimiter rateLimiter;
    private final FolderFactory folderFactory;
    private final FolderDomainService folderDomainService;
    private final FileDomainService fileDomainService;
    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;

    @Override
    @Transactional
    public String createFolder(CreateFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:CreateFolder", 10);

        FolderHierarchy folderHierarchy = folderHierarchyRepository.byCustomId(command.getCustomId());
        Folder folder = folderFactory.create(
                command.getFolderName(),
                userContext.getUid(),
                command.getParentId(),
                folderHierarchy,
                userContext
        );
        folderHierarchy.addFolder(folder, userContext);

        folderRepository.save(folder);
        folderHierarchyRepository.save(folderHierarchy);

        log.info("Created folder[{}]", folder.getId());
        return folder.getId();
    }

    @Override
    @Transactional
    public void renameFolder(String folderId, RenameFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:RenameFolder", 10);

        Folder folder = folderRepository.byIdAndCheckUserShip(folderId, userContext);
        folderDomainService.renameFolder(command.getCustomId(), folder, command.getNewName(), userContext);
        folderRepository.save(folder);
        log.info("Renamed folder[{}]", folderId);
    }

    @Override
    @Transactional
    public void deleteFolderForce(String folderId, DeleteFolderForceCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:DeleteFolder", 10);

        FolderHierarchy hierarchy = folderHierarchyRepository.byCustomId(command.getCustomId());
        FolderDomainService.DeleteFolderContext ctx = folderDomainService.collectDeleteFolderContext(folderId, hierarchy);

        List<Folder> folders = ctx.getFolders();
        List<File> files = ctx.getFiles();

        folders.forEach(folder -> folder.onDelete(userContext));
        folderRepository.delete(folders);

        files.forEach(file -> file.onDelete(userContext));
        fileDomainService.deleteFilesForce(files, userContext);

        hierarchy.removeFolder(folderId, userContext);
        folderHierarchyRepository.save(hierarchy);
        log.info("Deleted folder[{}]", folderId);
    }

    @Override
    @Transactional
    public MoveFolderResponse moveFolder(MoveFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:MoveFolder", 10);

        FolderDomainService.FolderFileCount count = folderDomainService.moveFolder(
                command.getCustomId(),
                command.getFolderId(),
                command.getNewParentId()
        );

        Folder folder = folderRepository.byId(command.getFolderId());
        folder.updateParentId(command.getNewParentId(), userContext);
        folderRepository.save(folder);

        log.info("Moved folder[{}]", command.getFolderId());
        return MoveFolderResponse.builder()
                .movedFolderCount(count.getFolderCount())
                .movedFileCount(count.getFileCount())
                .build();
    }
}
