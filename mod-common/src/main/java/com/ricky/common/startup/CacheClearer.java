package com.ricky.common.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.ricky.common.constants.ConfigConstants.*;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/10/14
 * @className CacheClearer
 * @desc
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClearer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Caching(evict = {
            @CacheEvict(value = USER_CACHE, allEntries = true),
            @CacheEvict(value = FILE_CACHE, allEntries = true),
            @CacheEvict(value = UPLOAD_SESSION_CACHE, allEntries = true),
            @CacheEvict(value = USER_FOLDERS_CACHE, allEntries = true),
            @CacheEvict(value = FOLDER_CACHE, allEntries = true),
            @CacheEvict(value = FOLDER_HIERARCHY_CACHE, allEntries = true),
            @CacheEvict(value = FILE_EXTRA_CACHE, allEntries = true),
            @CacheEvict(value = USER_GROUPS_CACHE, allEntries = true),
            @CacheEvict(value = PUBLIC_FILE_CACHE, allEntries = true),
    })
    public void evictAllCache() {
        stringRedisTemplate.delete(LIKE_KEY);
        stringRedisTemplate.delete(LIKED_COUNT_KEY);
        redisTemplate.delete(COMMENT_COUNT_KEY);

        log.info("Evicted all cache.");
    }

}