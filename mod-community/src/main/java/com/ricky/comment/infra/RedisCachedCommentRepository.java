package com.ricky.comment.infra;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.CommentCount;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.ricky.common.constants.ConfigConstants.COMMENT_COUNT_KEY;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.nonNull;

@Repository
@RequiredArgsConstructor
public class RedisCachedCommentRepository implements CachedCommentRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PublicFileRepository publicFileRepository;

    @Override
    public void increaseCommentCount(String postId, Integer delta) {
        Boolean exists = redisTemplate.opsForHash().hasKey(COMMENT_COUNT_KEY, postId);
        if (!exists) {
            PublicFile publicFile = publicFileRepository.cachedById(postId);
            redisTemplate.opsForHash().putIfAbsent(COMMENT_COUNT_KEY, postId, publicFile.getCommentCount());
        }

        redisTemplate.opsForHash().increment(COMMENT_COUNT_KEY, postId, delta);
    }

    @Override
    public void updateCommentCount(String postId, Integer newCommentCount) {
        redisTemplate.opsForHash().put(COMMENT_COUNT_KEY, postId, newCommentCount);
    }

    @Override
    public CommentCount cachedById(String postId) {
        Integer count = (Integer) redisTemplate.opsForHash().get(COMMENT_COUNT_KEY, postId);
        if (nonNull(count)) {
            return CommentCount.builder()
                    .postId(postId)
                    .commentCount(count)
                    .build();
        }

        PublicFile publicFile = publicFileRepository.cachedById(postId);
        redisTemplate.opsForHash().put(COMMENT_COUNT_KEY, postId, publicFile.getCommentCount());
        return CommentCount.builder()
                .postId(postId)
                .commentCount(publicFile.getCommentCount())
                .build();
    }

    @Override
    public Map<String, Integer> listAllCommentCount() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(COMMENT_COUNT_KEY);
        if (isEmpty(entries)) {
            return Map.of();
        }

        return entries.entrySet()
                .stream()
                .collect(toImmutableMap(
                        e -> (String) e.getKey(),
                        e -> (Integer) e.getValue()
                ));
    }
}
