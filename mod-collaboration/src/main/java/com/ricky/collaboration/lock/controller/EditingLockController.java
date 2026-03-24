package com.ricky.collaboration.lock.controller;

import com.ricky.collaboration.collaboration.infra.CollaborationSessionManager;
import com.ricky.collaboration.lock.command.AcquireEditingLockCommand;
import com.ricky.collaboration.lock.command.RenewEditingLockCommand;
import com.ricky.collaboration.lock.dto.EditingLockStateMessage;
import com.ricky.collaboration.lock.query.EditingLockResponse;
import com.ricky.collaboration.lock.query.EditingLockStateResponse;
import com.ricky.collaboration.lock.service.EditingLockService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.COLLAB_SESSION_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "编辑锁")
@RequestMapping("/collaboration/sessions/{sessionId}/locks")
public class EditingLockController {

    private final EditingLockService editingLockService;
    private final CollaborationSessionManager sessionManager;

    @GetMapping
    @Operation(summary = "查询当前编辑锁")
    public EditingLockStateResponse listLocks(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return editingLockService.listLocks(sessionId, userContext);
    }

    @PostMapping
    @Operation(summary = "申请编辑锁")
    public EditingLockResponse acquireLock(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @RequestBody @Valid AcquireEditingLockCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        command.setSessionId(sessionId);
        EditingLockResponse response = editingLockService.acquireLock(command, userContext);
        broadcastLockState(sessionId, userContext.getUid(), userContext);
        return response;
    }

    @PostMapping("/{lockId}/renew")
    @Operation(summary = "续租编辑锁")
    public EditingLockResponse renewLock(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @PathVariable String lockId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        EditingLockResponse response = editingLockService.renewLock(
                RenewEditingLockCommand.builder().sessionId(sessionId).lockId(lockId).build(),
                userContext
        );
        broadcastLockState(sessionId, userContext.getUid(), userContext);
        return response;
    }

    @DeleteMapping("/{lockId}")
    @Operation(summary = "释放编辑锁")
    public void releaseLock(
            @PathVariable @Id(COLLAB_SESSION_ID_PREFIX) String sessionId,
            @PathVariable String lockId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        editingLockService.releaseLock(sessionId, lockId, userContext);
        broadcastLockState(sessionId, userContext.getUid(), userContext);
    }

    private void broadcastLockState(String sessionId, String excludeUserId, UserContext userContext) {
        EditingLockStateResponse state = editingLockService.listLocks(sessionId, userContext);
        sessionManager.broadcast(sessionId, EditingLockStateMessage.of(sessionId, state.getLocks()), excludeUserId);
    }
}
