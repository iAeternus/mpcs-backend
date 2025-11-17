package com.ricky.common.event.publish.infra;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.ricky.common.event.publish.DomainEventPublisher;
import com.ricky.common.event.publish.PublishingDomainEvent;
import com.ricky.common.profile.NonCiProfile;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.messaging.ChangeStreamRequest;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListener;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;

import static com.ricky.common.constants.ConfigConstant.PUBLISHING_DOMAIN_EVENT_COLLECTION;

@Configuration
@NonCiProfile
public class MongoDomainEventPublishConfiguration {

    @Bean(destroyMethod = "stop")
    MessageListenerContainer mongoDomainEventChangeStreamListenerContainer(
            MongoTemplate mongoTemplate,
            TaskExecutor taskExecutor,
            DomainEventPublisher domainEventPublisher) {
        MessageListenerContainer container = new DefaultMessageListenerContainer(mongoTemplate, taskExecutor);

        // Get notification on DomainEvent insertion in MongoDB, then publish staged domain events to messaging middleware such as Kafka
        container.register(ChangeStreamRequest.builder(
                        (MessageListener<ChangeStreamDocument<Document>, PublishingDomainEvent>) message -> {
                            domainEventPublisher.publishStagedDomainEvents();
                        })
                .collection(PUBLISHING_DOMAIN_EVENT_COLLECTION)
                .filter(new Document("$match", new Document("operationType", OperationType.INSERT.getValue())))
                .build(), PublishingDomainEvent.class);
        container.start();
        return container;
    }
}