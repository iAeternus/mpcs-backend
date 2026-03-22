package com.ricky.collaboration.collaboration.controller;

import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import com.ricky.collaboration.collaboration.service.CollaborationService;
import com.ricky.common.domain.user.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@Tag(name = "协同编辑模块")
@RequiredArgsConstructor
@RequestMapping("/collaboration")
public class CollaborationController {
    
    private final CollaborationService collaborationService;
    
    @PostMapping("/sessions")
    @Operation(summary = "创建协同会话")
    public SessionInfoResponse createSession(
            @RequestBody @Valid CreateSessionCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("Creating collaboration session for document[{}]", command.getDocumentId());
        return collaborationService.createSession(command, userContext);
    }
    
    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "获取会话信息")
    public SessionInfoResponse getSessionInfo(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.getSessionInfo(sessionId, userContext);
    }
    
    @GetMapping("/sessions/document/{documentId}")
    @Operation(summary = "通过文档ID获取会话")
    public SessionInfoResponse getSessionByDocument(
            @PathVariable String documentId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.getSessionInfo(documentId, userContext);
    }
    
    @PostMapping("/sessions/{sessionId}/join")
    @Operation(summary = "加入协同会话")
    public SessionInfoResponse joinSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("User[{}] joining session[{}]", userContext.getUid(), sessionId);
        return collaborationService.joinSession(sessionId, userContext);
    }
    
    @PostMapping("/sessions/{sessionId}/leave")
    @Operation(summary = "离开协同会话")
    public void leaveSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("User[{}] leaving session[{}]", userContext.getUid(), sessionId);
        collaborationService.leaveSession(sessionId, userContext);
    }
    
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除协同会话")
    public void deleteSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        log.info("Deleting collaboration session[{}]", sessionId);
        collaborationService.deleteSession(sessionId, userContext);
    }
    
    @PostMapping("/operations")
    @Operation(summary = "提交操作")
    public SessionInfoResponse submitOperation(
            @RequestBody @Valid SubmitOperationCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.submitOperation(command, userContext);
    }
    
    @PostMapping("/cursors")
    @Operation(summary = "更新光标位置")
    public SessionInfoResponse updateCursor(
            @RequestBody @Valid UpdateCursorCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.updateCursor(command, userContext);
    }
    
    @GetMapping("/sessions/{sessionId}/history")
    @Operation(summary = "获取操作历史")
    public OperationHistoryResponse getOperationHistory(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "0") long fromVersion,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return collaborationService.getOperationHistory(sessionId, fromVersion, userContext);
    }
}
