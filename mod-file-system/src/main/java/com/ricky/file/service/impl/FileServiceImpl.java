package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.service.FileService;
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

    @Override
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

        log.info("Deleted File[{}] force", fileId);
    }
}
