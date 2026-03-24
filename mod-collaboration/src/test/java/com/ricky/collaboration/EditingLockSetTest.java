package com.ricky.collaboration;

import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.lock.domain.EditingLockSet;
import com.ricky.common.domain.user.Role;
import com.ricky.common.domain.user.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditingLockSetTest {

    private static final UserContext USER_1 = UserContext.of("USR1", "Alice", Role.NORMAL_USER);
    private static final UserContext USER_2 = UserContext.of("USR2", "Bob", Role.NORMAL_USER);

    @Test
    @DisplayName("should reject overlapping lock from another user")
    void shouldRejectOverlappingLock() {
        EditingLockSet lockSet = EditingLockSet.create("CLS1", "FIL1", USER_1);
        lockSet.acquire("USR1", "Alice", 2, 5, USER_1);

        assertThrows(RuntimeException.class, () -> lockSet.acquire("USR2", "Bob", 4, 6, USER_2));
    }

    @Test
    @DisplayName("should shift foreign locks after insert")
    void shouldShiftForeignLocksAfterInsert() {
        EditingLockSet lockSet = EditingLockSet.create("CLS1", "FIL1", USER_1);
        lockSet.acquire("USR2", "Bob", 5, 8, USER_2);

        lockSet.applyOperation(TextOperation.insert("USR1", 0, "abc", 0), "USR1", USER_1);

        assertEquals(8, lockSet.activeLocks(java.time.Instant.now()).get(0).getStart());
        assertEquals(11, lockSet.activeLocks(java.time.Instant.now()).get(0).getEnd());
    }

    @Test
    @DisplayName("should block delete that overlaps another user's lock")
    void shouldBlockDeleteOverlap() {
        EditingLockSet lockSet = EditingLockSet.create("CLS1", "FIL1", USER_1);
        lockSet.acquire("USR2", "Bob", 2, 5, USER_2);

        assertThrows(RuntimeException.class, () ->
                lockSet.validateOperationAllowed(TextOperation.delete("USR1", 3, 1, 0), "USR1"));
    }

    @Test
    @DisplayName("should release all locks owned by leaving user")
    void shouldReleaseAllLocksOwnedByLeavingUser() {
        EditingLockSet lockSet = EditingLockSet.create("CLS1", "FIL1", USER_1);
        lockSet.acquire("USR1", "Alice", 2, 5, USER_1);

        assertTrue(lockSet.releaseAllByUser("USR1", USER_1));
        assertEquals(0, lockSet.activeLocks(java.time.Instant.now()).size());
    }
}
