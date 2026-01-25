package com.ricky.comment.infra;

import com.ricky.comment.domain.Comment;
import com.ricky.common.mongo.MongoBaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;

import static com.ricky.common.constants.ConfigConstants.COMMENT_CACHE;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;

@Slf4j
@Repository
public class MongoCachedCommentRepository extends MongoBaseRepository<Comment> {

    @Cacheable(value = COMMENT_CACHE, key = "#commentId")
    public Comment cachedById(String commentId) {
        requireNotBlank(commentId, "Comment ID must not be blank.");
        return super.byId(commentId);
    }

    @Caching(evict = {@CacheEvict(value = COMMENT_CACHE, key = "#commentId")})
    public void evictCommentCache(String commentId) {
        requireNotBlank(commentId, "Comment ID must not be blank.");
        log.debug("Evicted comment cache for comment[{}].", commentId);
    }

    @Caching(evict = {@CacheEvict(value = COMMENT_CACHE, allEntries = true)})
    public void evictAll() {
        log.info("Evicted all caches for " + COMMENT_CACHE);
    }

}
