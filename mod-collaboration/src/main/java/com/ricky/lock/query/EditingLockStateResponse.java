package com.ricky.lock.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditingLockStateResponse {

    private String sessionId;
    private List<EditingLockResponse> locks;
}
