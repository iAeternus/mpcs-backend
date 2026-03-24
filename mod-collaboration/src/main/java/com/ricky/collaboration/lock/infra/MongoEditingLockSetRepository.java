package com.ricky.collaboration.lock.infra;

import com.ricky.collaboration.lock.domain.EditingLockSet;
import com.ricky.collaboration.lock.domain.EditingLockSetRepository;
import com.ricky.common.mongo.MongoBaseRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class MongoEditingLockSetRepository extends MongoBaseRepository<EditingLockSet>
        implements EditingLockSetRepository {

    public MongoEditingLockSetRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(EditingLockSet lockSet) {
        mongoTemplate.save(lockSet);
    }

    @Override
    public Optional<EditingLockSet> findBySessionId(String sessionId) {
        Query query = Query.query(where("sessionId").is(sessionId));
        return Optional.ofNullable(mongoTemplate.findOne(query, EditingLockSet.class));
    }
}
