package com.ricky.file.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoFileRepository extends MongoBaseRepository<File> implements FileRepository {

    private final MongoCachedFileRepository cachedFileRepository;

    @Override
    public boolean existsByHash(String hash) {
        requireNotBlank(hash, "File hash must not be blank");
        Query query = query(where("hash").is(hash));
        return mongoTemplate.exists(query, File.class);
    }

    @Override
    public File cachedById(String fileId) {
        requireNotBlank(fileId, "File ID must not be blank");
        return cachedFileRepository.cachedById(fileId);
    }

    @Override
    public File byId(String id) {
        return super.byId(id);
    }

    @Override
    public List<File> listByFileHash(String hash) {
        requireNotBlank(hash, "File hash must not be blank");
        Query query = query(where("hash").is(hash));
        return mongoTemplate.find(query, File.class);
    }

    @Override
    public void delete(List<File> files) {
        super.delete(files);
        cachedFileRepository.evictAll();
    }
}
