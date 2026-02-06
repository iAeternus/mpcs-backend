package com.ricky.apitest.comment;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.publicfile.PublicFileApi;
import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.comment.domain.Comment;
import com.ricky.comment.query.CommentPageQuery;
import com.ricky.comment.query.CommentResponse;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.ricky.apitest.RandomTestFixture.rCommentContent;
import static com.ricky.common.event.DomainEventType.FILE_PUBLISHED;
import static com.ricky.common.exception.ErrorCodeEnum.PUBLIC_FILE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

public class CommentControllerTest extends BaseApiTest {

    @Test
    void should_create_comment() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

        // When
        CreateCommentResponse response = CommentApi.createComment(manager.getJwt(), CreateCommentCommand.builder()
                .postId(postId)
                .content(rCommentContent())
                .build());

        // Then
        Comment comment = commentRepository.byId(response.getCommentId());
        assertEquals(postId, comment.getCustomId());

        assertTrue(commentDomainService.containsId(postId, comment.getId()));
    }

    @Test
    void should_reply() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

        // When
        String commentId = CommentApi.createComment(manager.getJwt(), postId).getCommentId();
        String replyId = CommentApi.createReply(manager.getJwt(), postId, commentId).getCommentId();

        // Then
        Comment reply = commentRepository.byId(replyId);
        assertEquals(postId, reply.getCustomId());

        assertTrue(commentDomainService.containsId(postId, commentId));
        assertTrue(commentDomainService.containsId(postId, replyId));
    }

    @Test
    void should_fail_to_create_comment_if_public_file_not_exists() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();

        // When & Then
        assertError(() -> CommentApi.createCommentRaw(manager.getJwt(), CreateCommentCommand.builder()
                .postId(PublicFile.newPublicFileId())
                .content(rCommentContent())
                .build()), PUBLIC_FILE_NOT_FOUND);
    }

    @Test
    void should_delete_comment() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

        String commentId = CommentApi.createComment(manager.getJwt(), postId).getCommentId();

        // When
        CommentApi.deleteComment(manager.getJwt(), DeleteCommentCommand.builder()
                .postId(postId)
                .commentId(commentId)
                .build());

        // Then
        assertFalse(commentRepository.exists(commentId));
        assertFalse(commentDomainService.containsId(postId, commentId));
    }

    @Test
    void should_fetch_comment_detail() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

        String commentId1 = CommentApi.createComment(manager.getJwt(), postId).getCommentId();

        // When
        CommentResponse response = CommentApi.fetchDetail(manager.getJwt(), commentId1);

        // Then
        assertEquals(postId, response.getPostId());
    }

    @Test
    void should_page() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

        String commentId1 = CommentApi.createComment(manager.getJwt(), postId).getCommentId();
        String commentId2 = CommentApi.createComment(manager.getJwt(), postId).getCommentId();
        String commentId3 = CommentApi.createComment(manager.getJwt(), postId).getCommentId();

        // When
        PagedList<CommentResponse> pagedList = CommentApi.page(manager.getJwt(), CommentPageQuery.builder()
                .postId(postId)
                .sortedBy("createdAt")
                .ascSort(false)
                .pageIndex(1)
                .pageSize(10)
                .build());

        // Then
        assertFalse(pagedList.isEmpty());
    }
}
