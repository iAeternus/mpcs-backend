package com.ricky.revision.infra;

import com.ricky.revision.domain.DocumentRevision;
import com.ricky.revision.domain.DocumentRevisionRepository;
import com.ricky.common.mongo.MongoBaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class MongoDocumentRevisionRepository extends MongoBaseRepository<DocumentRevision>
        implements DocumentRevisionRepository {

    public MongoDocumentRevisionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(DocumentRevision revision) {
        mongoTemplate.save(revision);
    }

    @Override
    public Optional<DocumentRevision> findById(String revisionId) {
        return Optional.ofNullable(mongoTemplate.findById(revisionId, DocumentRevision.class));
    }

    @Override
    public List<DocumentRevision> findByDocumentId(String documentId) {
        Query query = Query.query(where("documentId").is(documentId))
                .with(Sort.by(Sort.Direction.DESC, "revisionNo"));
        return mongoTemplate.find(query, DocumentRevision.class);
    }

    @Override
    public Optional<DocumentRevision> findLatestByDocumentId(String documentId) {
        Query query = Query.query(where("documentId").is(documentId))
                .with(Sort.by(Sort.Direction.DESC, "revisionNo"))
                .limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, DocumentRevision.class));
    }
}
