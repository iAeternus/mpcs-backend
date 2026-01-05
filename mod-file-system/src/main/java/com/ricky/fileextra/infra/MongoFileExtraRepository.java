package com.ricky.fileextra.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.fileextra.domain.FileExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_EXTRA_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoFileExtraRepository extends MongoBaseRepository<FileExtra> implements FileExtraRepository {

    private final MongoCachedFileExtraRepository cachedFileExtraRepository;

    @Override
    public void save(FileExtra fileExtra) {
        super.save(fileExtra);
        cachedFileExtraRepository.evictFileExtraCache(fileExtra.getFileId());
    }

    @Override
    public FileExtra byFileId(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");

        Query query = query(where("fileId").is(fileId));
        FileExtra fileExtra = mongoTemplate.findOne(query, FileExtra.class);

        if (isNull(fileExtra)) {
            throw new MyException(FILE_EXTRA_NOT_FOUND, "文件额外信息不存在", "fileId", fileId);
        }

        return fileExtra;
    }

    @Override
    public FileExtra cachedByFileId(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");

        return cachedFileExtraRepository.cachedByFileId(fileId);
    }

    @Override
    public void delete(FileExtra fileExtra) {
        super.delete(fileExtra);
        cachedFileExtraRepository.evictFileExtraCache(fileExtra.getFileId());
    }

    @Override
    public void delete(List<FileExtra> fileExtras) {
        super.delete(fileExtras);
        cachedFileExtraRepository.evictAll();
    }

    @Override
    public List<FileExtra> listByFileIds(List<String> fileIds) {
        if (isEmpty(fileIds)) {
            return Collections.emptyList();
        }

        Query query = query(where("fileId").in(fileIds));
        return mongoTemplate.find(query, FileExtra.class);
    }

    @Override
    public boolean existsByFileId(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");

        Query query = query(where("fileId").is(fileId));
        return mongoTemplate.exists(query, FileExtra.class);
    }
}
