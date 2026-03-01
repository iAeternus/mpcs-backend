package com.ricky.like.infra;

import com.ricky.common.utils.RedisKeyUtils;
import com.ricky.like.domain.CachedLikeRepository;
import com.ricky.like.domain.LikeRecord;
import com.ricky.like.domain.LikeStatus;
import com.ricky.like.domain.LikedCount;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ricky.common.constants.ConfigConstants.LIKED_COUNT_KEY;
import static com.ricky.common.constants.ConfigConstants.LIKE_KEY;
import static com.ricky.common.constants.LuaScriptConstants.*;
import static com.ricky.common.utils.RedisKeyUtils.SEPARATE;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static java.util.Collections.singletonList;

@Repository
@RequiredArgsConstructor
public class RedisCachedLikeRepository implements CachedLikeRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final PublicFileRepository publicFileRepository;

    @SuppressWarnings("unchecked")
    private static final RedisScript<List<Object>> HGETALL_AND_DEL_SCRIPT
            = new DefaultRedisScript<>(HGETALL_AND_DEL_LUA_SCRIPT, (Class<List<Object>>) (Class<?>) List.class);
    private static final RedisScript<Long> TRY_LIKE_SCRIPT = new DefaultRedisScript<>(TRY_LIKE_LUA, Long.class);
    private static final RedisScript<Long> TRY_UNLIKE_SCRIPT = new DefaultRedisScript<>(TRY_UNLIKE_LUA, Long.class);

    @Override
    public boolean tryLike(String userId, String postId) {
        String userPostKey = RedisKeyUtils.likedKey(userId, postId);

        Long result = stringRedisTemplate.execute(
                TRY_LIKE_SCRIPT,
                List.of(LIKE_KEY, LIKED_COUNT_KEY),
                userPostKey,
                postId,
                String.valueOf(LikeStatus.LIKE.getCode())
        );

        return result != null && result == 1;
    }

    @Override
    public boolean tryUnlike(String userId, String postId) {
        String userPostKey = RedisKeyUtils.likedKey(userId, postId);

        Long result = stringRedisTemplate.execute(
                TRY_UNLIKE_SCRIPT,
                List.of(LIKE_KEY, LIKED_COUNT_KEY),
                userPostKey,
                postId,
                String.valueOf(LikeStatus.LIKE.getCode()),
                String.valueOf(LikeStatus.UNLIKE.getCode())
        );

        return result != null && result == 1;
    }

    @Override
    public LikedCount cachedById(String postId) {
        Object value = stringRedisTemplate.opsForHash().get(LIKED_COUNT_KEY, postId);
        if (isNull(value)) {
            PublicFile publicFile = publicFileRepository.cachedById(postId);
            stringRedisTemplate.opsForHash().put(LIKED_COUNT_KEY, postId, String.valueOf(publicFile.getLikeCount()));
            return LikedCount.builder()
                    .postId(postId)
                    .count(publicFile.getLikeCount())
                    .build();
        }

        return LikedCount.builder()
                .postId(postId)
                .count(Integer.valueOf((String) value))
                .build();
    }


    @Override
    public List<LikeRecord> listAllLike() {
        List<Object> result = stringRedisTemplate.execute(HGETALL_AND_DEL_SCRIPT, singletonList(LIKE_KEY));
        if (isEmpty(result)) {
            return List.of();
        }

        List<LikeRecord> likes = new ArrayList<>(result.size() >> 1);
        for (int i = 0; i < result.size(); i += 2) {
            String key = (String) result.get(i);
            Integer value = Integer.valueOf((String) result.get(i + 1));

            String[] split = key.split(SEPARATE, SEPARATE.length());
            if (split.length < 2) {
                continue;
            }

            likes.add(LikeRecord.builder()
                    .userId(split[0])
                    .postId(split[1])
                    .status(LikeStatus.of(value))
                    .build());
        }
        return likes;
    }

    @Override
    public List<LikedCount> listAllLikedCount() {
        List<Object> result = stringRedisTemplate.execute(HGETALL_AND_DEL_SCRIPT, singletonList(LIKED_COUNT_KEY));
        if (isEmpty(result)) {
            return List.of();
        }

        List<LikedCount> likedCounts = new ArrayList<>(result.size() >> 1);
        for (int i = 0; i < result.size(); i += 2) {
            String postId = (String) result.get(i);
            Integer count = Integer.valueOf((String) result.get(i + 1));

            likedCounts.add(LikedCount.builder()
                    .postId(postId)
                    .count(count)
                    .build());
        }
        return likedCounts;
    }

    @Override
    public Map<String, Boolean> listLikeStatus(List<String> postIds, String userId) {
        List<Object> hashKeys = postIds.stream()
                .map(postId -> RedisKeyUtils.likedKey(userId, postId))
                .map(key -> (Object) key)
                .toList();

        List<Object> statuses = stringRedisTemplate.opsForHash().multiGet(LIKE_KEY, hashKeys);
        if (isEmpty(statuses)) {
            return Map.of();
        }

        return IntStream.range(0, Math.min(postIds.size(), statuses.size()))
                .filter(i -> statuses.get(i) != null)
                .boxed()
                .collect(Collectors.toMap(postIds::get,
                        i -> LikeStatus.LIKE.getCode().equals(Integer.valueOf(String.valueOf(statuses.get(i))))));
    }
}
