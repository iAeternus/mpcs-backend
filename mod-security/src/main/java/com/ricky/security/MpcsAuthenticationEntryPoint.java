package com.ricky.security;

import com.ricky.common.exception.MyError;
import com.ricky.common.tracing.TracingService;
import com.ricky.common.utils.MyObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import static com.ricky.common.exception.ErrorCodeEnum.AUTHENTICATION_FAILED;
import static org.apache.commons.lang3.CharEncoding.UTF_8;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class MpcsAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final MyObjectMapper objectMapper;
    private final TracingService mryTracingService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8);
        String traceId = mryTracingService.currentTraceId();
        MyError error = new MyError(AUTHENTICATION_FAILED, 401, "Authentication failed.", request.getRequestURI(), traceId, null);

        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(error.toErrorResponse()));
        writer.flush();
    }
}
