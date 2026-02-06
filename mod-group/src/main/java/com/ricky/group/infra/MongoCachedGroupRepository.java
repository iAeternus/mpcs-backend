package com.ricky.group.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.group.domain.CachedGroup;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.UserCachedGroup;
import com.ricky.group.domain.UserCachedGroups;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.exception.ErrorCodeEnum.GROUP_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Repository
public class MongoCachedGroupRepository extends MongoBaseRepository<Group> {

    @Cacheable(value = USER_GROUPS_CACHE, key = "#userId")
    public UserCachedGroups cachedUserAllGroups(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        Query query = query(where("userId").is(userId));
        query.fields().include("name", "active", "userId", "managers", "members", "grants", "inheritancePolicy");
        List<UserCachedGroup> groups = mongoTemplate.find(query, UserCachedGroup.class, GROUP_COLLECTION);
        return UserCachedGroups.builder()
                .groups(groups)
                .build();
    }

    @Caching(evict = {@CacheEvict(value = USER_GROUPS_CACHE, key = "#userId")})
    public void evictUserGroupsCache(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");

        log.debug("Evicted user groups cache for userId[{}].", userId);
    }

    @Cacheable(value = GROUPS_CACHE, key = "#groupId")
    public CachedGroup cachedById(String groupId) {
        requireNotBlank(groupId, "Group ID must not be blank.");

        Query query = query(where("_id").is(groupId));
        query.fields().include("name", "active", "managers", "members", "grants", "inheritancePolicy");
        CachedGroup cachedGroup = mongoTemplate.findOne(query, CachedGroup.class, GROUP_COLLECTION);
        if (isNull(cachedGroup)) {
            throw new MyException(GROUP_NOT_FOUND, "权限组不存在", "groupId", groupId);
        }

        return cachedGroup;
    }

    @Caching(evict = {@CacheEvict(value = GROUPS_CACHE, key = "#groupId")})
    public void evictGroupCache(String groupId) {
        requireNotBlank(groupId, "Group ID must not be blank.");

        log.debug("Evicted groups cache for groupId[{}].", groupId);
    }

}
