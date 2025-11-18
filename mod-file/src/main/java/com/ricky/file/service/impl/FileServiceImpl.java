package com.ricky.file.service.impl;

import com.ricky.common.context.ThreadLocalContext;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.file.domain.StorageId;
import com.ricky.file.domain.dto.FileUploadCommand;
import com.ricky.file.domain.metadata.FileType;
import com.ricky.file.domain.metadata.Metadata;
import com.ricky.file.domain.metadata.extractor.MetadataExtractor;
import com.ricky.file.domain.metadata.extractor.MetadataExtractorFactory;
import com.ricky.file.infra.FileRepository;
import com.ricky.file.infra.FileStorage;
import com.ricky.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MetadataExtractorFactory metadataExtractorFactory;
    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @Override
    @Transactional
    public String upload(FileUploadCommand dto) {
        MultipartFile multipartFile = dto.getFile();
        FileType fileType = FileType.fromContentType(multipartFile.getContentType());
        StorageId storageId = StorageId.EMPTY;
        if (!fileDomainService.exists(multipartFile)) {
            storageId = fileStorage.store(multipartFile);
        }
        MetadataExtractor extractor = metadataExtractorFactory.getExtractor(fileType);
        Metadata metaData = extractor.extract(multipartFile);
        File file = File.create(
                ThreadLocalContext.getContext().getUid(),
                dto.getParentId(),
                storageId,
                multipartFile.getOriginalFilename(),
                metaData,
                dto.getPath()
        );
        fileRepository.save(file);
        log.info("File[{}] upload complete", file.getId());
        return file.getId();
    }

}
