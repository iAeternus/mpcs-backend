package com.ricky.collaboration.collaboration.dto;

import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationMessage {
    
    private String type;
    private String sessionId;
    private String oderId;
    private TextOperationType operationType;
    private Integer position;
    private String content;
    private Integer length;
    private Long clientVersion;
    
    public TextOperation toTextOperation() {
        if (operationType == TextOperationType.INSERT) {
            return TextOperation.insert(oderId, position, content, clientVersion);
        } else if (operationType == TextOperationType.DELETE) {
            return TextOperation.delete(oderId, position, length, clientVersion);
        } else {
            return TextOperation.retain(oderId, position, length != null ? length : 0, clientVersion);
        }
    }
    
    public static OperationMessage fromTextOperation(String sessionId, TextOperation op) {
        return OperationMessage.builder()
                .type("operation")
                .sessionId(sessionId)
                .oderId(op.getUserId())
                .operationType(op.getType())
                .position(op.getPosition())
                .content(op.getContent())
                .length(op.getLength())
                .clientVersion(op.getClientVersion())
                .build();
    }
}
