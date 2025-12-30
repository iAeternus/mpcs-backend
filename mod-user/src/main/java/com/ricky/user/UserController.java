package com.ricky.user;

import com.ricky.common.domain.user.UserContext;
import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;
import com.ricky.user.domain.dto.resp.UserInfoResponse;
import com.ricky.user.domain.dto.resp.UserProfileResponse;
import com.ricky.user.service.UserQueryService;
import com.ricky.user.service.UserService;
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
@RequiredArgsConstructor
@RequestMapping(value = "/user")
public class UserController {

    private final UserService userService;
    private final UserQueryService userQueryService;

    @PostMapping("/registration")
    @ResponseStatus(CREATED)
    public RegisterResponse register(@RequestBody @Valid RegisterCommand command) {
        return userService.register(command);
    }

    @GetMapping(value = "/me")
    public UserProfileResponse fetchMyProfile(@AuthenticationPrincipal UserContext userContext) {
        return userQueryService.fetchMyProfile(userContext);
    }

    @GetMapping(value = "/me/info")
    public UserInfoResponse fetchMyUserInfo(@AuthenticationPrincipal UserContext userContext) {
        return userQueryService.fetchMyUserInfo(userContext);
    }

}
