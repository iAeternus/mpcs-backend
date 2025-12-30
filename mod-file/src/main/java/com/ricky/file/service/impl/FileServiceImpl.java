package com.ricky.file.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileDomainService;
import com.ricky.file.domain.StorageId;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
import com.ricky.file.domain.metadata.FileType;
import com.ricky.file.domain.metadata.Metadata;
import com.ricky.file.domain.metadata.extractor.MetadataExtractor;
import com.ricky.file.domain.metadata.extractor.MetadataExtractorFactory;
import com.ricky.file.domain.FileRepository;
import com.ricky.file.domain.FileStorage;
import com.ricky.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MetadataExtractorFactory metadataExtractorFactory;
    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;
    private final FileHasherFactory fileHasherFactory;

    @Override
    @Transactional
    public FileUploadResponse upload(MultipartFile multipartFile, String parentId, String path, UserContext userContext) {
        if (isBlank(multipartFile.getOriginalFilename())) {
            throw new MyException(FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK,
                    "文件原始名称不能为空", "filename", multipartFile.getName());
        }

        // 提取元数据
        FileType fileType = FileType.fromContentType(multipartFile.getContentType());
        MetadataExtractor extractor = metadataExtractorFactory.getExtractor(fileType);
        Metadata metaData = extractor.extract(multipartFile);

        // 存储文件内容
        List<File> files = fileDomainService.listByFileHash(multipartFile);
        StorageId storageId = isEmpty(files) ? fileStorage.store(multipartFile) : files.get(0).getStorageId();

        // 落库
        File file = File.create(
                userContext.getUid(),
                parentId,
                storageId,
                multipartFile.getOriginalFilename(),
                metaData,
                path,
                userContext
        );
        fileRepository.save(file);

        log.info("File[{}] upload complete", file.getId());
        return FileUploadResponse.builder()
                .fileId(file.getId())
                .build();
    }

}
