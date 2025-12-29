package com.ricky.user.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.USER_CACHE;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Slf4j
@Repository
public class MongoCachedUserRepository extends MongoBaseRepository<User> {

    @Cacheable(value = USER_CACHE, key = "#userId")
    public User cachedById(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");
        return super.byId(userId);
    }

    @Caching(evict = {@CacheEvict(value = USER_CACHE, key = "#userId")})
    public void evictUserCache(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");
        log.debug("Evicted user cache for user[{}].", userId);
    }

}
