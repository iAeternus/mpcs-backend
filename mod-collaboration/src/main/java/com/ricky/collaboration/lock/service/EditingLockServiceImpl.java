package com.ricky.collaboration.lock.service;

import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.lock.command.AcquireEditingLockCommand;
import com.ricky.collaboration.lock.command.RenewEditingLockCommand;
import com.ricky.collaboration.lock.domain.EditingLock;
import com.ricky.collaboration.lock.domain.EditingLockDomainService;
import com.ricky.collaboration.lock.domain.EditingLockSet;
import com.ricky.collaboration.lock.domain.EditingLockSetRepository;
import com.ricky.collaboration.lock.query.EditingLockResponse;
import com.ricky.collaboration.lock.query.EditingLockStateResponse;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditingLockServiceImpl implements EditingLockService {

    private static final int MAX_WRITE_RETRIES = 3;

    private final EditingLockSetRepository repository;
    private final EditingLockDomainService domainService;

    @Override
    public EditingLockStateResponse listLocks(String sessionId, UserContext userContext) {
        EditingLockSet lockSet = repository.findBySessionId(sessionId)
                .orElse(null);
        return EditingLockStateResponse.builder()
                .sessionId(sessionId)
                .locks(lockSet == null ? java.util.List.of() : lockSet.activeLocks(java.time.Instant.now()).stream().map(EditingLockResponse::from).toList())
                .build();
    }

    @Override
    public EditingLockResponse acquireLock(AcquireEditingLockCommand command, UserContext userContext) {
        return withWriteRetry(() -> {
            EditingLockSet lockSet = domainService.getOrCreate(command.getSessionId(), command.getDocumentId(), userContext);
            EditingLock lock = lockSet.acquire(
                    userContext.getUid(),
                    userContext.getUsername(),
                    command.getStart(),
                    command.getEnd(),
                    userContext
            );
            repository.save(lockSet);
            return EditingLockResponse.from(lock);
        });
    }

    @Override
    public void releaseLock(String sessionId, String lockId, UserContext userContext) {
        withWriteRetry(() -> {
            repository.findBySessionId(sessionId).ifPresent(lockSet -> {
                lockSet.release(userContext.getUid(), lockId, userContext);
                repository.save(lockSet);
            });
            return null;
        });
    }

    @Override
    public EditingLockResponse renewLock(RenewEditingLockCommand command, UserContext userContext) {
        return withWriteRetry(() -> {
            EditingLockSet lockSet = repository.findBySessionId(command.getSessionId())
                    .orElseThrow(() -> MyException.requestValidationException("Lock set not found"));
            EditingLock renewed = lockSet.renew(userContext.getUid(), command.getLockId(), userContext)
                    .orElseThrow(() -> MyException.requestValidationException("Lock not found"));
            repository.save(lockSet);
            return EditingLockResponse.from(renewed);
        });
    }

    @Override
    public void validateOperationAllowed(String sessionId, TextOperation operation, String userId) {
        repository.findBySessionId(sessionId)
                .ifPresent(lockSet -> lockSet.validateOperationAllowed(operation, userId));
    }

    @Override
    public void rebaseLocks(String sessionId, String documentId, TextOperation operation, String ownerUserId, UserContext userContext) {
        withWriteRetry(() -> {
            EditingLockSet lockSet = repository.findBySessionId(sessionId)
                    .orElse(null);
            if (lockSet == null) {
                return null;
            }
            lockSet.applyOperation(operation, ownerUserId, userContext);
            repository.save(lockSet);
            return null;
        });
    }

    private <T> T withWriteRetry(java.util.concurrent.Callable<T> action) {
        int attempt = 0;
        while (true) {
            try {
                return action.call();
            } catch (DataIntegrityViolationException | OptimisticLockingFailureException ex) {
                attempt += 1;
                if (attempt >= MAX_WRITE_RETRIES) {
                    throw MyException.requestValidationException("编辑锁状态冲突，请重试。");
                }
            } catch (MyException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
