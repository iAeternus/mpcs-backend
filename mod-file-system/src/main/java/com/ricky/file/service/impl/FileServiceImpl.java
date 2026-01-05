package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;
import com.ricky.file.service.FileService;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final RateLimiter rateLimiter;
    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FolderRepository folderRepository;

    @Override
    @Transactional
    public void deleteFileForce(String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:DeleteFile", 10);

        File file = fileRepository.byId(fileId);
        file.onDelete(userContext);
        fileRepository.delete(file);
        fileDomainService.deleteFileForce(file, userContext);
    }

    @Override
    public FetchFilePathResponse fetchFilePath(String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:FetchFilePath", 50);

        File file = fileRepository.cachedById(fileId);
        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(userContext.getUid());
        String dirPath = Arrays.stream(hierarchy.schemaOf(file.getParentId()).split(NODE_ID_SEPARATOR))
                .map(folderRepository::cachedById)
                .map(Folder::getFolderName)
                .collect(Collectors.joining(NODE_ID_SEPARATOR));

        String path = dirPath + NODE_ID_SEPARATOR + file.getFilename();
        return FetchFilePathResponse.builder()
                .path(path)
                .build();
    }
}
