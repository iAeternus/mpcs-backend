package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.service.FileService;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final RateLimiter rateLimiter;
    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;

    @Override
    @Transactional
    public void renameFile(String fileId, RenameFileCommand command, UserContext userContext) {
        rateLimiter.applyFor("File:RenameFile", 10);

        File file = fileRepository.byId(fileId);
        file.rename(command.getNewName(), userContext);
        fileRepository.save(file);

        log.info("Renamed file[{}]", fileId);
    }

    @Override
    @Transactional
    public void deleteFileForce(String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:DeleteFile", 10);

        File file = fileRepository.byId(fileId);
        file.onDelete(userContext);
        fileRepository.delete(file);
        fileDomainService.deleteFileForce(file, userContext);

        Folder folder = folderRepository.byId(file.getParentId());
        folder.removeFile(file.getId(), userContext);
        folderRepository.save(folder);

        log.info("Deleted File[{}] force", fileId);
    }

    @Override
    @Transactional
    public void moveFile(MoveFileCommand command, UserContext userContext) {
        rateLimiter.applyFor("File:MoveFile", 10);

        File file = fileRepository.byId(command.getFileId());
        fileDomainService.checkFileNameDuplicates(file, command.getNewParentId());

        Folder parentFolder = folderRepository.byId(file.getParentId());
        parentFolder.removeFile(file.getId(), userContext);
        folderRepository.save(parentFolder);

        file.updateParentId(command.getNewParentId(), userContext);
        fileRepository.save(file);

        Folder newParentFolder = folderRepository.byId(command.getNewParentId());
        newParentFolder.addFile(file.getId(), userContext);
        folderRepository.save(newParentFolder);
    }
}
