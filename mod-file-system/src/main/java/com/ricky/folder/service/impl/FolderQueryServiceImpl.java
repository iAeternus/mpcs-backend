package com.ricky.folder.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folder.query.FolderContentResponse;
import com.ricky.folder.service.FolderQueryService;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.utils.CommonUtils.instantToLocalDateTime;

@Service
@RequiredArgsConstructor
public class FolderQueryServiceImpl implements FolderQueryService {

    private final RateLimiter rateLimiter;
    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FileRepository fileRepository;

    @Override
    public FolderContentResponse fetchFolderContent(String customId, String folderId, UserContext userContext) {
        rateLimiter.applyFor("Folder:FetchFolderContent", 50);

        FolderHierarchy hierarchy = folderHierarchyRepository.byCustomId(customId);
        Set<String> directChildFolderIds = hierarchy.directChildFolderIdsUnder(folderId);
        List<Folder> directChildFolders = folderRepository.byIds(directChildFolderIds);

        List<FolderContentResponse.Folder> folders = directChildFolders.stream()
                .map(folder -> FolderContentResponse.Folder.builder()
                        .id(folder.getId())
                        .folderName(folder.getFolderName())
                        .lastModifiedAt(instantToLocalDateTime(folder.getUpdatedAt()))
                        .build())
                .collect(toImmutableList());

        Folder folder = folderRepository.byId(folderId);
        List<File> directFiles = fileRepository.byIds(folder.getFileIds());

        List<FolderContentResponse.File> files = directFiles.stream()
                .map(file -> FolderContentResponse.File.builder()
                        .id(file.getId())
                        .filename(file.getFilename())
                        .lastModifiedAt(instantToLocalDateTime(file.getUpdatedAt()))
                        .size(file.getSize())
                        .build())
                .collect(toImmutableList());

        return FolderContentResponse.builder()
                .folders(folders)
                .files(files)
                .build();
    }
}
