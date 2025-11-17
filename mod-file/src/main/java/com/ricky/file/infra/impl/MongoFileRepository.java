package com.ricky.file.infra.impl;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.file.domain.File;
import com.ricky.file.infra.FileRepository;
import com.ricky.file.infra.MongoCachedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Repository
@RequiredArgsConstructor
public class MongoFileRepository extends MongoBaseRepository<File> implements FileRepository {

    private final MongoCachedFileRepository cachedFileRepository;

    @Override
    public boolean existsByHash(String hash) {
        requireNotBlank(hash, "File hash must not be blank");
        Query query = Query.query(Criteria.where("metadata.hash").is(hash));
        return mongoTemplate.exists(query, File.class);
    }
}
