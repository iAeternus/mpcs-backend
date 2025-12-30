package com.ricky.login;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.security.IpJwtCookieUpdater;
import com.ricky.common.security.jwt.JwtCookieFactory;
import com.ricky.login.domain.dto.cmd.MobileOrEmailLoginCommand;
import com.ricky.login.domain.dto.cmd.VerificationCodeLoginCommand;
import com.ricky.login.domain.dto.resp.JwtTokenResponse;
import com.ricky.login.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping
@Tag(name = "登录模块")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final IpJwtCookieUpdater ipJwtCookieUpdater;
    private final JwtCookieFactory jwtCookieFactory;

    @PostMapping(value = "/login")
    @Operation(summary = "使用手机号或邮箱登录")
    public JwtTokenResponse loginWithMobileOrEmail(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @RequestBody @Valid MobileOrEmailLoginCommand command) {
        String jwt = loginService.loginWithMobileOrEmail(command);
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.newJwtCookie(jwt), request));
        return JwtTokenResponse.builder().token(jwt).build();
    }

    @Operation(summary = "使用验证码登录")
    @PostMapping(value = "/verification-code-login")
    public JwtTokenResponse loginWithVerificationCode(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      @RequestBody @Valid VerificationCodeLoginCommand command) {
        String jwt = loginService.loginWithVerificationCode(command);
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.newJwtCookie(jwt), request));
        return JwtTokenResponse.builder().token(jwt).build();
    }

    @Operation(summary = "登出")
    @DeleteMapping(value = "/logout")
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       @AuthenticationPrincipal UserContext userContext) {
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.logoutCookie(), request));
        if (userContext.isLoggedIn()) {
            log.info("User[{}] tried log out.", userContext.getUid());
        }
    }

    @Operation(summary = "刷新Token")
    @PutMapping(value = "/refresh-token")
    public JwtTokenResponse refreshToken(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @AuthenticationPrincipal UserContext userContext) {
        String jwt = loginService.refreshToken(userContext);
        response.addCookie(ipJwtCookieUpdater.updateCookie(jwtCookieFactory.newJwtCookie(jwt), request));
        return JwtTokenResponse.builder().token(jwt).build();
    }

}
