package com.ricky.group.domain.aspect;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static com.ricky.common.exception.MyException.accessDeniedException;
import static com.ricky.common.utils.ValidationUtils.isNull;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {

    private final PermissionMetadataResolver metadataResolver;
    private final PermissionEvaluator permissionEvaluator;
    private final SystemProperties systemProperties;

    @Before("@annotation(com.ricky.common.auth.PermissionRequired) || " +
            "@within(com.ricky.common.auth.PermissionRequired)")
    public void check(JoinPoint joinPoint) {
        if (!systemProperties.isAuth()) {
            return;
        }

        PermissionMetadata metadata = metadataResolver.resolve(joinPoint);
        if (isNull(metadata)) {
            return;
        }

        UserContext user = extractUserContext(joinPoint);
        if (isNull(user)) {
            throw new IllegalStateException("无法获取 UserContext");
        }

        List<Object> resources = metadataResolver.resolveResources(joinPoint, metadata);

        boolean allowed = permissionEvaluator.allowed(user, metadata, resources);
        if (!allowed) {
            throw accessDeniedException();
        }
    }

    private UserContext extractUserContext(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(UserContext.class::isInstance)
                .map(UserContext.class::cast)
                .findFirst()
                .orElse(null);
    }
}
