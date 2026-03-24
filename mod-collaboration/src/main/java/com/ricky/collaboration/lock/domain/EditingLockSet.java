package com.ricky.collaboration.lock.domain;

import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.ricky.common.constants.ConfigConstants.EDITING_LOCK_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.EDITING_LOCK_ID_PREFIX;
import static lombok.AccessLevel.PROTECTED;

@Getter
@FieldNameConstants
@Document(EDITING_LOCK_COLLECTION)
@TypeAlias("editing_lock_set")
@NoArgsConstructor(access = PROTECTED)
public class EditingLockSet extends AggregateRoot {

    private static final long DEFAULT_LOCK_TTL_SECONDS = 15;

    private String sessionId;
    private String documentId;
    private List<EditingLock> locks;

    private EditingLockSet(String sessionId, String documentId, UserContext userContext) {
        super(newLockSetId(), userContext);
        this.sessionId = sessionId;
        this.documentId = documentId;
        this.locks = new ArrayList<>();
        addOpsLog("创建编辑锁集合", userContext);
    }

    public static EditingLockSet create(String sessionId, String documentId, UserContext userContext) {
        return new EditingLockSet(sessionId, documentId, userContext);
    }

    public static String newLockSetId() {
        return EDITING_LOCK_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public List<EditingLock> activeLocks(Instant now) {
        clearExpired(now);
        return List.copyOf(locks);
    }

    public EditingLock acquire(String userId, String username, int start, int end, UserContext userContext) {
        Instant now = Instant.now();
        clearExpired(now);
        locks.removeIf(lock -> lock.getUserId().equals(userId));

        EditingLock requested = EditingLock.create(
                userId,
                username,
                start,
                end,
                now,
                now.plusSeconds(DEFAULT_LOCK_TTL_SECONDS)
        );

        for (EditingLock lock : locks) {
            if (!lock.getUserId().equals(userId) && lock.conflictsWith(requested)) {
                throw MyException.requestValidationException("Editing range is locked by " + lock.getUsername());
            }
        }

        locks.add(requested);
        addOpsLog("用户[" + username + "]获得编辑锁[" + requested.getStart() + "," + requested.getEnd() + "]", userContext);
        return requested;
    }

    public void release(String userId, String lockId, UserContext userContext) {
        boolean removed = locks.removeIf(lock -> lock.getUserId().equals(userId) && lock.getLockId().equals(lockId));
        if (removed) {
            addOpsLog("用户[" + userContext.getUsername() + "]释放编辑锁[" + lockId + "]", userContext);
        }
    }

    public Optional<EditingLock> renew(String userId, String lockId, UserContext userContext) {
        Instant now = Instant.now();
        clearExpired(now);
        for (int i = 0; i < locks.size(); i++) {
            EditingLock lock = locks.get(i);
            if (lock.getUserId().equals(userId) && lock.getLockId().equals(lockId)) {
                EditingLock renewed = lock.renew(now, now.plusSeconds(DEFAULT_LOCK_TTL_SECONDS));
                locks.set(i, renewed);
                addOpsLog("用户[" + userContext.getUsername() + "]续租编辑锁[" + lockId + "]", userContext);
                return Optional.of(renewed);
            }
        }
        return Optional.empty();
    }

    public void validateOperationAllowed(TextOperation operation, String userId) {
        clearExpired(Instant.now());
        for (EditingLock lock : locks) {
            if (lock.getUserId().equals(userId)) {
                continue;
            }
            if (conflicts(lock, operation)) {
                throw MyException.requestValidationException("Editing range is locked by " + lock.getUsername());
            }
        }
    }

    public void applyOperation(TextOperation operation, String ownerUserId, UserContext userContext) {
        clearExpired(Instant.now());
        for (int i = 0; i < locks.size(); i++) {
            EditingLock lock = locks.get(i);
            if (lock.getUserId().equals(ownerUserId)) {
                continue;
            }
            locks.set(i, transform(lock, operation));
        }
        addOpsLog("同步编辑锁位置到版本操作[" + operation.getType() + "@" + operation.getPosition() + "]", userContext);
    }

    private void clearExpired(Instant now) {
        Iterator<EditingLock> iterator = locks.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired(now)) {
                iterator.remove();
            }
        }
    }

    private boolean conflicts(EditingLock lock, TextOperation operation) {
        int lockStart = lock.getStart();
        int lockEnd = lock.getEnd();

        if (operation.isInsert()) {
            int pos = operation.getPosition();
            return lockStart <= pos && pos <= lockEnd;
        }

        if (operation.isDelete()) {
            int opStart = operation.getPosition();
            int opEnd = operation.getPosition() + operation.getLength();
            if (lockStart == lockEnd) {
                return opStart <= lockStart && lockStart <= opEnd;
            }
            return opStart < lockEnd && lockStart < opEnd;
        }

        return false;
    }

    private EditingLock transform(EditingLock lock, TextOperation operation) {
        int start = lock.getStart();
        int end = lock.getEnd();

        if (operation.isInsert()) {
            int len = operation.getLength();
            int pos = operation.getPosition();
            if (pos <= start) {
                return lock.withRange(start + len, end + len);
            }
            if (pos < end) {
                return lock.withRange(start, end + len);
            }
            return lock;
        }

        if (operation.isDelete()) {
            int pos = operation.getPosition();
            int deleteEnd = pos + operation.getLength();
            return lock.withRange(transformIndexForDelete(start, pos, deleteEnd), transformIndexForDelete(end, pos, deleteEnd));
        }

        return lock;
    }

    private int transformIndexForDelete(int index, int deleteStart, int deleteEnd) {
        if (index <= deleteStart) {
            return index;
        }
        if (index >= deleteEnd) {
            return index - (deleteEnd - deleteStart);
        }
        return deleteStart;
    }
}
