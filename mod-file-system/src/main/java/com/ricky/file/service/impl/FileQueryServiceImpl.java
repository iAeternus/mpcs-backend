package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.query.FetchFilePathResponse;
import com.ricky.file.service.FileQueryService;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;

@Service
@RequiredArgsConstructor
public class FileQueryServiceImpl implements FileQueryService {

    private final RateLimiter rateLimiter;
    private final FileRepository fileRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FolderRepository folderRepository;

    @Override
    public FetchFilePathResponse fetchFilePath(String customId, String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:FetchFilePath", 50);

        File file = fileRepository.cachedById(fileId);
        FolderHierarchy hierarchy = folderHierarchyRepository.cachedByCustomId(customId);
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
