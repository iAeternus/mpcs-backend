package com.ricky.collaboration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ricky.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.domain.ot.TextOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationMessage {

    private String type;
    private String sessionId;
    private String oderId;
    private Long serverVersion;

    @JsonProperty("operation")
    private OperationData operation;

    @JsonProperty("operations")
    private List<OperationData> operations;

    private TextOperationType operationType;
    private Integer position;
    private String content;
    private Integer length;
    private Long clientVersion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperationData {
        private String id;

        @JsonProperty("type")
        private TextOperationType operationType;

        private Integer position;
        private String content;
        private Integer length;
        private String userId;
        private Long clientVersion;
        private String timestamp;
    }

    public TextOperation toTextOperation() {
        TextOperationType opType;
        Integer pos;
        String contentVal;
        Integer len;
        String userId;
        Long version;

        if (operation != null) {
            opType = operation.getOperationType();
            pos = operation.getPosition();
            contentVal = operation.getContent();
            len = operation.getLength();
            userId = operation.getUserId();
            version = operation.getClientVersion();
        } else {
            opType = this.operationType;
            pos = this.position;
            contentVal = this.content;
            len = this.length;
            userId = this.oderId;
            version = this.clientVersion;
        }

        if (opType == null) {
            throw new IllegalArgumentException("operationType is required");
        }
        if (pos == null) {
            throw new IllegalArgumentException("position is required");
        }

        long finalVersion = version != null ? version : 0L;

        if (opType == TextOperationType.INSERT) {
            return TextOperation.insert(userId, pos, contentVal, finalVersion);
        } else if (opType == TextOperationType.DELETE) {
            return TextOperation.delete(userId, pos, len != null ? len : 0, finalVersion);
        } else {
            return TextOperation.retain(userId, pos, len != null ? len : 0, finalVersion);
        }
    }

    public static OperationMessage fromTextOperation(String sessionId, TextOperation op, long serverVersion) {
        return OperationMessage.builder()
                .type("operation")
                .sessionId(sessionId)
                .oderId(op.getUserId())
                .serverVersion(serverVersion)
                .operationType(op.getType())
                .position(op.getPosition())
                .content(op.getContent())
                .length(op.getLength())
                .clientVersion(op.getClientVersion())
                .operation(OperationData.builder()
                        .id(op.getId())
                        .operationType(op.getType())
                        .position(op.getPosition())
                        .content(op.getContent())
                        .length(op.getLength())
                        .userId(op.getUserId())
                        .clientVersion(op.getClientVersion())
                        .timestamp(op.getTimestamp() != null ? op.getTimestamp().toString() : null)
                        .build())
                .build();
    }
}
