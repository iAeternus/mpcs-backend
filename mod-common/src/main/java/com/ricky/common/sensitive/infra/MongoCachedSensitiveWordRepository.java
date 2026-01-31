package com.ricky.common.sensitive.infra;

import com.ricky.common.sensitive.domain.SensitiveWord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.SENSITIVE_WORD_CACHE;

@Slf4j
@RequiredArgsConstructor
public class MongoCachedSensitiveWordRepository {

    private final MongoTemplate mongoTemplate;

    @Cacheable(value = SENSITIVE_WORD_CACHE)
    public List<SensitiveWord> cachedAll() {
        return mongoTemplate.findAll(SensitiveWord.class);
    }

    @Caching(evict = {@CacheEvict(value = SENSITIVE_WORD_CACHE, allEntries = true)})
    public void evictAll() {
        log.info("Evicting all sensitive words cache");
    }

}
