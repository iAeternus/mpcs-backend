package com.ricky.revision.domain;

import com.ricky.common.domain.user.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentRevisionDomainService {

    private final DocumentRevisionRepository revisionRepository;

    public DocumentRevision createRevision(
            String sessionId,
            String documentId,
            String documentTitle,
            long baseVersion,
            String contentSnapshot,
            String changeSummary,
            RevisionSource source,
            UserContext userContext
    ) {
        long nextRevisionNo = revisionRepository.findLatestByDocumentId(documentId)
                .map(DocumentRevision::getRevisionNo)
                .orElse(0L) + 1;

        return DocumentRevision.create(
                sessionId,
                documentId,
                documentTitle,
                nextRevisionNo,
                baseVersion,
                contentSnapshot,
                normalizeSummary(changeSummary, nextRevisionNo, source),
                source,
                null,
                baseVersion,
                userContext
        );
    }

    private String normalizeSummary(String changeSummary, long revisionNo, RevisionSource source) {
        if (changeSummary != null && !changeSummary.isBlank()) {
            return changeSummary;
        }
        return source.name() + " revision #" + revisionNo;
    }
}
