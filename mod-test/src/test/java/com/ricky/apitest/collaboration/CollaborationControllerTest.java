package com.ricky.apitest.collaboration;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.SetupApi;
import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.CollaborationSessionRepository;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import com.ricky.collaboration.collaboration.infra.CollaborationSessionManager;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.exception.ErrorCodeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.ricky.apitest.RandomTestFixture.rDocumentId;
import static com.ricky.common.exception.ErrorCodeEnum.*;
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
                    .content(" World")
                    .clientVersion(1L)
                    .build());

            OperationHistoryResponse historyResponse = CollaborationApi.getOperationHistory(manager.getJwt(), sessionId, 0);

            assertEquals(2, historyResponse.getOperations().size());
            assertEquals(2, historyResponse.getToVersion());
        }
    }
}
