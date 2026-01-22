package com.ricky.apitest.comment;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.publicfile.PublicFileApi;
import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.event.CommentCreatedEvent;
import com.ricky.comment.domain.event.CommentDeletedEvent;
import com.ricky.commenthierarchy.domain.CommentHierarchy;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.publicfile.command.PostResponse;
import com.ricky.publicfile.domain.PublicFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.ricky.apitest.RandomTestFixture.rCommentContent;
import static com.ricky.common.event.DomainEventType.COMMENT_CREATED;
import static com.ricky.common.event.DomainEventType.COMMENT_DELETED;
import static com.ricky.common.exception.ErrorCodeEnum.PUBLIC_FILE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

public class CommentControllerTest extends BaseApiTest {

    @Test
    void should_create_comment() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();

        // When
        CreateCommentResponse response = CommentApi.createComment(manager.getJwt(), CreateCommentCommand.builder()
                .postId(postId)
                .content(rCommentContent())
                .build());

        // Then
        Comment comment = commentRepository.byId(response.getCommentId());
        assertEquals(postId, comment.getPostId());

        CommentHierarchy hierarchy = commentHierarchyRepository.byPostId(postId);
        assertTrue(hierarchy.containsCommentId(response.getCommentId()));

        CommentCreatedEvent evt = latestEventFor(comment.getId(), COMMENT_CREATED, CommentCreatedEvent.class);
        assertEquals(postId, evt.getPostId());
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
        String commentId = CommentApi.createComment(manager.getJwt(), postId).getCommentId();

        // When
        CommentApi.deleteComment(manager.getJwt(), DeleteCommentCommand.builder()
                .postId(postId)
                .commentId(commentId)
                .build());

        // Then
        assertFalse(commentRepository.exists(commentId));

        CommentHierarchy hierarchy = commentHierarchyRepository.byPostId(postId);
        assertFalse(hierarchy.containsCommentId(commentId));

        CommentDeletedEvent evt = latestEventFor(commentId, COMMENT_DELETED, CommentDeletedEvent.class);
        assertEquals(postId, evt.getPostId());
    }

}
