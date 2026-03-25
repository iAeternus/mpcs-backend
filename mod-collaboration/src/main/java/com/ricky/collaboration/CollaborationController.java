package com.ricky.collaboration;

import com.ricky.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.query.SessionInfoResponse;
import com.ricky.collaboration.service.CollaborationService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.ricky.common.constants.ConfigConstants.COLLAB_SESSION_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;

@Slf4j
@Validated
@RestController
@Tag(name = "collaboration")
@RequiredArgsConstructor
@RequestMapping("/collaboration")
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping("/sessions")
    @Operation(summary = "create collaboration session")
    public SessionInfoResponse createSession(
            @RequestBody @Valid CreateSessionCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("Creating collaboration session for document[{}]", command.getDocumentId());
        return collaborationService.createSession(command, userContext);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "get session info")
    public SessionInfoResponse getSessionInfo(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.getSessionInfo(sessionId, userContext);
    }

    @GetMapping("/sessions/document/{documentId}")
    @Operation(summary = "get session by document")
    public SessionInfoResponse getSessionByDocument(
            @PathVariable @Id(FILE_ID_PREFIX) String documentId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.getSessionByDocumentId(documentId, userContext);
    }

    @PostMapping("/sessions/{sessionId}/join")
    @Operation(summary = "join collaboration session")
    public SessionInfoResponse joinSession(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("User[{}] joining session[{}]", userContext.getUid(), sessionId);
        return collaborationService.joinSession(sessionId, userContext);
    }

    @PostMapping("/sessions/{sessionId}/leave")
    @Operation(summary = "leave collaboration session")
    public void leaveSession(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("User[{}] leaving session[{}]", userContext.getUid(), sessionId);
        collaborationService.leaveSession(sessionId, userContext);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "delete collaboration session")
    public void deleteSession(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("Deleting collaboration session[{}]", sessionId);
        collaborationService.deleteSession(sessionId, userContext);
    }

    @PostMapping("/operations")
    @Operation(summary = "submit operation")
    public SessionInfoResponse submitOperation(
            @RequestBody @Valid SubmitOperationCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.submitOperation(command, userContext);
    }

    @PostMapping("/cursors")
    @Operation(summary = "update cursor")
    public SessionInfoResponse updateCursor(
            @RequestBody @Valid UpdateCursorCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.updateCursor(command, userContext);
    }

    @GetMapping("/sessions/{sessionId}/history")
    @Operation(summary = "get operation history")
    public OperationHistoryResponse getOperationHistory(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @RequestParam(defaultValue = "0") long fromVersion,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.getOperationHistory(sessionId, fromVersion, userContext);
    }

    @PutMapping("/sessions/{sessionId}/base-version")
    @Operation(summary = "update base version")
    public SessionInfoResponse updateBaseVersion(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @RequestParam(defaultValue = "0") long baseVersion,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("Updating baseVersion to {} for session[{}]", baseVersion, sessionId);
        return collaborationService.updateBaseVersion(sessionId, baseVersion, userContext);
    }

    @GetMapping("/ws-test")
    @Operation(summary = "websocket test")
    public String wsTest() {
        return "WebSocket endpoint: /ws/collaboration/{sessionId}";
    }
}
