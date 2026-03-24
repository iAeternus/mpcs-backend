package com.ricky.collaboration.lock.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EditingLock {

    private String lockId;
    private String userId;
    private String username;
    private int start;
    private int end;
    private Instant acquiredAt;
    private Instant expiresAt;
    private Instant lastHeartbeatAt;

    public static EditingLock create(String userId, String username, int start, int end, Instant now, Instant expiresAt) {
        return EditingLock.builder()
                .lockId(UUID.randomUUID().toString())
                .userId(userId)
                .username(username)
                .start(Math.min(start, end))
                .end(Math.max(start, end))
                .acquiredAt(now)
                .expiresAt(expiresAt)
                .lastHeartbeatAt(now)
                .build();
    }

    public EditingLock renew(Instant now, Instant newExpiresAt) {
        return EditingLock.builder()
                .lockId(lockId)
                .userId(userId)
                .username(username)
                .start(start)
                .end(end)
                .acquiredAt(acquiredAt)
                .expiresAt(newExpiresAt)
                .lastHeartbeatAt(now)
                .build();
    }

    public EditingLock withRange(int newStart, int newEnd) {
        return EditingLock.builder()
                .lockId(lockId)
                .userId(userId)
                .username(username)
                .start(Math.min(newStart, newEnd))
                .end(Math.max(newStart, newEnd))
                .acquiredAt(acquiredAt)
                .expiresAt(expiresAt)
                .lastHeartbeatAt(lastHeartbeatAt)
                .build();
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public boolean conflictsWith(EditingLock other) {
        if (this.end == this.start && other.end == other.start) {
            return this.start == other.start;
        }
        if (this.end == this.start) {
            return other.start <= this.start && this.start <= other.end;
        }
        if (other.end == other.start) {
            return this.start <= other.start && other.start <= this.end;
        }
        return this.start < other.end && other.start < this.end;
    }
}
