package com.ricky.upload.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.upload.domain.UploadSession;
import com.ricky.upload.domain.UploadSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoUploadSessionRepository extends MongoBaseRepository<UploadSession> implements UploadSessionRepository {

    private final MongoCachedUploadSessionRepository cachedUploadSessionRepository;

    @Override
    public Optional<UploadSession> byFileHashAndOwnerIdOptional(String fileHash, String ownerId) {
        requireNotBlank(fileHash, "FileHash must not be blank");
        requireNotBlank(ownerId, "OwnerId must not be blank");

        Query query = query(where("fileHash").is(fileHash).and("ownerId").is(ownerId));
        UploadSession uploadSession = mongoTemplate.findOne(query, UploadSession.class); // TODO imm 这里查找出来的是否唯一？
        return isNull(uploadSession) ? Optional.empty() : Optional.of(uploadSession);
    }

    @Override
    public void save(UploadSession uploadSession) {
        super.save(uploadSession);
        cachedUploadSessionRepository.evictUploadSessionCache(uploadSession.getId());
    }

    @Override
    public UploadSession cachedById(String uploadId) {
        return cachedUploadSessionRepository.cachedById(uploadId);
    }

    @Override
    public UploadSession byId(String id) {
        return super.byId(id);
    }
}
