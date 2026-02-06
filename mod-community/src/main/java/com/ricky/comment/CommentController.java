package com.ricky.comment;

import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.comment.query.*;
import com.ricky.comment.service.CommentQueryService;
import com.ricky.comment.service.CommentService;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.COMMENT_ID_PREFIX;

@Validated
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Tag(name = "评论模块")
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @PostMapping
    @Operation(summary = "创建评论")
    public CreateCommentResponse createComment(@RequestBody @Valid CreateCommentCommand command,
                                               @AuthenticationPrincipal UserContext userContext) {
        return commentService.createComment(command, userContext);
    }

    @DeleteMapping
    @Operation(summary = "删除评论及其子评论")
    public void deleteComment(@RequestBody @Valid DeleteCommentCommand command,
                              @AuthenticationPrincipal UserContext userContext) {
        commentService.deleteComment(command, userContext);
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "获取评论详情")
    public CommentResponse fetchDetail(@PathVariable @NotBlank @Id(COMMENT_ID_PREFIX) String commentId) {
        return commentQueryService.fetchDetail(commentId);
    }

    @PostMapping("/page")
    @Operation(summary = "分页获取一级评论")
    public PagedList<CommentResponse> page(@RequestBody @Valid CommentPageQuery query) {
        return commentQueryService.page(query);
    }

    @PostMapping("/page/direct")
    @Operation(summary = "分页获取某条评论的直接回复，包括自身")
    public PagedList<CommentResponse> pageDirect(@RequestBody @Valid DirectReplyPageQuery query) {
        return commentQueryService.pageDirect(query);
    }

    @PostMapping("/page/my")
    @Operation(summary = "分页获取我的评论列表")
    public PagedList<MyCommentResponse> pageMyComment(@RequestBody @Valid MyCommentPageQuery query,
                                                      @AuthenticationPrincipal UserContext userContext) {
        return commentQueryService.pageMyComment(query, userContext);
    }

}
