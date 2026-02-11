package com.ricky.user;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.query.FolderHierarchyResponse;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;
import com.ricky.user.service.UserQueryService;
import com.ricky.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

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
}
