package com.ricky.collaboration.collaboration.query;

import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistoryResponse {
    
    private String sessionId;
    private long fromVersion;
    private long toVersion;
    private List<TextOperation> operations;
}
