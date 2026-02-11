package com.ricky.folder.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.folder.command.*;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderDomainService;
import com.ricky.folder.domain.FolderFactory;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folder.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.ricky.common.exception.ErrorCodeEnum.CANNOT_DELETE_ROOT_FOLDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final RateLimiter rateLimiter;
    private final FolderFactory folderFactory;
    private final FolderDomainService folderDomainService;
    private final FileDomainService fileDomainService;
    private final FolderRepository folderRepository;

    @Override
    @Transactional
    public String createFolder(CreateFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:CreateFolder", 10);

        String parentId = folderDomainService.resolveParentId(command.getCustomId(), command.getParentId());
        Folder folder = folderFactory.create(command.getCustomId(), parentId, command.getFolderName(), userContext);
        folderRepository.save(folder);

        log.info("Created folder[{}]", folder.getId());
        return folder.getId();
    }

    @Override
    @Transactional
    public void renameFolder(String folderId, RenameFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:RenameFolder", 10);

        Folder folder = folderRepository.byIdAndCheckUserShip(folderId, userContext);
        folderDomainService.renameFolder(folder, command.getNewName(), userContext);
        folderRepository.save(folder);
        log.info("Renamed folder[{}]", folderId);
    }

    @Override
    @Transactional
    public void deleteFolderForce(String folderId, DeleteFolderForceCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:DeleteFolder", 10);

        if (folderDomainService.isRoot(command.getCustomId(), folderId)) {
            throw new MyException(CANNOT_DELETE_ROOT_FOLDER, "不能删除根目录",
                    "customId", command.getCustomId(), "userId", userContext.getUid());
        }

        FolderDomainService.DeleteFolderContext ctx = folderDomainService.collectDeleteFolderContext(command.getCustomId(), folderId);

        List<Folder> folders = ctx.getFolders();
        List<File> files = ctx.getFiles();

        files.forEach(file -> file.onDelete(userContext));
        fileDomainService.deleteFilesForce(files, userContext);

        folders.forEach(folder -> folder.onDelete(userContext));
        folderRepository.delete(folders);

        log.info("Deleted folder[{}]", folderId);
    }

    @Override
    @Transactional
    public MoveFolderResponse moveFolder(MoveFolderCommand command, UserContext userContext) {
        rateLimiter.applyFor("Folder:MoveFolder", 10);

        String newParentId = folderDomainService.resolveParentId(command.getCustomId(), command.getNewParentId());
        var moveResult = folderDomainService.moveFolder(command.getCustomId(), command.getFolderId(), newParentId, userContext);
        Set<String> movedFolderIds = moveResult.getMovedFolderIds();
        Set<String> movedFileIds = moveResult.getMovedFileIds();

        log.info("Moved folder [{}] under [{}]", command.getFolderId(), command.getNewParentId());
        return MoveFolderResponse.builder()
                .movedFolderCount(movedFolderIds.size())
                .movedFileCount(movedFileIds.size())
                .build();
    }
}
