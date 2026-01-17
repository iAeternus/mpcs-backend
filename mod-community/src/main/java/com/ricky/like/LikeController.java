package com.ricky.like;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.like.query.LikedCountResponse;
import com.ricky.like.service.LikeQueryService;
import com.ricky.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;

@Validated
@CrossOrigin
@RestController
@Tag(name = "点赞模块")
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;
    private final LikeQueryService likeQueryService;

    @PostMapping("/{postId}/like")
    @Operation(summary = "点赞")
    public void like(@PathVariable @NotBlank @Id(POST_ID_PREFIX) String postId,
                     @AuthenticationPrincipal UserContext userContext) {
        likeService.like(postId, userContext);
    }

    @PostMapping("/{postId}/unlike")
    @Operation(summary = "取消点赞")
    public void unlike(@PathVariable @NotBlank @Id(POST_ID_PREFIX) String postId,
                       @AuthenticationPrincipal UserContext userContext) {
        likeService.unlike(postId, userContext);
    }

    @PostMapping("/{postId}/count")
    @Operation(summary = "获取发布物被点赞数量")
    public LikedCountResponse fetchLikedCount(@PathVariable @NotBlank @Id(POST_ID_PREFIX) String postId) {
        return likeQueryService.fetchLikedCount(postId);
    }

}
