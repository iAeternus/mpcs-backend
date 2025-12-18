package com.ricky.file.service.impl;

import com.ricky.common.exception.MyException;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.file.domain.StorageId;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
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

import static com.ricky.common.exception.ErrorCodeEnum.FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK;
import static com.ricky.common.utils.ValidationUtils.isBlank;

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
    public FileUploadResponse upload(MultipartFile multipartFile, String parentId, String path) {
        if (isBlank(multipartFile.getOriginalFilename())) {
            throw new MyException(FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK,
                    "文件原始名称不能为空", "filename", multipartFile.getName());
        }

        // 提取元数据
        FileType fileType = FileType.fromContentType(multipartFile.getContentType());
        MetadataExtractor extractor = metadataExtractorFactory.getExtractor(fileType);
        Metadata metaData = extractor.extract(multipartFile);

        // 存储文件内容
        StorageId storageId = StorageId.EMPTY;
        if (!fileDomainService.exists(multipartFile)) {
            storageId = fileStorage.store(multipartFile);
        }

        // 落库
        File file = File.create(
//                ThreadLocalContext.getContext().getUid(), // TODO
                "USR789367234132222976", // TODO
                parentId,
                storageId,
                multipartFile.getOriginalFilename(),
                metaData,
                path
        );
        fileRepository.save(file);

        log.info("File[{}] upload complete", file.getId());
        return FileUploadResponse.builder()
                .fileId(file.getId())
                .build();
    }

}
