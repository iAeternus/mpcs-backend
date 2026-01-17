package com.ricky.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricky.file.domain.File;
import com.ricky.fileextra.domain.FileExtra;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.UserCachedFolders;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.UserCachedFolderHierarchies;
import com.ricky.group.domain.UserCachedGroups;
import com.ricky.publicfile.domain.PublicFile;
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
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), NON_FINAL, PROPERTY);
        GenericJackson2JsonRedisSerializer defaultSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        var userCacheSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, User.class);
        var fileCacheSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, File.class);
        var uploadSessionCacheSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, UploadSession.class);
        var userCachedFoldersSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, UserCachedFolders.class);
        var folderCacheSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Folder.class);
        var userCachedFolderHierarchiesSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, UserCachedFolderHierarchies.class);
        var folderHierarchyCacheSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, FolderHierarchy.class);
        var fileExtraCacheSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, FileExtra.class);
        var userCachedGroupSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, UserCachedGroups.class);
        var publicFileSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, PublicFile.class);

        return builder -> builder.cacheDefaults(defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(defaultSerializer))
                        .entryTtl(ofDays(1)))
                .withCacheConfiguration(USER_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(userCacheSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(FILE_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(fileCacheSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(UPLOAD_SESSION_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(uploadSessionCacheSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(USER_FOLDERS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(userCachedFoldersSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(FOLDER_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(folderCacheSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(USER_FOLDER_HIERARCHIES_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(userCachedFolderHierarchiesSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(FOLDER_HIERARCHY_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(folderHierarchyCacheSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(FILE_EXTRA_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(fileExtraCacheSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(USER_GROUPS_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(userCachedGroupSerializer))
                        .entryTtl(ofDays(7)))
                .withCacheConfiguration(PUBLIC_FILE_CACHE, defaultCacheConfig()
                        .prefixCacheNameWith(CACHE_PREFIX)
                        .serializeValuesWith(fromSerializer(publicFileSerializer))
                        .entryTtl(ofDays(7)))
                ;
    }
}
