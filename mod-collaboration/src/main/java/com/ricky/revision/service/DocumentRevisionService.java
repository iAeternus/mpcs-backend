package com.ricky.revision.service;

import com.ricky.revision.command.CreateRevisionCommand;
import com.ricky.revision.query.RevisionDetailResponse;
import com.ricky.revision.query.RevisionDiffResponse;
import com.ricky.revision.query.RevisionSummaryResponse;
import com.ricky.common.domain.user.UserContext;

import java.util.List;

public interface DocumentRevisionService {

    RevisionDetailResponse createRevision(CreateRevisionCommand command, UserContext userContext);

    List<RevisionSummaryResponse> listRevisions(String documentId, UserContext userContext);

    RevisionDetailResponse getRevision(String documentId, String revisionId, UserContext userContext);

    RevisionDiffResponse getRevisionDiff(String documentId, String revisionId, String compareToRevisionId, UserContext userContext);
}
