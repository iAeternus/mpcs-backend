package com.ricky.common.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import static com.ricky.common.constants.ConfigConstant.FILE_CACHE;
import static com.ricky.common.constants.ConfigConstant.USER_CACHE;

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

    @Caching(evict = {
            @CacheEvict(value = USER_CACHE, allEntries = true),
            @CacheEvict(value = FILE_CACHE, allEntries = true),
    })
    public void evictAllCache() {
        log.info("Evicted all cache.");
    }
}