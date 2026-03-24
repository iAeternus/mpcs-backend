package com.ricky.apitest.collaboration;

import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import com.ricky.collaboration.lock.command.AcquireEditingLockCommand;
import com.ricky.collaboration.lock.query.EditingLockResponse;
import com.ricky.collaboration.lock.query.EditingLockStateResponse;
import com.ricky.collaboration.revision.command.CreateRevisionCommand;
import com.ricky.collaboration.revision.query.RevisionDetailResponse;
import com.ricky.collaboration.revision.query.RevisionDiffResponse;
import com.ricky.collaboration.revision.query.RevisionSummaryResponse;
import io.restassured.response.Response;

import java.util.List;

import static com.ricky.apitest.BaseApiTest.givenBearer;

public class CollaborationApi {

    private static final String COLLAB = "/collaboration";

    public static SessionInfoResponse createSession(String jwt, CreateSessionCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/sessions")
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static Response createSessionRaw(String jwt, CreateSessionCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/sessions");
    }

    public static SessionInfoResponse getSessionInfo(String jwt, String sessionId) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/sessions/{sessionId}", sessionId)
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static SessionInfoResponse getSessionByDocument(String jwt, String documentId) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/sessions/document/{documentId}", documentId)
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static Response getSessionInfoRaw(String jwt, String sessionId) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/sessions/{sessionId}", sessionId);
    }

    public static SessionInfoResponse joinSession(String jwt, String sessionId) {
        return givenBearer(jwt)
                .when()
                .post(COLLAB + "/sessions/{sessionId}/join", sessionId)
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static Response joinSessionRaw(String jwt, String sessionId) {
        return givenBearer(jwt)
                .when()
                .post(COLLAB + "/sessions/{sessionId}/join", sessionId);
    }

    public static void leaveSession(String jwt, String sessionId) {
        givenBearer(jwt)
                .when()
                .post(COLLAB + "/sessions/{sessionId}/leave", sessionId)
                .then()
                .statusCode(200);
    }

    public static void deleteSession(String jwt, String sessionId) {
        givenBearer(jwt)
                .when()
                .delete(COLLAB + "/sessions/{sessionId}", sessionId)
                .then()
                .statusCode(200);
    }

    public static Response deleteSessionRaw(String jwt, String sessionId) {
        return givenBearer(jwt)
                .when()
                .delete(COLLAB + "/sessions/{sessionId}", sessionId);
    }

    public static SessionInfoResponse submitOperation(String jwt, SubmitOperationCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/operations")
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static Response submitOperationRaw(String jwt, SubmitOperationCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/operations");
    }

    public static SessionInfoResponse updateCursor(String jwt, UpdateCursorCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/cursors")
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static OperationHistoryResponse getOperationHistory(String jwt, String sessionId, long fromVersion) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/sessions/{sessionId}/history?fromVersion={version}", sessionId, fromVersion)
                .then()
                .statusCode(200)
                .extract()
                .as(OperationHistoryResponse.class);
    }

    public static SessionInfoResponse updateBaseVersion(String jwt, String sessionId, long baseVersion) {
        return givenBearer(jwt)
                .param("baseVersion", baseVersion)
                .when()
                .put(COLLAB + "/sessions/{sessionId}/base-version", sessionId)
                .then()
                .statusCode(200)
                .extract()
                .as(SessionInfoResponse.class);
    }

    public static RevisionDetailResponse createRevision(String jwt, String documentId, CreateRevisionCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/documents/{documentId}/revisions", documentId)
                .then()
                .statusCode(200)
                .extract()
                .as(RevisionDetailResponse.class);
    }

    public static List<RevisionSummaryResponse> listRevisions(String jwt, String documentId) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/documents/{documentId}/revisions", documentId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", RevisionSummaryResponse.class);
    }

    public static RevisionDetailResponse getRevision(String jwt, String documentId, String revisionId) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/documents/{documentId}/revisions/{revisionId}", documentId, revisionId)
                .then()
                .statusCode(200)
                .extract()
                .as(RevisionDetailResponse.class);
    }

    public static RevisionDiffResponse getRevisionDiff(String jwt, String documentId, String revisionId, String compareToRevisionId) {
        var request = givenBearer(jwt);
        if (compareToRevisionId != null) {
            request = request.param("compareToRevisionId", compareToRevisionId);
        }
        return request.when()
                .get(COLLAB + "/documents/{documentId}/revisions/{revisionId}/diff", documentId, revisionId)
                .then()
                .statusCode(200)
                .extract()
                .as(RevisionDiffResponse.class);
    }

    public static EditingLockResponse acquireLock(String jwt, String sessionId, AcquireEditingLockCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/sessions/{sessionId}/locks", sessionId)
                .then()
                .statusCode(200)
                .extract()
                .as(EditingLockResponse.class);
    }

    public static Response acquireLockRaw(String jwt, String sessionId, AcquireEditingLockCommand command) {
        return givenBearer(jwt)
                .body(command)
                .when()
                .post(COLLAB + "/sessions/{sessionId}/locks", sessionId);
    }

    public static EditingLockStateResponse listLocks(String jwt, String sessionId) {
        return givenBearer(jwt)
                .when()
                .get(COLLAB + "/sessions/{sessionId}/locks", sessionId)
                .then()
                .statusCode(200)
                .extract()
                .as(EditingLockStateResponse.class);
    }

    public static void releaseLock(String jwt, String sessionId, String lockId) {
        givenBearer(jwt)
                .when()
                .delete(COLLAB + "/sessions/{sessionId}/locks/{lockId}", sessionId, lockId)
                .then()
                .statusCode(200);
    }
}
