package com.ricky.common.security;

import com.ricky.common.exception.MyError;
import com.ricky.common.json.JsonCodec;
import com.ricky.common.tracing.TracingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import static com.ricky.common.constants.ConfigConstants.DEFAULT_CHARSET;
import static com.ricky.common.exception.ErrorCodeEnum.AUTHENTICATION_FAILED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
public class MpcsAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final JsonCodec jsonCodec;
    private final TracingService tracingService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(DEFAULT_CHARSET.name());
        String traceId = tracingService.currentTraceId();
        MyError error = new MyError(AUTHENTICATION_FAILED, 401, "Authentication failed.", request.getRequestURI(), traceId, null);

        PrintWriter writer = response.getWriter();
        writer.print(jsonCodec.writeValueAsString(error.toErrorResponse()));
        writer.flush();
    }
}
