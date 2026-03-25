package com.ricky.revision.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.ricky.revision.command.CreateRevisionCommand;
import com.ricky.revision.domain.DocumentRevision;
import com.ricky.revision.domain.DocumentRevisionDomainService;
import com.ricky.revision.domain.DocumentRevisionRepository;
import com.ricky.revision.domain.RevisionSource;
import com.ricky.revision.query.RevisionDetailResponse;
import com.ricky.revision.query.RevisionDiffResponse;
import com.ricky.revision.query.RevisionSummaryResponse;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentRevisionServiceImpl implements DocumentRevisionService {

    private final DocumentRevisionRepository revisionRepository;
    private final DocumentRevisionDomainService domainService;

    @Override
    @Transactional
    public RevisionDetailResponse createRevision(CreateRevisionCommand command, UserContext userContext) {
        DocumentRevision revision = domainService.createRevision(
                command.getSessionId(),
                command.getDocumentId(),
                command.getDocumentTitle(),
                command.getBaseVersion(),
                command.getContent(),
                command.getChangeSummary(),
                RevisionSource.from(command.getSource()),
                userContext
        );
        revisionRepository.save(revision);
        return RevisionDetailResponse.from(revision);
    }

    @Override
    public List<RevisionSummaryResponse> listRevisions(String documentId, UserContext userContext) {
        return revisionRepository.findByDocumentId(documentId).stream()
                .map(RevisionSummaryResponse::from)
                .toList();
    }

    @Override
    public RevisionDetailResponse getRevision(String documentId, String revisionId, UserContext userContext) {
        return RevisionDetailResponse.from(requireRevision(documentId, revisionId));
    }

    @Override
    public RevisionDiffResponse getRevisionDiff(String documentId, String revisionId, String compareToRevisionId, UserContext userContext) {
        DocumentRevision right = requireRevision(documentId, revisionId);
        DocumentRevision left = compareToRevisionId == null || compareToRevisionId.isBlank()
                ? revisionRepository.findByDocumentId(documentId).stream()
                .filter(revision -> revision.getRevisionNo() == right.getRevisionNo() - 1)
                .findFirst()
                .orElse(null)
                : requireRevision(documentId, compareToRevisionId);

        String leftContent = left != null ? left.getContentSnapshot() : "";
        String rightContent = right.getContentSnapshot();

        List<String> original = Arrays.asList(leftContent.split("\\R", -1));
        List<String> revised = Arrays.asList(rightContent.split("\\R", -1));

        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                left != null ? left.getId() : "empty",
                right.getId(),
                original,
                DiffUtils.diff(original, revised),
                3
        );

        return RevisionDiffResponse.builder()
                .revisionId(right.getId())
                .compareToRevisionId(left != null ? left.getId() : null)
                .unifiedDiffLines(unifiedDiff)
                .leftContent(leftContent)
                .rightContent(rightContent)
                .build();
    }

    private DocumentRevision requireRevision(String documentId, String revisionId) {
        DocumentRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> MyException.requestValidationException("Revision not found"));
        if (!revision.getDocumentId().equals(documentId)) {
            throw MyException.requestValidationException("Revision does not belong to document");
        }
        return revision;
    }
}
