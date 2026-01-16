package com.ricky.publicfile;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.command.PostResponse;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import com.ricky.publicfile.service.PublicFileQueryService;
import com.ricky.publicfile.service.PublicFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;

@Validated
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Tag(name = "公共社区文件模块")
@RequestMapping("/public-files")
public class PublicFileController {

    private final PublicFileService publicFileService;
    private final PublicFileQueryService publicFileQueryService;

    @PostMapping
    @Operation(summary = "发布到社区")
    public PostResponse post(@RequestBody @Valid PostCommand command,
                             @AuthenticationPrincipal UserContext userContext) {
        return publicFileService.post(command, userContext);
    }

    @Operation(summary = "撤回文件")
    @DeleteMapping("/{postId}/withdraw")
    public void withdraw(@PathVariable @NotBlank @Id(POST_ID_PREFIX) String postId,
                         @AuthenticationPrincipal UserContext userContext) {
        publicFileService.withdraw(postId, userContext);
    }

    @PutMapping("/title")
    @Operation(summary = "修改标题")
    public void updateTitle(@RequestBody @Valid ModifyTitleCommand command,
                            @AuthenticationPrincipal UserContext userContext) {
        publicFileService.updateTitle(command, userContext);
    }

    @PutMapping("/description")
    @Operation(summary = "编辑介绍文字")
    public void editDescription(@RequestBody @Valid EditDescriptionCommand command,
                                @AuthenticationPrincipal UserContext userContext) {
        publicFileService.editDescription(command, userContext);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询社区文件")
    public PagedList<PublicFileResponse> page(@RequestBody @Valid PublicFilePageQuery query,
                                              @AuthenticationPrincipal UserContext userContext) {
        return publicFileQueryService.page(query, userContext);
    }

}
