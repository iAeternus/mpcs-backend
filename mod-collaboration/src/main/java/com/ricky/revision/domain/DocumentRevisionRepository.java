package com.ricky.revision.domain;

import java.util.List;
import java.util.Optional;

public interface DocumentRevisionRepository {

    void save(DocumentRevision revision);

    Optional<DocumentRevision> findById(String revisionId);

    List<DocumentRevision> findByDocumentId(String documentId);

    Optional<DocumentRevision> findLatestByDocumentId(String documentId);
}
