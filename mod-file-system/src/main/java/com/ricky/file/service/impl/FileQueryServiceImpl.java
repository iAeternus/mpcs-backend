package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.es.ElasticSearchService;
import com.ricky.common.es.FileElasticSearchService;
import com.ricky.common.es.SearchResult;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.es.EsFile;
import com.ricky.file.query.FileInfoResponse;
import com.ricky.file.query.FilePathResponse;
import com.ricky.file.query.SearchPageQuery;
import com.ricky.file.query.SearchResponse;
import com.ricky.file.service.FileQueryService;
import com.ricky.folder.domain.FolderDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.utils.CommonUtils.instantToLocalDateTime;

@Service
@RequiredArgsConstructor
public class FileQueryServiceImpl implements FileQueryService {

    private final RateLimiter rateLimiter;
    private final FileElasticSearchService esService;
    private final FolderDomainService folderDomainService;
    private final FileRepository fileRepository;

    @Override
    public FilePathResponse fetchFilePath(String customId, String fileId, UserContext userContext) {
        rateLimiter.applyFor("File:FetchFilePath", 50);

        File file = fileRepository.cachedById(fileId);
        String folderPath = folderDomainService.folderPath(customId, file.getParentId());
        String path = folderPath + NODE_ID_SEPARATOR + file.getFilename();

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

    @Override
    public SearchResponse search(SearchPageQuery query, UserContext userContext) {
        rateLimiter.applyFor("File:Search", 10);

        SearchResult<EsFile> result = esService.search(query.getKeyword(), query.getPageIndex() - 1, query.getPageSize());
        return SearchResponse.from(result);
    }
}
