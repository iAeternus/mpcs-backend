package com.ricky.apitest.collaboration;

import com.ricky.apitest.BaseApiTest;
import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import com.ricky.collaboration.collaboration.infra.CollaborationSessionManager;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import com.ricky.collaboration.lock.command.AcquireEditingLockCommand;
import com.ricky.collaboration.lock.query.EditingLockResponse;
import com.ricky.collaboration.lock.query.EditingLockStateResponse;
import com.ricky.collaboration.revision.command.CreateRevisionCommand;
import com.ricky.collaboration.revision.query.RevisionDetailResponse;
import com.ricky.collaboration.revision.query.RevisionDiffResponse;
import com.ricky.collaboration.revision.query.RevisionSummaryResponse;
import com.ricky.common.domain.dto.resp.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ricky.apitest.RandomTestFixture.rDocumentId;
import static com.ricky.common.exception.ErrorCodeEnum.COLLAB_SESSION_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

public class CollaborationControllerTest extends BaseApiTest {

    @Autowired
    private CollaborationSessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        super.setUp();
        sessionManager.startHeartbeatChecker();
    }

    @Nested
    @DisplayName("Session creation tests")
    class SessionCreationTests {

        @Test
        void should_create_session() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .ttlHours(24)
                    .build();

            SessionInfoResponse response = CollaborationApi.createSession(manager.getJwt(), command);

            assertNotNull(response);
            assertEquals(documentId, response.getDocumentId());
            assertEquals("Test Document", response.getDocumentTitle());
            assertEquals(1, response.getActiveUserCount());
            assertFalse(response.isExpired());
        }

        @Test
        void should_create_session_with_parent_folder_id() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();
            String parentFolderId = "FLD123456789012345678";

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .parentFolderId(parentFolderId)
                    .build();

            SessionInfoResponse response = CollaborationApi.createSession(manager.getJwt(), command);

            assertNotNull(response);
            assertEquals(parentFolderId, response.getParentFolderId());
        }

        @Test
        void should_return_existing_session_when_creating_duplicate() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse firstResponse = CollaborationApi.createSession(manager.getJwt(), command);
            SessionInfoResponse secondResponse = CollaborationApi.createSession(manager.getJwt(), command);

            assertEquals(firstResponse.getSessionId(), secondResponse.getSessionId());
        }
    }

    @Nested
    @DisplayName("Session retrieval tests")
    class SessionRetrievalTests {

        @Test
        void should_get_session_info() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            SessionInfoResponse getResponse = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);

            assertEquals(createResponse.getSessionId(), getResponse.getSessionId());
            assertEquals(createResponse.getDocumentId(), getResponse.getDocumentId());
        }

        @Test
        void should_get_session_by_document_id() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);

            SessionInfoResponse getResponse = CollaborationApi.getSessionByDocument(manager.getJwt(), documentId);

            assertEquals(createResponse.getSessionId(), getResponse.getSessionId());
            assertEquals(documentId, getResponse.getDocumentId());
        }

        @Test
        void should_fail_to_get_nonexistent_session() {
            LoginResponse manager = setupApi.registerWithLogin();
            String fakeSessionId = CollaborationSession.newSessionId();

            assertError(() -> CollaborationApi.getSessionInfoRaw(manager.getJwt(), fakeSessionId),
                    COLLAB_SESSION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Session join tests")
    class SessionJoinTests {

        @Test
        void should_join_session() {
            LoginResponse user1 = setupApi.registerWithLogin();
            LoginResponse user2 = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(user1.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            SessionInfoResponse joinResponse = CollaborationApi.joinSession(user2.getJwt(), sessionId);

            assertEquals(2, joinResponse.getActiveUserCount());
        }

        @Test
        void should_allow_same_user_to_join_twice() {
            LoginResponse user1 = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(user1.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            SessionInfoResponse firstJoin = CollaborationApi.joinSession(user1.getJwt(), sessionId);
            SessionInfoResponse secondJoin = CollaborationApi.joinSession(user1.getJwt(), sessionId);

            assertEquals(1, firstJoin.getActiveUserCount());
            assertEquals(1, secondJoin.getActiveUserCount());
        }

        @Test
        void should_leave_session() {
            LoginResponse user1 = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(user1.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.leaveSession(user1.getJwt(), sessionId);
        }

        @Test
        void should_join_and_leave_multiple_users() {
            LoginResponse user1 = setupApi.registerWithLogin();
            LoginResponse user2 = setupApi.registerWithLogin();
            LoginResponse user3 = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(user1.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.joinSession(user2.getJwt(), sessionId);
            CollaborationApi.joinSession(user3.getJwt(), sessionId);

            SessionInfoResponse beforeLeave = CollaborationApi.getSessionInfo(user1.getJwt(), sessionId);
            assertEquals(3, beforeLeave.getActiveUserCount());

            CollaborationApi.leaveSession(user2.getJwt(), sessionId);

            SessionInfoResponse afterLeave = CollaborationApi.getSessionInfo(user1.getJwt(), sessionId);
            assertEquals(2, afterLeave.getActiveUserCount());
        }
    }

    @Nested
    @DisplayName("Session deletion tests")
    class SessionDeletionTests {

        @Test
        void should_delete_session() {
            LoginResponse manager = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.deleteSession(manager.getJwt(), sessionId);

            assertError(() -> CollaborationApi.getSessionInfoRaw(manager.getJwt(), sessionId),
                    COLLAB_SESSION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Operation tests")
    class OperationTests {

        @Test
        void should_submit_operation() {
            LoginResponse manager = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            SubmitOperationCommand opCommand = SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build();

            SessionInfoResponse response = CollaborationApi.submitOperation(manager.getJwt(), opCommand);

            assertEquals(1, response.getVersion());
        }

        @Test
        void should_update_cursor() {
            LoginResponse manager = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            UpdateCursorCommand cursorCommand = UpdateCursorCommand.builder()
                    .sessionId(sessionId)
                    .position(10)
                    .selectionStart(10)
                    .selectionEnd(15)
                    .build();

            SessionInfoResponse response = CollaborationApi.updateCursor(manager.getJwt(), cursorCommand);

            assertNotNull(response.getCursors());
        }

        @Test
        void should_get_operation_history() {
            LoginResponse manager = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            SubmitOperationCommand opCommand = SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build();
            CollaborationApi.submitOperation(manager.getJwt(), opCommand);

            OperationHistoryResponse historyResponse = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);

            assertNotNull(historyResponse);
            assertEquals(sessionId, historyResponse.getSessionId());
            assertEquals(1, historyResponse.getOperations().size());
        }

        @Test
        void should_submit_multiple_operations() {
            LoginResponse manager = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(5)
                    .content("World")
                    .clientVersion(1L)
                    .build());

            OperationHistoryResponse historyResponse = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);

            assertEquals(2, historyResponse.getOperations().size());
            assertEquals(2, historyResponse.getToVersion());
            assertEquals(5, historyResponse.getOperations().get(1).getPosition());
        }

        @Test
        void should_transform_only_against_concurrent_operations() {
            LoginResponse manager = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(5)
                    .content("!")
                    .clientVersion(1L)
                    .build());

            OperationHistoryResponse historyResponse = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);

            assertEquals(2, historyResponse.getOperations().size());
            assertEquals(0, historyResponse.getOperations().get(0).getPosition());
            assertEquals(5, historyResponse.getOperations().get(1).getPosition());
            assertEquals("Hello!", replay(historyResponse));
        }

        @Test
        void should_transform_concurrent_inserts_from_same_base_version() {
            LoginResponse user1 = setupApi.registerWithLogin();
            LoginResponse user2 = setupApi.registerWithLogin();

            String documentId = rDocumentId();
            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Concurrent Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(user1.getJwt(), command);
            String sessionId = createResponse.getSessionId();
            CollaborationApi.joinSession(user2.getJwt(), sessionId);

            CollaborationApi.submitOperation(user1.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("A")
                    .clientVersion(0L)
                    .build());

            CollaborationApi.submitOperation(user2.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("B")
                    .clientVersion(0L)
                    .build());

            OperationHistoryResponse historyResponse = CollaborationApi.getOperationHistory(user1.getJwt(), sessionId, 0);

            assertEquals(2, historyResponse.getOperations().size());
            assertEquals(0, historyResponse.getOperations().get(0).getPosition());
            assertEquals(1, historyResponse.getOperations().get(1).getPosition());
            assertEquals(expectedConcurrentOrder(user1.getUserId(), user2.getUserId()), replay(historyResponse));
        }
    }

    @Nested
    @DisplayName("Operation history tests")
    class OperationHistoryTests {

        @Test
        void should_reconstruct_document_from_operations() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(5)
                    .content("World")
                    .clientVersion(1L)
                    .build());

            OperationHistoryResponse history = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);

            StringBuilder document = new StringBuilder();
            for (TextOperation op : history.getOperations()) {
                if (op.isInsert() && op.getContent() != null) {
                    int pos = Math.min(op.getPosition(), document.length());
                    document.insert(pos, op.getContent());
                } else if (op.isDelete()) {
                    int start = Math.min(op.getPosition(), document.length());
                    int end = Math.min(start + op.getLength(), document.length());
                    document.delete(start, end);
                }
            }

            assertEquals("HelloWorld", document.toString());
        }

        @Test
        void should_maintain_correct_document_length_after_duplicate_session() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse firstResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = firstResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("aaaa")
                    .clientVersion(0L)
                    .build());

            SessionInfoResponse afterEdit = CollaborationApi.getSessionByDocument(manager.getJwt(), documentId);
            assertEquals(4, afterEdit.getDocumentLength());

            SessionInfoResponse secondResponse = CollaborationApi.getSessionByDocument(manager.getJwt(), documentId);
            assertEquals(firstResponse.getSessionId(), secondResponse.getSessionId());
            assertEquals(afterEdit.getDocumentLength(), secondResponse.getDocumentLength());
        }
    }

    @Nested
    @DisplayName("Base version tests")
    class BaseVersionTests {

        @Test
        void should_return_default_base_version_on_session_creation() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse response = CollaborationApi.createSession(manager.getJwt(), command);

            assertNotNull(response);
            assertEquals(0L, response.getBaseVersion());
        }

        @Test
        void should_update_base_version() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(5)
                    .content(" World")
                    .clientVersion(1L)
                    .build());

            SessionInfoResponse afterOps = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(2L, afterOps.getVersion());

            SessionInfoResponse updateResponse = CollaborationApi.updateBaseVersion(manager.getJwt(), sessionId, 2L);

            assertEquals(2L, updateResponse.getBaseVersion());
            assertEquals(2L, updateResponse.getVersion());
        }

        @Test
        void should_only_allow_increasing_base_version() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("Hello")
                    .clientVersion(0L)
                    .build());

            SessionInfoResponse afterOps = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(1L, afterOps.getVersion());

            CollaborationApi.updateBaseVersion(manager.getJwt(), sessionId, 1L);
            SessionInfoResponse afterFirstUpdate = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(1L, afterFirstUpdate.getBaseVersion());

            CollaborationApi.updateBaseVersion(manager.getJwt(), sessionId, 0L);
            SessionInfoResponse afterSecondUpdate = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(1L, afterSecondUpdate.getBaseVersion());
        }

        @Test
        void should_filter_operations_by_base_version() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(0)
                    .content("First")
                    .clientVersion(0L)
                    .build());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(5)
                    .content(" Second")
                    .clientVersion(1L)
                    .build());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(12)
                    .content(" Third")
                    .clientVersion(2L)
                    .build());

            CollaborationApi.updateBaseVersion(manager.getJwt(), sessionId, 2L);

            OperationHistoryResponse historyFrom0 = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);
            assertEquals(3, historyFrom0.getOperations().size());

            OperationHistoryResponse historyFrom1 = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 1);
            assertEquals(2, historyFrom1.getOperations().size());

            OperationHistoryResponse historyFrom2 = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 2);
            assertEquals(1, historyFrom2.getOperations().size());
        }

        @Test
        void should_update_base_version_after_save_scenario() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            for (int i = 0; i < 10; i++) {
                CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                        .sessionId(sessionId)
                        .type(TextOperationType.INSERT)
                        .position(i)
                        .content(String.valueOf(i))
                        .clientVersion((long) i)
                        .build());
            }

            SessionInfoResponse afterEdits = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(10L, afterEdits.getVersion());
            assertEquals(0L, afterEdits.getBaseVersion());

            CollaborationApi.updateBaseVersion(manager.getJwt(), sessionId, 10L);

            SessionInfoResponse afterSave = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(10L, afterSave.getBaseVersion());
            assertEquals(10L, afterSave.getVersion());

            CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                    .sessionId(sessionId)
                    .type(TextOperationType.INSERT)
                    .position(10)
                    .content("NEW")
                    .clientVersion(10L)
                    .build());

            SessionInfoResponse afterNewEdit = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(11L, afterNewEdit.getVersion());
            assertEquals(10L, afterNewEdit.getBaseVersion());

            OperationHistoryResponse history = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 10);
            assertEquals(1, history.getOperations().size());
            assertEquals("NEW", history.getOperations().get(0).getContent());
        }

        @Test
        void should_handle_concurrent_operations_without_conflict() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            CreateSessionCommand command = CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Concurrent Test Document")
                    .build();

            SessionInfoResponse createResponse = CollaborationApi.createSession(manager.getJwt(), command);
            String sessionId = createResponse.getSessionId();

            for (int i = 0; i < 5; i++) {
                CollaborationApi.submitOperation(manager.getJwt(), SubmitOperationCommand.builder()
                        .sessionId(sessionId)
                        .type(TextOperationType.INSERT)
                        .position(i)
                        .content(String.valueOf(i))
                        .clientVersion((long) i)
                        .build());
            }

            SessionInfoResponse afterOps = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(5L, afterOps.getVersion());
            assertEquals(0L, afterOps.getBaseVersion());

            CollaborationApi.updateBaseVersion(manager.getJwt(), sessionId, 5L);

            SessionInfoResponse afterBaseVersionUpdate = CollaborationApi.getSessionInfo(manager.getJwt(), sessionId);
            assertEquals(5L, afterBaseVersionUpdate.getBaseVersion());
            assertEquals(5L, afterBaseVersionUpdate.getVersion());

            OperationHistoryResponse historyFrom0 = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);
            OperationHistoryResponse historyFrom5 = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 5);

            assertEquals(5, historyFrom0.getOperations().size());
            assertEquals(0, historyFrom5.getOperations().size());
        }

        @Test
        void should_maintain_separate_sessions_for_different_files() {
            LoginResponse user1 = setupApi.registerWithLogin();
            LoginResponse user2 = setupApi.registerWithLogin();

            String documentId1 = rDocumentId();
            String documentId2 = rDocumentId();

            CreateSessionCommand command1 = CreateSessionCommand.builder()
                    .documentId(documentId1)
                    .documentTitle("File 1")
                    .build();
            CreateSessionCommand command2 = CreateSessionCommand.builder()
                    .documentId(documentId2)
                    .documentTitle("File 2")
                    .build();

            SessionInfoResponse session1 = CollaborationApi.createSession(user1.getJwt(), command1);
            SessionInfoResponse session2 = CollaborationApi.createSession(user2.getJwt(), command2);

            assertNotEquals(session1.getSessionId(), session2.getSessionId());

            for (int i = 0; i < 3; i++) {
                CollaborationApi.submitOperation(user1.getJwt(), SubmitOperationCommand.builder()
                        .sessionId(session1.getSessionId())
                        .type(TextOperationType.INSERT)
                        .position(i)
                        .content("A")
                        .clientVersion((long) i)
                        .build());

                CollaborationApi.submitOperation(user2.getJwt(), SubmitOperationCommand.builder()
                        .sessionId(session2.getSessionId())
                        .type(TextOperationType.INSERT)
                        .position(i)
                        .content("B")
                        .clientVersion((long) i)
                        .build());
            }

            SessionInfoResponse s1After = CollaborationApi.getSessionInfo(user1.getJwt(), session1.getSessionId());
            SessionInfoResponse s2After = CollaborationApi.getSessionInfo(user2.getJwt(), session2.getSessionId());

            assertEquals(3L, s1After.getVersion());
            assertEquals(3L, s2After.getVersion());

            CollaborationApi.updateBaseVersion(user1.getJwt(), session1.getSessionId(), 3L);
            CollaborationApi.updateBaseVersion(user2.getJwt(), session2.getSessionId(), 3L);

            SessionInfoResponse s1Final = CollaborationApi.getSessionInfo(user1.getJwt(), session1.getSessionId());
            SessionInfoResponse s2Final = CollaborationApi.getSessionInfo(user2.getJwt(), session2.getSessionId());

            assertEquals(3L, s1Final.getBaseVersion());
            assertEquals(3L, s2Final.getBaseVersion());
            assertEquals(3L, s1Final.getVersion());
            assertEquals(3L, s2Final.getVersion());
        }
    }

    @Nested
    @DisplayName("Revision tests")
    class RevisionTests {

        @Test
        void should_create_and_list_document_revisions() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(manager.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Revision Doc")
                    .build());

            RevisionDetailResponse first = CollaborationApi.createRevision(manager.getJwt(), documentId, CreateRevisionCommand.builder()
                    .sessionId(session.getSessionId())
                    .documentId(documentId)
                    .documentTitle("Revision Doc")
                    .baseVersion(1L)
                    .content("Hello")
                    .source("MANUAL_SAVE")
                    .changeSummary("Initial save")
                    .build());

            RevisionDetailResponse second = CollaborationApi.createRevision(manager.getJwt(), documentId, CreateRevisionCommand.builder()
                    .sessionId(session.getSessionId())
                    .documentId(documentId)
                    .documentTitle("Revision Doc")
                    .baseVersion(2L)
                    .content("Hello World")
                    .source("MANUAL_SAVE")
                    .changeSummary("Append world")
                    .build());

            assertEquals(1L, first.getRevisionNo());
            assertEquals(2L, second.getRevisionNo());

            java.util.List<RevisionSummaryResponse> revisions = CollaborationApi.listRevisions(manager.getJwt(), documentId);
            assertEquals(2, revisions.size());
            assertEquals(second.getRevisionId(), revisions.get(0).getRevisionId());
            assertEquals(first.getRevisionId(), revisions.get(1).getRevisionId());
        }

        @Test
        void should_return_revision_detail_and_diff() {
            LoginResponse manager = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(manager.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Revision Doc")
                    .build());

            RevisionDetailResponse first = CollaborationApi.createRevision(manager.getJwt(), documentId, CreateRevisionCommand.builder()
                    .sessionId(session.getSessionId())
                    .documentId(documentId)
                    .documentTitle("Revision Doc")
                    .baseVersion(1L)
                    .content("alpha")
                    .source("MANUAL_SAVE")
                    .build());

            RevisionDetailResponse second = CollaborationApi.createRevision(manager.getJwt(), documentId, CreateRevisionCommand.builder()
                    .sessionId(session.getSessionId())
                    .documentId(documentId)
                    .documentTitle("Revision Doc")
                    .baseVersion(2L)
                    .content("alpha\nbeta")
                    .source("MANUAL_SAVE")
                    .build());

            RevisionDetailResponse detail = CollaborationApi.getRevision(manager.getJwt(), documentId, second.getRevisionId());
            RevisionDiffResponse diff = CollaborationApi.getRevisionDiff(manager.getJwt(), documentId, second.getRevisionId(), first.getRevisionId());

            assertEquals("alpha\nbeta", detail.getContentSnapshot());
            assertEquals(first.getRevisionId(), diff.getCompareToRevisionId());
            assertTrue(diff.getUnifiedDiffLines().stream().anyMatch(line -> line.contains("+beta")));
        }
    }

    @Nested
    @DisplayName("Editing lock tests")
    class EditingLockTests {

        @Test
        void should_acquire_and_release_lock() {
            LoginResponse user = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(user.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Lock Doc")
                    .build());

            EditingLockResponse lock = CollaborationApi.acquireLock(user.getJwt(), session.getSessionId(), AcquireEditingLockCommand.builder()
                    .documentId(documentId)
                    .start(2)
                    .end(5)
                    .build());

            EditingLockStateResponse state = CollaborationApi.listLocks(user.getJwt(), session.getSessionId());
            assertEquals(1, state.getLocks().size());
            assertEquals(lock.getLockId(), state.getLocks().get(0).getLockId());

            CollaborationApi.releaseLock(user.getJwt(), session.getSessionId(), lock.getLockId());
            EditingLockStateResponse afterRelease = CollaborationApi.listLocks(user.getJwt(), session.getSessionId());
            assertEquals(0, afterRelease.getLocks().size());
        }

        @Test
        void should_reject_conflicting_lock_and_block_operations() {
            LoginResponse user1 = setupApi.registerWithLogin();
            LoginResponse user2 = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(user1.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Lock Doc")
                    .build());
            CollaborationApi.joinSession(user2.getJwt(), session.getSessionId());

            CollaborationApi.acquireLock(user1.getJwt(), session.getSessionId(), AcquireEditingLockCommand.builder()
                    .documentId(documentId)
                    .start(0)
                    .end(5)
                    .build());

            CollaborationApi.acquireLockRaw(user2.getJwt(), session.getSessionId(), AcquireEditingLockCommand.builder()
                            .documentId(documentId)
                            .start(3)
                            .end(6)
                            .build())
                    .then()
                    .statusCode(400);

            CollaborationApi.submitOperationRaw(user2.getJwt(), SubmitOperationCommand.builder()
                            .sessionId(session.getSessionId())
                            .type(TextOperationType.INSERT)
                            .position(3)
                            .content("X")
                            .clientVersion(0L)
                            .build())
                    .then()
                    .statusCode(400);
        }

        @Test
        void should_accept_lock_request_when_session_id_only_comes_from_path() {
            LoginResponse user = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(user.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Lock Doc")
                    .build());

            EditingLockResponse lock = CollaborationApi.acquireLock(user.getJwt(), session.getSessionId(), AcquireEditingLockCommand.builder()
                    .documentId(documentId)
                    .start(1)
                    .end(1)
                    .build());

            assertNotNull(lock.getLockId());
            assertEquals(1, lock.getStart());
            assertEquals(1, lock.getEnd());
        }

        @Test
        void should_allow_releasing_same_lock_multiple_times() {
            LoginResponse user = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(user.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Lock Doc")
                    .build());

            EditingLockResponse lock = CollaborationApi.acquireLock(user.getJwt(), session.getSessionId(), AcquireEditingLockCommand.builder()
                    .documentId(documentId)
                    .start(2)
                    .end(2)
                    .build());

            CollaborationApi.releaseLock(user.getJwt(), session.getSessionId(), lock.getLockId());
            CollaborationApi.releaseLock(user.getJwt(), session.getSessionId(), lock.getLockId());

            EditingLockStateResponse state = CollaborationApi.listLocks(user.getJwt(), session.getSessionId());
            assertEquals(0, state.getLocks().size());
        }

        @Test
        void should_release_user_locks_when_user_leaves_session() {
            LoginResponse user = setupApi.registerWithLogin();
            String documentId = rDocumentId();

            SessionInfoResponse session = CollaborationApi.createSession(user.getJwt(), CreateSessionCommand.builder()
                    .documentId(documentId)
                    .documentTitle("Lock Doc")
                    .build());

            CollaborationApi.acquireLock(user.getJwt(), session.getSessionId(), AcquireEditingLockCommand.builder()
                    .documentId(documentId)
                    .start(2)
                    .end(4)
                    .build());

            CollaborationApi.leaveSession(user.getJwt(), session.getSessionId());

            EditingLockStateResponse state = CollaborationApi.listLocks(user.getJwt(), session.getSessionId());
            assertEquals(0, state.getLocks().size());
        }
    }

    private String replay(OperationHistoryResponse historyResponse) {
        StringBuilder document = new StringBuilder();
        for (TextOperation op : historyResponse.getOperations()) {
            if (op.isInsert() && op.getContent() != null) {
                document.insert(op.getPosition(), op.getContent());
            } else if (op.isDelete() && op.getLength() > 0) {
                document.delete(op.getPosition(), op.getPosition() + op.getLength());
            }
        }
        return document.toString();
    }

    private String expectedConcurrentOrder(String user1Id, String user2Id) {
        return user1Id.compareTo(user2Id) < 0 ? "AB" : "BA";
    }
}
