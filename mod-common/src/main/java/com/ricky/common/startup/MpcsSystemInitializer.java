package com.ricky.common.startup;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

import static com.ricky.common.constants.ConfigConstants.*;
import static java.util.Locale.CHINESE;
import static java.util.TimeZone.getTimeZone;
import static java.util.TimeZone.setDefault;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.CollectionOptions.just;
import static org.springframework.data.mongodb.core.query.Collation.of;

/**
 * @brief MPCS系统初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MpcsSystemInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final CacheClearer cacheClearer;
    private final MongoTemplate mongoTemplate;

    @PostConstruct
    void init() {
        setDefault(getTimeZone(ZoneId.of(CHINA_TIME_ZONE)));
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        cacheClearer.evictAllCache();
        ensureMongoCollectionExist();
        ensureMongoIndexExist();
        log.info("MPCS system initialized.");
    }

    private void ensureMongoCollectionExist() {
        createCollection(USER_COLLECTION);
        createCollection(FILE_COLLECTION);
        createCollection(FOLDER_COLLECTION);
        createCollection(COMMUNITY_POST_COLLECTION);
        createCollection(TEAM_COLLECTION);
        createCollection(PUBLISHING_DOMAIN_EVENT_COLLECTION);
        createCollection(CONSUMING_DOMAIN_EVENT_COLLECTION);
    }

    private void createCollection(String collectionName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName, just(of(CHINESE).numericOrderingEnabled()));
        }
    }

    private void ensureMongoIndexExist() {
        ensurePublishingDomainEventIndex();
        ensureConsumingDomainEventIndex();
    }

    private void ensurePublishingDomainEventIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(PUBLISHING_DOMAIN_EVENT_COLLECTION);
        indexOperations.createIndex(new Index().on("status", DESC));
        indexOperations.createIndex(new Index().on("publishedCount", DESC));
        indexOperations.createIndex(new Index().on("raisedAt", DESC));
    }

    private void ensureConsumingDomainEventIndex() {
        IndexOperations indexOperations = mongoTemplate.indexOps(CONSUMING_DOMAIN_EVENT_COLLECTION);
        indexOperations.createIndex(new Index().on("eventId", DESC));
    }
}
