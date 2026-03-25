package com.ricky.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationAckMessage {

    private String type;
    private String sessionId;
    private long serverVersion;
    private boolean success;
    private String errorMessage;

    public static OperationAckMessage success(String sessionId, long serverVersion) {
        return OperationAckMessage.builder()
                .type("operation_ack")
                .sessionId(sessionId)
                .serverVersion(serverVersion)
                .success(true)
                .build();
    }

    public static OperationAckMessage failure(String sessionId, long serverVersion, String errorMessage) {
        return OperationAckMessage.builder()
                .type("operation_ack")
                .sessionId(sessionId)
                .serverVersion(serverVersion)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
