package com.ricky.collaboration.revision.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionDiffResponse {

    private String revisionId;
    private String compareToRevisionId;
    private List<String> unifiedDiffLines;
    private String leftContent;
    private String rightContent;
}
