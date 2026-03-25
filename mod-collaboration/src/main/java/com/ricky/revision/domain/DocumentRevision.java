package com.ricky.revision.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.ricky.common.constants.ConfigConstants.DOCUMENT_REVISION_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.DOCUMENT_REVISION_ID_PREFIX;
import static lombok.AccessLevel.PROTECTED;

@Getter
@FieldNameConstants
@Document(DOCUMENT_REVISION_COLLECTION)
@TypeAlias("document_revision")
@NoArgsConstructor(access = PROTECTED)
public class DocumentRevision extends AggregateRoot {

    private String sessionId;
    private String documentId;
    private String documentTitle;
    private long revisionNo;
    private long baseVersion;
    private String contentSnapshot;
    private String changeSummary;
    private RevisionSource source;
    private Long fromVersion;
    private Long toVersion;
    private Instant revisionAt;

    private DocumentRevision(
            String id,
            String sessionId,
            String documentId,
            String documentTitle,
            long revisionNo,
            long baseVersion,
            String contentSnapshot,
            String changeSummary,
            RevisionSource source,
            Long fromVersion,
            Long toVersion,
            UserContext userContext
    ) {
        super(id, userContext);
        this.sessionId = sessionId;
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.revisionNo = revisionNo;
        this.baseVersion = baseVersion;
        this.contentSnapshot = contentSnapshot;
        this.changeSummary = changeSummary;
        this.source = source;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.revisionAt = Instant.now();
        addOpsLog("创建文档版本[" + revisionNo + "]", userContext);
    }

    public static DocumentRevision create(
            String sessionId,
            String documentId,
            String documentTitle,
            long revisionNo,
            long baseVersion,
            String contentSnapshot,
            String changeSummary,
            RevisionSource source,
            Long fromVersion,
            Long toVersion,
            UserContext userContext
    ) {
        return new DocumentRevision(
                newRevisionId(),
                sessionId,
                documentId,
                documentTitle,
                revisionNo,
                baseVersion,
                contentSnapshot,
                changeSummary,
                source,
                fromVersion,
                toVersion,
                userContext
        );
    }

    public static String newRevisionId() {
        return DOCUMENT_REVISION_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }
}
