package com.ricky.common.exception;

import com.ricky.common.tracing.TracingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * 系统中，错误处理存在两种路径：<br>
 * 1. 全局异常处理器，返回ResponseEntity包裹的ErrorResponse<br>
 * 2. SpringSecurity的过滤器，例如{@link com.ricky.common.security.jwt.JwtAuthenticationFilter}，直接返回ErrorResponse<br>
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final TracingService tracingService;

    @ResponseBody
    @ExceptionHandler(MyException.class)
    public ResponseEntity<?> handleMyException(MyException ex, HttpServletRequest request) {
        log.error("Mpcs error: {}", ex.getMessage());
        return createErrorResponse(ex, request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbortException(Exception ex) {
        // 客户端主动断开连接，不是服务端错误
        log.debug("Client aborted connection: {}", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDinedException(HttpServletRequest request) {
        return createErrorResponse(MyException.accessDeniedException(), request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(HttpServletRequest request) {
        return createErrorResponse(MyException.authenticationException(), request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> error = ex.getBindingResult().getFieldErrors().stream()
                .collect(toImmutableMap(FieldError::getField, fieldError -> {
                    String message = fieldError.getDefaultMessage();
                    return isBlank(message) ? "无错误提示。" : message;
                }, (field1, field2) -> field1 + "|" + field2));

        log.error("Method argument validation error[{}]: {}", ex.getParameter().getParameterType().getName(), error);
        MyException exception = MyException.requestValidationException(error);
        return createErrorResponse(exception, request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler({ServletRequestBindingException.class, HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleServletRequestBindingException(Exception ex, HttpServletRequest request) {
        MyException exception = MyException.requestValidationException("message", "请求验证失败。");
        log.error("Request processing error: {}", ex.getMessage());
        return createErrorResponse(exception, request.getRequestURI());
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleGeneralException(Throwable ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String traceId = tracingService.currentTraceId();

        log.error("Error access[{}]:", path, ex);
        MyError error = new MyError(ErrorCodeEnum.SYSTEM_ERROR, ErrorCodeEnum.SYSTEM_ERROR.getStatus(), "系统错误。", path, traceId, null);
        return new ResponseEntity<>(error.toErrorResponse(), new HttpHeaders(), HttpStatus.valueOf(ErrorCodeEnum.SYSTEM_ERROR.getStatus()));
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(MyException exception, String path) {
        String traceId = tracingService.currentTraceId();
        MyError error = new MyError(exception, path, traceId);
        ErrorResponse representation = error.toErrorResponse();
        return new ResponseEntity<>(representation, new HttpHeaders(), HttpStatus.valueOf(representation.getError().getStatus()));
    }

}
