package com.ricky.common.startup;

import com.ricky.common.properties.RedisProperties;
import io.lettuce.core.RedisBusyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import static com.ricky.common.constants.ConfigConstants.*;

/**
 * @brief Redis流初始化
 */
@Slf4j
public class RedisStreamInitializer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisProperties redisProperties;

    public RedisStreamInitializer(RedisTemplate<String, Object> redisTemplate, RedisProperties redisProperties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = redisProperties;
        ensureConsumerGroupsExist();
    }

    private void ensureConsumerGroupsExist() {
        StreamOperations<String, Object, Object> operations = redisTemplate.opsForStream();
        redisProperties.allDomainEventStreams().forEach(stream -> tryCreateConsumerGroup(operations, stream, REDIS_DOMAIN_EVENT_CONSUMER_GROUP));
        tryCreateConsumerGroup(operations, redisProperties.getWebhookStream(), REDIS_WEBHOOK_CONSUMER_GROUP);
        tryCreateConsumerGroup(operations, redisProperties.getNotificationStream(), REDIS_NOTIFICATION_CONSUMER_GROUP);
    }

    private void tryCreateConsumerGroup(StreamOperations<String, Object, Object> operations, String streamKey, String group) {
        try {
            operations.createGroup(streamKey, group);
            log.info("Created redis consumer group[{}] for stream[{}].", group, streamKey);
        } catch (RedisSystemException ex) {
            var cause = ex.getRootCause();
            if (cause != null && RedisBusyException.class.equals(cause.getClass())) {
                log.warn("Redis group[{}] for stream[{}] already exists, skip create group.", group, streamKey);
            } else {
                throw ex;
            }
        }
    }

}
