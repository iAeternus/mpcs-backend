package com.ricky.collaboration.revision.query;

import com.ricky.collaboration.revision.domain.DocumentRevision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionDetailResponse {

    private String revisionId;
    private String sessionId;
    private String documentId;
    private String documentTitle;
    private long revisionNo;
    private long baseVersion;
    private String contentSnapshot;
    private String changeSummary;
    private String source;
    private String createdBy;
    private String creator;
    private Instant createdAt;

    public static RevisionDetailResponse from(DocumentRevision revision) {
        return RevisionDetailResponse.builder()
                .revisionId(revision.getId())
                .sessionId(revision.getSessionId())
                .documentId(revision.getDocumentId())
                .documentTitle(revision.getDocumentTitle())
                .revisionNo(revision.getRevisionNo())
                .baseVersion(revision.getBaseVersion())
                .contentSnapshot(revision.getContentSnapshot())
                .changeSummary(revision.getChangeSummary())
                .source(revision.getSource().name())
                .createdBy(revision.getCreatedBy())
                .creator(revision.getCreator())
                .createdAt(revision.getCreatedAt())
                .build();
    }
}
