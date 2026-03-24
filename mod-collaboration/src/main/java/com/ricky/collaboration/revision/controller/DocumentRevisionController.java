package com.ricky.collaboration.revision.controller;

import com.ricky.collaboration.revision.command.CreateRevisionCommand;
import com.ricky.collaboration.revision.query.RevisionDetailResponse;
import com.ricky.collaboration.revision.query.RevisionDiffResponse;
import com.ricky.collaboration.revision.query.RevisionSummaryResponse;
import com.ricky.collaboration.revision.service.DocumentRevisionService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.DOCUMENT_REVISION_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "文档版本管理")
@RequestMapping("/collaboration/documents/{documentId}/revisions")
public class DocumentRevisionController {

    private final DocumentRevisionService revisionService;

    @PostMapping
    @Operation(summary = "创建文档版本")
    public RevisionDetailResponse createRevision(
            @PathVariable @Id(FILE_ID_PREFIX) String documentId,
            @RequestBody @Valid CreateRevisionCommand command,
            @AuthenticationPrincipal UserContext userContext
    ) {
        command.setDocumentId(documentId);
        return revisionService.createRevision(command, userContext);
    }

    @GetMapping
    @Operation(summary = "查询文档版本列表")
    public List<RevisionSummaryResponse> listRevisions(
            @PathVariable @Id(FILE_ID_PREFIX) String documentId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return revisionService.listRevisions(documentId, userContext);
    }

    @GetMapping("/{revisionId}")
    @Operation(summary = "查询文档版本详情")
    public RevisionDetailResponse getRevision(
            @PathVariable @Id(FILE_ID_PREFIX) String documentId,
            @PathVariable @Id(DOCUMENT_REVISION_ID_PREFIX) String revisionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return revisionService.getRevision(documentId, revisionId, userContext);
    }

    @GetMapping("/{revisionId}/diff")
    @Operation(summary = "查询文档版本差异")
    public RevisionDiffResponse getDiff(
            @PathVariable @Id(FILE_ID_PREFIX) String documentId,
            @PathVariable @Id(DOCUMENT_REVISION_ID_PREFIX) String revisionId,
            @RequestParam(required = false) String compareToRevisionId,
            @AuthenticationPrincipal UserContext userContext
    ) {
        return revisionService.getRevisionDiff(documentId, revisionId, compareToRevisionId, userContext);
    }
}
