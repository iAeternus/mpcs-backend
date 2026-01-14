package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.query.FilePathResponse;
import com.ricky.file.query.FileInfoResponse;
import com.ricky.file.service.FileQueryService;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;
import static com.ricky.common.utils.CommonUtils.instantToLocalDateTime;

@Service
@RequiredArgsConstructor
public class FileQueryServiceImpl implements FileQueryService {

    private final RateLimiter rateLimiter;
    private final FileRepository fileRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FolderRepository folderRepository;

    @Override
    public FilePathResponse fetchFilePath(String customId, String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:FetchFilePath", 50);

        File file = fileRepository.cachedById(fileId);
        FolderHierarchy hierarchy = folderHierarchyRepository.cachedByCustomId(customId);
        String dirPath = Arrays.stream(hierarchy.schemaOf(file.getParentId()).split(NODE_ID_SEPARATOR))
                .map(folderRepository::cachedById)
                .map(Folder::getFolderName)
                .collect(Collectors.joining(NODE_ID_SEPARATOR));

        String path = dirPath + NODE_ID_SEPARATOR + file.getFilename();
        return FilePathResponse.builder()
                .path(path)
                .build();
    }

    @Override
    public FileInfoResponse fetchFileInfo(String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:FetchFileInfo", 50);

        File file = fileRepository.cachedById(fileId);
        return FileInfoResponse.builder()
                .filename(file.getFilename())
                .size(file.getSize())
                .status(file.getStatus())
                .category(file.getCategory())
                .createTime(instantToLocalDateTime(file.getCreatedAt()))
                .updateTime(instantToLocalDateTime(file.getUpdatedAt()))
                .build();
    }
}
