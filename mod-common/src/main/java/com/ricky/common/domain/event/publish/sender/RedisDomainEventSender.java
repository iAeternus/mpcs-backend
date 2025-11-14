package com.ricky.common.domain.event.publish.sender;

import com.ricky.common.domain.event.DomainEvent;
import com.ricky.common.domain.event.DomainEventDao;
import com.ricky.common.redis.RedisProperties;
import com.ricky.common.utils.MyObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

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

    private final MyObjectMapper objectMapper;
    private final RedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final DomainEventDao domainEventDao;

    @Override
    public void send(DomainEvent event) {
        try {
            String eventString = objectMapper.writeValueAsString(event);
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                    .ofObject(eventString)
                    .withStreamKey(redisProperties.getDomainEventStream());
            stringRedisTemplate.opsForStream().add(record);
            domainEventDao.successPublish(event);
        } catch (Throwable t) {
            log.error("MyError happened while publish domain event[{}:{}] to redis.", event.getType(), event.getId(), t);
            domainEventDao.failPublish(event);
        }
    }
}
