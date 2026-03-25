package com.ricky.lock.query;

import com.ricky.lock.domain.EditingLock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditingLockResponse {

    private String lockId;
    private String userId;
    private String username;
    private int start;
    private int end;
    private Instant acquiredAt;
    private Instant expiresAt;

    public static EditingLockResponse from(EditingLock lock) {
        return EditingLockResponse.builder()
                .lockId(lock.getLockId())
                .userId(lock.getUserId())
                .username(lock.getUsername())
                .start(lock.getStart())
                .end(lock.getEnd())
                .acquiredAt(lock.getAcquiredAt())
                .expiresAt(lock.getExpiresAt())
                .build();
    }
}
