package com.ricky.revision.query;

import com.ricky.revision.domain.DocumentRevision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionSummaryResponse {

    private String revisionId;
    private String documentId;
    private long revisionNo;
    private long baseVersion;
    private String changeSummary;
    private String source;
    private String createdBy;
    private String creator;
    private Instant createdAt;

    public static RevisionSummaryResponse from(DocumentRevision revision) {
        return RevisionSummaryResponse.builder()
                .revisionId(revision.getId())
                .documentId(revision.getDocumentId())
                .revisionNo(revision.getRevisionNo())
                .baseVersion(revision.getBaseVersion())
                .changeSummary(revision.getChangeSummary())
                .source(revision.getSource().name())
                .createdBy(revision.getCreatedBy())
                .creator(revision.getCreator())
                .createdAt(revision.getCreatedAt())
                .build();
    }
}
