package com.ricky.upload.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.oss.OssService;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.file.domain.storage.OssStorageId;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.upload.domain.StorageService;
import com.ricky.upload.domain.UploadSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static com.ricky.common.constants.ConfigConstants.FILE_BUCKET;
import static com.ricky.common.exception.ErrorCodeEnum.OSS_ERROR;

@Component
@RequiredArgsConstructor
public class OssStorageService implements StorageService {

    private final OssService ossService;

    @Override
    public StorageId store(MultipartFile multipartFile) {
        try {
            String objectKey = generateObjectKey(multipartFile.getOriginalFilename());

            ossService.putObject(
                    FILE_BUCKET,
                    objectKey,
                    multipartFile.getInputStream(),
                    multipartFile.getSize(),
                    multipartFile.getContentType()
            );

            return OssStorageId.withFileBucket(objectKey);
        } catch (IOException ex) {
            throw new MyException(OSS_ERROR, "OSS上传失败", "exception", ex);
        }
    }

    @Override
    public StoredFile mergeChunksAndStore(UploadSession session, Path chunkDir) {
        return null; // TODO
    }

    @Override
    public InputStream getFileStream(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        return ossService.getObject(ossStorageId.getBucket(), ossStorageId.getObjectKey());
    }

    @Override
    public void delete(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        ossService.deleteObject(ossStorageId.getBucket(), ossStorageId.getObjectKey());
    }

    @Override
    public void delete(List<StorageId> storageIds) {
        storageIds.forEach(this::delete);
    }

    @Override
    public boolean exists(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        return ossService.exists(ossStorageId.getBucket(), ossStorageId.getObjectKey());
    }

    private String generateObjectKey(String filename) {
        return UuidGenerator.newShortUuid() + "/" + filename;
    }
}
