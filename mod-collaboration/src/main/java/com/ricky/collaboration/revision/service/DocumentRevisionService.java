package com.ricky.collaboration.revision.service;

import com.ricky.collaboration.revision.command.CreateRevisionCommand;
import com.ricky.collaboration.revision.query.RevisionDetailResponse;
import com.ricky.collaboration.revision.query.RevisionDiffResponse;
import com.ricky.collaboration.revision.query.RevisionSummaryResponse;
import com.ricky.common.domain.user.UserContext;

import java.util.List;

public interface DocumentRevisionService {

    RevisionDetailResponse createRevision(CreateRevisionCommand command, UserContext userContext);

    List<RevisionSummaryResponse> listRevisions(String documentId, UserContext userContext);

    RevisionDetailResponse getRevision(String documentId, String revisionId, UserContext userContext);

    RevisionDiffResponse getRevisionDiff(String documentId, String revisionId, String compareToRevisionId, UserContext userContext);
}
