package com.ricky.common.cache;

import com.ricky.common.utils.MyObjectMapper;
import com.ricky.file.domain.File;
import com.ricky.file.domain.HashCachedStorageIds;
import com.ricky.folder.domain.UserCachedFolders;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.upload.domain.UploadSession;
import com.ricky.user.domain.User;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.ricky.common.constants.ConfigConstants.*;
import static java.time.Duration.ofDays;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/10/14
 * @className CacheConfiguration
 * @desc
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {

    /**
     * 缓存名称前缀
     */
    private static final String CACHE_PREFIX = "Cache:";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer(MyObjectMapper objectMapper) {
        MyObjectMapper defaultObjectMapper = new MyObjectMapper();
        defaultObjectMapper.activateDefaultTyping(defaultObjectMapper.getPolymorphicTypeValidator(), NON_FINAL, PROPERTY);
        GenericJackson2JsonRedisSerializer defaultSerializer = new GenericJackson2JsonRedisSerializer(defaultObjectMapper);

        var userCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, User.class);
        var fileCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, File.class);
        var uploadCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, UploadSession.class);
        var hashCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, HashCachedStorageIds.class);
        var userFolderCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, UserCachedFolders.class);
        var folderHierarchyCachedMembersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, FolderHierarchy.class);

        return builder -> builder.cacheDefaults(defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(defaultSerializer))
                        .entryTtl(ofDays(1)))
                .withCacheConfiguration(USER_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(userCachedMembersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(FILE_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(fileCachedMembersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(UPLOAD_SESSION_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(uploadCachedMembersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(HASH_STORAGES_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(hashCachedMembersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(USER_FOLDERS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(userFolderCachedMembersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(FOLDER_HIERARCHY_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(folderHierarchyCachedMembersSerializer))
                        .entryTtl(ofDays(7)));
    }
}
