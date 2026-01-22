package com.ricky.apitest.commenthierarchy;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.comment.CommentApi;
import com.ricky.apitest.publicfile.PublicFileApi;
import com.ricky.comment.domain.Comment;
import com.ricky.commenthierarchy.command.ReplyCommand;
import com.ricky.commenthierarchy.command.ReplyResponse;
import com.ricky.commenthierarchy.domain.CommentHierarchy;
import com.ricky.common.domain.dto.resp.LoginResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.ricky.apitest.RandomTestFixture.rCommentContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommentHierarchyControllerTest extends BaseApiTest {

    @Test
    void should_reply() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        String postId = PublicFileApi.post(manager.getJwt(), ctx.getFileId()).getPostId();
        String commentId = CommentApi.createComment(manager.getJwt(), postId).getCommentId();

        // When
        ReplyResponse response = CommentHierarchyApi.reply(manager.getJwt(), ReplyCommand.builder()
                .postId(postId)
                .parentId(commentId)
                .content(rCommentContent())
                .build());

        // Then
        Comment comment = commentRepository.byId(response.getCommentId());
        assertEquals(postId, comment.getPostId());

        CommentHierarchy hierarchy = commentHierarchyRepository.byPostId(postId);
        assertTrue(hierarchy.containsCommentId(response.getCommentId()));
    }

}
