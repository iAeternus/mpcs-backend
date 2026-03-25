package com.ricky.lock.service;

import com.ricky.collaboration.domain.ot.TextOperation;
import com.ricky.lock.command.AcquireEditingLockCommand;
import com.ricky.lock.command.RenewEditingLockCommand;
import com.ricky.lock.query.EditingLockResponse;
import com.ricky.lock.query.EditingLockStateResponse;
import com.ricky.common.domain.user.UserContext;

public interface EditingLockService {

    EditingLockStateResponse listLocks(String sessionId, UserContext userContext);

    EditingLockResponse acquireLock(AcquireEditingLockCommand command, UserContext userContext);

    void releaseLock(String sessionId, String lockId, UserContext userContext);

    void releaseUserLocks(String sessionId, String userId, UserContext userContext);

    EditingLockResponse renewLock(RenewEditingLockCommand command, UserContext userContext);

    void validateOperationAllowed(String sessionId, TextOperation operation, String userId);

    void rebaseLocks(String sessionId, String documentId, TextOperation operation, String ownerUserId, UserContext userContext);
}
