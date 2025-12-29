package com.ricky.common.event.consume.infra;

import com.ricky.common.event.DomainEvent;
import com.ricky.common.event.consume.ConsumingDomainEvent;
import com.ricky.common.event.consume.DomainEventConsumer;
import com.ricky.common.profile.NonCiProfile;
import com.ricky.common.properties.RedisProperties;
import com.ricky.common.tracing.TracingService;
import com.ricky.common.utils.MyObjectMapper;
import io.micrometer.tracing.ScopedSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.util.ErrorHandler;

import static com.ricky.common.constants.ConfigConstants.REDIS_DOMAIN_EVENT_CONSUMER_GROUP;
import static org.springframework.data.redis.connection.stream.Consumer.from;
import static org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed;
import static org.springframework.data.redis.connection.stream.StreamOffset.create;

@Slf4j
@Configuration
@NonCiProfile
@RequiredArgsConstructor
@DependsOn("redisStreamInitializer")
@ConditionalOnProperty(value = "mpcs.redis.domainEventStreamEnabled", havingValue = "true")
public class RedisDomainEventConsumeConfiguration {
    private final RedisProperties redisProperties;
    private final MyObjectMapper objectMapper;
    private final DomainEventConsumer<DomainEvent> domainEventConsumer;
    private final TracingService tracingService;

    @Bean
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> domainEventContainer(RedisConnectionFactory factory) {
        var options = StreamMessageListenerContainerOptions
                .builder()
                .batchSize(20)
                .executor(new SimpleAsyncTaskExecutor("mpcs-event-"))
                .targetType(String.class)
                .errorHandler(new RedisErrorHandler())
                .build();

        var container = StreamMessageListenerContainer.create(factory, options);

        redisProperties.allDomainEventStreams().forEach(stream -> {
            container.receiveAutoAck(
                    from(REDIS_DOMAIN_EVENT_CONSUMER_GROUP, "DomainEventRedisStreamConsumer-" + stream),
                    create(stream, lastConsumed()),
                    message -> {
                        ScopedSpan scopedSpan = tracingService.startNewSpan("domain-event-listener");

                        String jsonString = message.getValue();
                        DomainEvent domainEvent = objectMapper.readValue(jsonString, DomainEvent.class);
                        try {
                            domainEventConsumer.consume(new ConsumingDomainEvent<>(domainEvent.getId(), domainEvent.getType().name(), domainEvent));
                        } catch (Throwable t) {
                            log.error("Failed to listen domain event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
                        }

                        scopedSpan.end();
                    });
        });

        container.start();
        log.info("Start consuming domain events from redis stream.");
        return container;
    }

    @Slf4j
    private static class RedisErrorHandler implements ErrorHandler {
        @Override
        public void handleError(Throwable t) {
            log.error(t.getMessage(), t);
        }
    }
}

