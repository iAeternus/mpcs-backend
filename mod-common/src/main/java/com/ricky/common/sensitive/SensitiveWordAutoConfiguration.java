package com.ricky.common.sensitive;

import com.ricky.common.sensitive.domain.SensitiveWordRepository;
import com.ricky.common.sensitive.infra.MongoCachedSensitiveWordRepository;
import com.ricky.common.sensitive.infra.MongoSensitiveWordRepository;
import com.ricky.common.sensitive.service.SensitiveWordService;
import com.ricky.common.sensitive.service.impl.SensitiveWordServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@AutoConfiguration
public class SensitiveWordAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "mpcs.sensitive")
    public SensitiveWordProperties sensitiveWordProperties() {
        return new SensitiveWordProperties();
    }

    @Bean
    public MongoCachedSensitiveWordRepository cachedSensitiveWordRepository(MongoTemplate mongoTemplate) {
        return new MongoCachedSensitiveWordRepository(mongoTemplate);
    }

    @Bean
    public SensitiveWordRepository sensitiveWordRepository(MongoTemplate mongoTemplate,
                                                           MongoCachedSensitiveWordRepository cachedRepository) {
        return new MongoSensitiveWordRepository(mongoTemplate, cachedRepository);
    }

    @Bean
    public SensitiveWordService sensitiveWordService(SensitiveWordProperties properties,
                                                     SensitiveWordRepository repository) {
        return SensitiveWordServiceImpl.newInstance(properties)
                .repository(repository)
                .init();
    }

}
