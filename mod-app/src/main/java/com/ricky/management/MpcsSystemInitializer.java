package com.ricky.management;

import com.ricky.common.cache.CacheClearer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import static com.ricky.common.constants.ConfigConstant.*;
import static java.util.Locale.CHINESE;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.CollectionOptions.just;
import static org.springframework.data.mongodb.core.query.Collation.of;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpcsSystemInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final CacheClearer cacheClearer;
    private final MongoTemplate mongoTemplate;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        cacheClearer.evictAllCache();
        ensureMongoCollectionExist();
        ensureMongoIndexExist();
    }

    private void ensureMongoCollectionExist() {
        createCollection(EVENT_COLLECTION);
        createCollection(USER_COLLECTION);
        createCollection(FILE_COLLECTION);
        createCollection(FOLDER_COLLECTION);
        createCollection(COMMUNITY_POST_COLLECTION);
        createCollection(TEAM_COLLECTION);
    }

    private void createCollection(String collectionName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName, just(of(CHINESE).numericOrderingEnabled()));
        }
    }

    private void ensureMongoIndexExist() {
        ensureDomainEventIndex();
    }

    private void ensureDomainEventIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(EVENT_COLLECTION);
        indexOperations.createIndex(new Index().on("status", DESC));
        indexOperations.createIndex(new Index().on("publishedCount", DESC));
        indexOperations.createIndex(new Index().on("consumedCount", DESC));
        indexOperations.createIndex(new Index().on("raisedAt", DESC));
    }
}
