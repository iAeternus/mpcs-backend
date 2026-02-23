package com.ricky.user;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.command.UploadAvatarResponse;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;
import com.ricky.user.service.UserQueryService;
import com.ricky.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.ricky.common.constants.ConfigConstants.USER_ID_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@Validated
@RestController
@Tag(name = "用户模块")
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserQueryService userQueryService;

    @ResponseStatus(CREATED)
    @Operation(summary = "注册")
    @PostMapping("/registration")
    public RegisterResponse register(@RequestBody @Valid RegisterCommand command) {
        return userService.register(command);
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "上传我的头像")
    @PostMapping(value = "/me/avatar", consumes = MULTIPART_FORM_DATA_VALUE)
    public UploadAvatarResponse uploadMyAvatar(@RequestParam("avatar") @NotNull MultipartFile avatar,
                                               @AuthenticationPrincipal UserContext userContext) {
        return userService.uploadAvatar(avatar, userContext);
    }

    @GetMapping("/me")
    @Operation(summary = "获取我的个人资料")
    public UserProfileResponse fetchMyProfile(@AuthenticationPrincipal UserContext userContext) {
        return userQueryService.fetchMyProfile(userContext);
    }

    @GetMapping("/me/info")
    @Operation(summary = "获取我的用户信息")
    public UserInfoResponse fetchMyUserInfo(@AuthenticationPrincipal UserContext userContext) {
        return userQueryService.fetchMyUserInfo(userContext);
    }

    @GetMapping("/{userId}/info")
    @Operation(summary = "获取用户信息")
    public UserInfoResponse fetchUserInfo(@PathVariable @NotBlank @Id(USER_ID_PREFIX) String userId) {
        return userQueryService.fetchUserInfo(userId);
    }

}
