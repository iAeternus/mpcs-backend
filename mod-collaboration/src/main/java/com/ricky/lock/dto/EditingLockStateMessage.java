package com.ricky.lock.dto;

import com.ricky.lock.query.EditingLockResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditingLockStateMessage {

    private String type;
    private String sessionId;
    private List<EditingLockResponse> locks;

    public static EditingLockStateMessage of(String sessionId, List<EditingLockResponse> locks) {
        return EditingLockStateMessage.builder()
                .type("lock_state")
                .sessionId(sessionId)
                .locks(locks)
                .build();
    }
}
