package com.ricky.apitest.publicfile;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.comment.CommentApi;
import com.ricky.apitest.group.GroupApi;
import com.ricky.apitest.upload.FileUploadApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.permission.Permission;
import com.ricky.file.domain.File;
import com.ricky.group.command.AddGrantCommand;
import com.ricky.group.domain.InheritancePolicy;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import com.ricky.publicfile.domain.event.FileWithdrewEvent;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import com.ricky.upload.domain.event.FileUploadedLocalEvent;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Set;

import static com.ricky.apitest.RandomTestFixture.rDescription;
import static com.ricky.apitest.RandomTestFixture.rFilename;
import static com.ricky.common.event.DomainEventType.FILE_PUBLISHED;
import static com.ricky.common.event.DomainEventType.FILE_WITHDREW;
import static com.ricky.common.exception.ErrorCodeEnum.ACCESS_DENIED;
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
    void should_allow_posting_team_space_file_for_team_manager() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());
        String teamRootId = folderRepository.getRoot(groupRepository.byId(groupId).getCustomId()).getId();
        java.io.File file = new ClassPathResource("testdata/plain-text-file.txt").getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, teamRootId).getFileId();
        awaitLatestLocalEventConsumed(fileId, FileUploadedLocalEvent.class);

        // When
        String postId = PublicFileApi.post(manager.getJwt(), PostCommand.builder()
                .fileId(fileId)
                .build()).getPostId();

        // Then
        assertTrue(publicFileRepository.exists(postId));
    }

    @Test
    void should_allow_posting_team_space_file_for_member_with_public_permission() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, member.getUserId());

        String teamRootId = folderRepository.getRoot(groupRepository.byId(groupId).getCustomId()).getId();
        java.io.File file = new ClassPathResource("testdata/plain-text-file.txt").getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, teamRootId).getFileId();
        awaitLatestLocalEventConsumed(fileId, FileUploadedLocalEvent.class);

        GroupApi.addGrant(manager.getJwt(), AddGrantCommand.builder()
                .groupId(groupId)
                .memberId(member.getUserId())
                .folderId(teamRootId)
                .permissions(Set.of(Permission.PUBLIC))
                .inheritancePolicy(InheritancePolicy.NONE)
                .build());

        // When
        String postId = PublicFileApi.post(member.getJwt(), PostCommand.builder()
                .fileId(fileId)
                .build()).getPostId();

        // Then
        assertTrue(publicFileRepository.exists(postId));
    }

    @Test
    void should_fail_to_post_team_space_file_for_member_without_public_permission() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, member.getUserId());

        String teamRootId = folderRepository.getRoot(groupRepository.byId(groupId).getCustomId()).getId();
        java.io.File file = new ClassPathResource("testdata/plain-text-file.txt").getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, teamRootId).getFileId();
        awaitLatestLocalEventConsumed(fileId, FileUploadedLocalEvent.class);

        // When & Then
        assertError(() -> PublicFileApi.postRaw(member.getJwt(), PostCommand.builder()
                .fileId(fileId)
                .build()), ACCESS_DENIED);
    }

    @Test
    void should_withdraw_file() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        String postId = PublicFileApi.post(manager.getJwt(), fileId).getPostId();
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

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
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

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
        awaitLatestEventConsumed(postId, FILE_PUBLISHED, FilePublishedEvent.class);

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

        CommentApi.createComment(manager.getJwt(), postId3); // 评论数更新采用定时任务，不会立刻更新

        // When
        PagedList<PublicFileResponse> pagedList = PublicFileApi.page(manager.getJwt(), PublicFilePageQuery.builder()
                .search("plain")
                .sortedBy("commentCount")
                .ascSort(false)
                .pageIndex(1)
                .pageSize(10)
                .build());

        // Then
        assertFalse(pagedList.isEmpty());
//        assertEquals(1, pagedList.getData().get(0).getCommentCount());
    }

}
