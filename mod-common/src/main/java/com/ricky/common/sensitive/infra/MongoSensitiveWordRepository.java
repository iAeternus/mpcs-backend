package com.ricky.common.sensitive.infra;

import com.ricky.common.sensitive.domain.SensitiveWord;
import com.ricky.common.sensitive.domain.SensitiveWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@RequiredArgsConstructor
public class MongoSensitiveWordRepository implements SensitiveWordRepository {

    private final MongoTemplate mongoTemplate;
    private final MongoCachedSensitiveWordRepository cachedSensitiveWordRepository;

    @Override
    public void insert(List<SensitiveWord> sensitiveWords) {
        mongoTemplate.insert(sensitiveWords, SensitiveWord.class);
        cachedSensitiveWordRepository.evictAll();
    }

    @Override
    public List<SensitiveWord> findAll() {
        return cachedSensitiveWordRepository.cachedAll();
    }
}
