package com.ricky.springtest.like;

import com.ricky.common.startup.CacheClearer;
import com.ricky.like.domain.CachedLikeRepository;
import com.ricky.like.domain.LikedCount;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class CachedLikeRepositoryTest {

    @Autowired
    private CachedLikeRepository cachedLikeRepository;

    @Autowired
    private CacheClearer cacheClearer;

    @MockitoBean
    private PublicFileRepository publicFileRepository;

    private static final String USER_ID = "u1";
    private static final String POST_ID = "p1";

    @BeforeEach
    void setUp() {
        cacheClearer.evictAllCache();

        // mock PublicFile 默认存在，likeCount = 0
        PublicFile publicFile = mock(PublicFile.class);
        when(publicFile.getId()).thenReturn(POST_ID);
        when(publicFile.getLikeCount()).thenReturn(0);

        when(publicFileRepository.cachedById(POST_ID))
                .thenReturn(publicFile);
    }

    /**
     * tryLike: 未点赞 -> true, count +1
     */
    @Test
    void try_like_first_time_should_increase_count() {
        boolean changed = cachedLikeRepository.tryLike(USER_ID, POST_ID);

        assertTrue(changed);

        LikedCount count = cachedLikeRepository.cachedById(POST_ID);
        assertEquals(1, count.getCount());
    }

    /**
     * tryLike: 已点赞 -> false, count 不变
     */
    @Test
    void try_like_twice_should_not_increase_count() {
        cachedLikeRepository.tryLike(USER_ID, POST_ID);

        boolean changed = cachedLikeRepository.tryLike(USER_ID, POST_ID);

        assertFalse(changed);

        LikedCount count = cachedLikeRepository.cachedById(POST_ID);
        assertEquals(1, count.getCount());
    }

    /**
     * tryUnlike: 已点赞 -> true, count -1
     */
    @Test
    void try_unlike_after_like_should_decrease_count() {
        cachedLikeRepository.tryLike(USER_ID, POST_ID);

        boolean changed = cachedLikeRepository.tryUnlike(USER_ID, POST_ID);

        assertTrue(changed);

        LikedCount count = cachedLikeRepository.cachedById(POST_ID);
        assertEquals(0, count.getCount());
    }

    /**
     * tryUnlike: 未点赞 -> false, count 不变
     */
    @Test
    void try_unlike_without_like_should_do_nothing() {
        boolean changed = cachedLikeRepository.tryUnlike(USER_ID, POST_ID);

        assertFalse(changed);

        LikedCount count = cachedLikeRepository.cachedById(POST_ID);
        assertEquals(0, count.getCount());
    }

    /**
     * cachedById: 不存在 -> count = 0
     */
    @Test
    void cached_by_id_when_not_exist_should_return_zero() {
        LikedCount count = cachedLikeRepository.cachedById(POST_ID);

        assertEquals(POST_ID, count.getPostId());
        assertEquals(0, count.getCount());
    }

    /**
     * Lua 原子性：like / unlike 不出现负数
     */
    @Test
    void like_and_unlike_should_never_go_negative() {
        cachedLikeRepository.tryUnlike(USER_ID, POST_ID);
        cachedLikeRepository.tryUnlike(USER_ID, POST_ID);

        LikedCount count = cachedLikeRepository.cachedById(POST_ID);
        assertTrue(count.getCount() >= 0);
        assertEquals(0, count.getCount());
    }
}