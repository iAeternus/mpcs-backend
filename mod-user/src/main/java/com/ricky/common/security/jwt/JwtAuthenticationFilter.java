package com.ricky.common.security.jwt;

import com.ricky.common.exception.MyError;
import com.ricky.common.exception.MyException;
import com.ricky.common.tracing.TracingService;
import com.ricky.common.utils.MyObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

import static com.ricky.common.constants.ConfigConstants.*;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.WebUtils.getCookie;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final MyObjectMapper objectMapper;
    private final TracingService tracingService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   MyObjectMapper objectMapper,
                                   TracingService tracingService) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
        this.tracingService = tracingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication token = convert(request);

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authenticationIsRequired()) {
                Authentication authenticatedToken = authenticationManager.authenticate(token);
                SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
            }
        } catch (MyException ex) {
            SecurityContextHolder.clearContext();

            int status = ex.getCode().getStatus();
            if (status == 401 || status == 409) { // 对于401和409异常，直接中断执行并返回
                response.setStatus(status);
                response.setContentType(APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(UTF_8);
                String traceId = tracingService.currentTraceId();
                MyError error = new MyError(ex.getCode(),
                        status,
                        ex.getUserMessage(),
                        request.getRequestURI(),
                        traceId,
                        null);

                PrintWriter writer = response.getWriter();
                writer.print(objectMapper.writeValueAsString(error.toErrorResponse()));
                writer.flush();
                return;
            }
        } catch (Throwable t) { // 其他异常继续执行，之后的MpcsAuthenticationEntryPoint会捕捉到
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Authentication convert(HttpServletRequest request) {
        // 先提取cookie中的jwt，如果有，无论是否合法均不再检查authorization头
        Cookie tokenCookie = getCookie(request, AUTH_COOKIE_NAME);
        if (tokenCookie != null && isNotBlank(tokenCookie.getValue())) {
            return new JwtAuthenticationToken(tokenCookie.getValue());
        }

        // 如果没有提供cookie，再尝试检查authorization头
        String bearerToken = extractBearerToken(request);
        if (isNotBlank(bearerToken)) {
            return new JwtAuthenticationToken(bearerToken);
        }
        return null;
    }


    private String extractBearerToken(HttpServletRequest request) {
        String authorizationString = request.getHeader(AUTHORIZATION);

        if (isBlank(authorizationString) || !authorizationString.startsWith(BEARER)) {
            return null;
        }

        return authorizationString.substring(BEARER.length());
    }


    private boolean authenticationIsRequired() {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }

        return existingAuth instanceof AnonymousAuthenticationToken;
    }


}
