package com.ricky.common.event.publish.infra;

import com.ricky.common.event.DomainEvent;
import com.ricky.common.event.publish.DomainEventSender;
import com.ricky.common.json.JsonCodec;
import com.ricky.common.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/10
 * @className RedisDomainEventSender
 * @desc 使用Redis流发送领域事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDomainEventSender implements DomainEventSender {

    private final JsonCodec jsonCodec;
    private final RedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public CompletableFuture<String> send(DomainEvent event) {
        try {
            String eventString = jsonCodec.writeValueAsString(event);
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                    .ofObject(eventString)
                    .withStreamKey(redisProperties.domainEventStreamForUser(event.getArUserId()));
            stringRedisTemplate.opsForStream().add(record);
            return CompletableFuture.completedFuture(event.getId());
        } catch (Throwable t) {
            log.error("Error happened while publish domain event[{}:{}] to redis.", event.getType(), event.getId(), t);
            return CompletableFuture.failedFuture(t);
        }
    }
}
