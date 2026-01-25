package com.ricky.apitest.publicfile;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.comment.CommentApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.file.domain.File;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import com.ricky.publicfile.domain.event.FileWithdrewEvent;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.ricky.apitest.RandomTestFixture.rDescription;
import static com.ricky.apitest.RandomTestFixture.rFilename;
import static com.ricky.common.event.DomainEventType.FILE_PUBLISHED;
import static com.ricky.common.event.DomainEventType.FILE_WITHDREW;
import static com.ricky.common.exception.ErrorCodeEnum.AR_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

public class PublicFileControllerTest extends BaseApiTest {

    @Test
    void should_post_file() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();

        // When
        String postId = PublicFileApi.post(manager.getJwt(), PostCommand.builder()
                .fileId(fileId)
                .build()).getPostId();

        // Then
        PublicFile publicFile = publicFileRepository.byId(postId);
        assertEquals(postId, publicFile.getId());

        FilePublishedEvent evt = latestEventFor(postId, FILE_PUBLISHED, FilePublishedEvent.class);
        assertEquals(postId, evt.getPostId());
    }

    @Test
    void should_fail_to_post_if_file_not_exists() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();

        // When & Then
        assertError(() -> PublicFileApi.postRaw(manager.getJwt(), PostCommand.builder()
                .fileId(File.newFileId())
                .build()), AR_NOT_FOUND);
    }

    @Test
    void should_withdraw_file() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String postId = PublicFileApi.post(manager.getJwt(), fileId).getPostId();

        // When
        PublicFileApi.withdraw(manager.getJwt(), postId);

        // Then
        assertFalse(publicFileRepository.exists(postId));
        assertTrue(fileRepository.exists(fileId));

        FileWithdrewEvent evt = latestEventFor(postId, FILE_WITHDREW, FileWithdrewEvent.class);
        assertEquals(postId, evt.getPostId());
    }

    @Test
    void should_update_title() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String postId = PublicFileApi.post(manager.getJwt(), fileId).getPostId();

        String newTitle = rFilename();

        // When
        PublicFileApi.updateTitle(manager.getJwt(), ModifyTitleCommand.builder()
                .postId(postId)
                .newTitle(newTitle)
                .build());

        // Then
        PublicFile dbPublicFile = publicFileRepository.byId(postId);
        assertEquals(newTitle, dbPublicFile.getTitle());
    }

    @Test
    void should_edit_description() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String postId = PublicFileApi.post(manager.getJwt(), fileId).getPostId();

        String newDescription = rDescription();

        // When
        PublicFileApi.editDescription(manager.getJwt(), EditDescriptionCommand.builder()
                .postId(postId)
                .newDescription(newDescription)
                .build());

        // Then
        PublicFile dbPublicFile = publicFileRepository.byId(postId);
        assertEquals(newDescription, dbPublicFile.getDescription());
    }

    @Test
    void should_page() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();

        String postId1 = PublicFileApi.post(manager.getJwt(), fileId).getPostId();
        String postId2 = PublicFileApi.post(manager.getJwt(), fileId).getPostId();
        String postId3 = PublicFileApi.post(manager.getJwt(), fileId).getPostId();

        CommentApi.createComment(manager.getJwt(), postId3);

        // When
        PagedList<PublicFileResponse> pagedList = PublicFileApi.page(manager.getJwt(), PublicFilePageQuery.builder()
                .search("plain")
                .sortedBy("commentCount")
                .ascSort(false)
                .pageIndex(1)
                .pageSize(10)
                .build());

        // Then
        assertEquals(3, pagedList.size());
    }

}
