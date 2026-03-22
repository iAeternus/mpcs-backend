package com.ricky.collaboration.collaboration.infra;

import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.CollaborationSessionRepository;
import com.ricky.common.mongo.MongoBaseRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class MongoCollaborationSessionRepository 
        extends MongoBaseRepository<CollaborationSession> 
        implements CollaborationSessionRepository {
    
    public MongoCollaborationSessionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public void save(CollaborationSession session) {
        mongoTemplate.save(session);
    }
    
    @Override
    public void delete(CollaborationSession session) {
        mongoTemplate.remove(session);
    }
    
    @Override
    public Optional<CollaborationSession> findById(String sessionId) {
        Query query = Query.query(where("_id").is(sessionId));
        CollaborationSession session = mongoTemplate.findOne(query, CollaborationSession.class);
        return Optional.ofNullable(session);
    }
    
    @Override
    public Optional<CollaborationSession> findByDocumentId(String documentId) {
        Query query = Query.query(where("documentId").is(documentId));
        CollaborationSession session = mongoTemplate.findOne(query, CollaborationSession.class);
        return Optional.ofNullable(session);
    }
    
    @Override
    public List<CollaborationSession> findByUserId(String oderId) {
        Query query = Query.query(where("activeUsers.oderId").is(oderId));
        return mongoTemplate.find(query, CollaborationSession.class);
    }
    
    @Override
    public List<CollaborationSession> findActiveSessions() {
        Query query = Query.query(where("expiresAt").gt(Instant.now()));
        return mongoTemplate.find(query, CollaborationSession.class);
    }
    
    @Override
    public boolean existsByDocumentId(String documentId) {
        Query query = Query.query(where("documentId").is(documentId));
        return mongoTemplate.exists(query, CollaborationSession.class);
    }
    
    @Override
    public void deleteExpiredSessions() {
        Query query = Query.query(where("expiresAt").lt(Instant.now()));
        mongoTemplate.remove(query, CollaborationSession.class);
    }
    
    @Override
    public boolean existsById(String sessionId) {
        Query query = Query.query(where("_id").is(sessionId));
        return mongoTemplate.exists(query, CollaborationSession.class);
    }
}
