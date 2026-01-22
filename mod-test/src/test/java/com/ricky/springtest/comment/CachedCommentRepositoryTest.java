package com.ricky.springtest.comment;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.CommentCount;
import com.ricky.common.startup.CacheClearer;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CachedCommentRepositoryTest {

    @Autowired
    private CachedCommentRepository cachedCommentRepository;

    @Autowired
    private CacheClearer cacheClearer;

    @MockitoBean
    private PublicFileRepository publicFileRepository;

    private static final String POST_ID = "p1";
    private static final String POST_ID2 = "p2";

    @BeforeEach
    void setUp() {
        cacheClearer.evictAllCache();

        // mock PublicFile 默认存在，commentCount = 0
        mockPublicFile(POST_ID, 0);
        mockPublicFile(POST_ID2, 0);
    }

    /**
     * increaseCommentCount: 不存在 -> 回源 DB -> count +1
     */
    @Test
    void increase_comment_count_when_not_exist_should_init_and_increase() {
        cachedCommentRepository.increaseCommentCount(POST_ID, 1);

        CommentCount count = cachedCommentRepository.cachedById(POST_ID);
        assertEquals(1, count.getCommentCount());

        verify(publicFileRepository, times(1)).cachedById(POST_ID);
    }

    /**
     * increaseCommentCount: 已存在 -> 不再回源 DB
     */
    @Test
    void increase_comment_count_when_exist_should_not_query_db_again() {
        cachedCommentRepository.increaseCommentCount(POST_ID, 1);
        cachedCommentRepository.increaseCommentCount(POST_ID, 1);

        CommentCount count = cachedCommentRepository.cachedById(POST_ID);
        assertEquals(2, count.getCommentCount());

        verify(publicFileRepository, times(1)).cachedById(POST_ID);
    }

    /**
     * updateCommentCount: 覆盖成功
     */
    @Test
    void update_comment_count_should_override_value() {
        cachedCommentRepository.updateCommentCount(POST_ID, 10);

        CommentCount count = cachedCommentRepository.cachedById(POST_ID);
        assertEquals(10, count.getCommentCount());
    }

    /**
     * cachedById: 不存在 -> 回源 DB
     */
    @Test
    void cached_by_id_when_not_exist_should_query_db() {
        CommentCount count = cachedCommentRepository.cachedById(POST_ID);

        assertEquals(POST_ID, count.getPostId());
        assertEquals(0, count.getCommentCount());

        verify(publicFileRepository, times(1)).cachedById(POST_ID);
    }

    /**
     * cachedById: 已存在 -> 不再回源 DB
     */
    @Test
    void cached_by_id_when_exist_should_not_query_db_again() {
        cachedCommentRepository.cachedById(POST_ID);
        cachedCommentRepository.cachedById(POST_ID);

        verify(publicFileRepository, times(1)).cachedById(POST_ID);
    }

    /**
     * listAllCommentCount: 返回完整 Map
     */
    @Test
    void list_all_comment_count_should_return_all_entries() {
        cachedCommentRepository.increaseCommentCount(POST_ID, 1);
        cachedCommentRepository.increaseCommentCount(POST_ID2, 2);

        Map<String, Integer> result = cachedCommentRepository.listAllCommentCount();

        assertEquals(2, result.size());
        assertEquals(1, result.get(POST_ID));
        assertEquals(2, result.get(POST_ID2));
    }

    private void mockPublicFile(String postId, int commentCount) {
        PublicFile publicFile = mock(PublicFile.class);
        when(publicFile.getId()).thenReturn(postId);
        when(publicFile.getCommentCount()).thenReturn(commentCount);

        when(publicFileRepository.cachedById(postId))
                .thenReturn(publicFile);
    }
}
