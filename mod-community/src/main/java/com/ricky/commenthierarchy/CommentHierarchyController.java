package com.ricky.commenthierarchy;

import com.ricky.commenthierarchy.command.ReplyCommand;
import com.ricky.commenthierarchy.command.ReplyResponse;
import com.ricky.commenthierarchy.query.ReplyPageQuery;
import com.ricky.commenthierarchy.query.ReplyPageResponse;
import com.ricky.commenthierarchy.service.CommentHierarchyQueryService;
import com.ricky.commenthierarchy.service.CommentHierarchyService;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Tag(name = "评论层次结构模块")
@RequestMapping("/replies")
public class CommentHierarchyController {

    private final CommentHierarchyService commentHierarchyService;
    private final CommentHierarchyQueryService commentHierarchyQueryService;

    @PostMapping
    @Operation(summary = "回复某条评论")
    public ReplyResponse reply(@RequestBody @Valid ReplyCommand command,
                               @AuthenticationPrincipal UserContext userContext) {
        return commentHierarchyService.reply(command, userContext);
    }

//    @PostMapping("/page")
//    @Operation(summary = "分页获取某评论的所有回复 - 未实现")
//    public PagedList<ReplyPageResponse> pageReply(@RequestBody @Valid ReplyPageQuery query) {
//        return commentHierarchyQueryService.pageReply(query);
//    }

}
