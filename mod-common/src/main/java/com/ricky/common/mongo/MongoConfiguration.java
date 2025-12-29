package com.ricky.common.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoManagedTypes;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import static com.mongodb.ReadPreference.secondaryPreferred;
import static com.mongodb.WriteConcern.MAJORITY;
import static org.springframework.data.mongodb.core.WriteResultChecking.EXCEPTION;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className MongoConfig
 * @desc
 */
@Configuration
public class MongoConfiguration {

    /**
     * 获取配置文件中数据库信息
     */
    @Value("${spring.data.mongodb.database}")
    String db;

    @Bean
    MongoManagedTypes mongoManagedTypes(ApplicationContext applicationContext) throws ClassNotFoundException {
        return MongoManagedTypes.fromIterable(new EntityScanner(applicationContext).scan(Persistent.class));
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> builder.applyToConnectionPoolSettings(poolBuilder -> poolBuilder.maxSize(500).minSize(5));
    }

    @Bean
    MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter converter) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);
        mongoTemplate.setWriteConcern(MAJORITY);
        mongoTemplate.setWriteConcernResolver(action -> MAJORITY);
        mongoTemplate.setWriteResultChecking(EXCEPTION);
        mongoTemplate.setReadPreference(secondaryPreferred());
        return mongoTemplate;
    }

    /**
     * GridFSBucket用于打开下载流
     *
     * @param mongoClient MongoClient
     * @return GridFSBucket
     */
    @Bean
    public GridFSBucket getGridFsBucket(MongoClient mongoClient) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(db);
        return GridFSBuckets.create(mongoDatabase);
    }


}
