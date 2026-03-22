package com.ricky.apitest.collaboration;

import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import com.ricky.common.domain.dto.resp.LoginResponse;
import io.restassured.response.Response;

import static com.ricky.apitest.BaseApiTest.givenBearer;
import static io.restassured.http.ContentType.JSON;

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
}
