package com.ricky.security;

import com.ricky.common.domain.user.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpHeaders.USER_AGENT;

public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        MDC.put("userAgent", request.getHeader(USER_AGENT));

        String remoteAddr = request.getRemoteAddr();
        String xForwardedFor = request.getHeader("x-forwarded-for");
        MDC.put("clientIp", isNotBlank(xForwardedFor) ? xForwardedFor : remoteAddr);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof MpcsAuthenticationToken token) {
            UserContext user = token.getUser();
            MDC.put("uid", user.getUid());
        }
        filterChain.doFilter(request, response);
        MDC.clear();
    }

}
