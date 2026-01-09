package com.ricky.group.domain;

import com.ricky.common.auth.Permission;
import com.ricky.common.auth.PermissionRequired;
import com.ricky.common.domain.SpaceType;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.MyException.accessDeniedException;
import static com.ricky.common.utils.CommonUtils.objectToListString;
import static com.ricky.common.utils.CommonUtils.objectToString;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {

    private final GroupDomainService groupDomainService;
    private final SystemProperties systemProperties;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Before("@annotation(permissionRequired) || @within(permissionRequired)")
    public void checkPermission(JoinPoint joinPoint, PermissionRequired permissionRequired) {
        if (!systemProperties.isAuth()) {
            return;
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        UserContext userContext = extractUserContext(joinPoint);
        if (userContext == null) {
            throw new IllegalStateException("无法获取 UserContext");
        }

        List<Object> resources = resolveResources(joinPoint, method, permissionRequired.resources());
        Set<Permission> required = Set.of(permissionRequired.value());

        boolean allowed;
        if (permissionRequired.batch()) {
            allowed = checkBatch(userContext.getUid(), resources, required);
        } else {
            allowed = checkSingle(userContext.getUid(), resources, required);
        }

        if (!allowed) {
            throw accessDeniedException();
        }
    }

    private boolean checkSingle(String userId, List<Object> resources, Set<Permission> required) {
        if (resources.size() != 2) {
            throw new IllegalArgumentException("资源列表必须包含customId和folderId");
        }

        String customId = objectToString(resources.get(0));
        SpaceType spaceType = SpaceType.fromPrefix(customId.split("-")[0]);
        if (spaceType == SpaceType.PERSONAL || spaceType == SpaceType.PUBLIC) {
            return true; // 个人空间和公共空间不鉴权
        }

        String folderId = objectToString(resources.get(1));
        return groupDomainService.hasPermission(userId, customId, folderId, required);
    }

    private boolean checkBatch(String userId, List<Object> resources, Set<Permission> required) {
        if (resources.size() != 2) {
            throw new IllegalArgumentException("资源列表必须包含customId和folderIds");
        }

        String customId = objectToString(resources.get(0));
        SpaceType spaceType = SpaceType.fromPrefix(customId.split("-")[0]);
        if (spaceType == SpaceType.PERSONAL || spaceType == SpaceType.PUBLIC) {
            return true; // 个人空间和公共空间不鉴权
        }

        List<String> folderIds = objectToListString(resources.get(1));
        return groupDomainService.hasPermission(userId, customId, folderIds, required);
    }

    // SpEL 解析
    private Object resolveResource(JoinPoint joinPoint, Method method, String spel) {
        EvaluationContext context = new StandardEvaluationContext();

        String[] paramNames = paramNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        Expression expression = parser.parseExpression(spel);
        return expression.getValue(context);
    }

    // SpEL 解析
    // 单资源鉴权格式：resources = {"#customId", "#folderId"}
    // 批量鉴权格式：resources = {"#customId", "#folderIds"}
    private List<Object> resolveResources(JoinPoint joinPoint, Method method, String[] spelExpressions) {
        EvaluationContext context = createEvaluationContext(joinPoint, method);
        return Arrays.stream(spelExpressions)
                .map(spel -> {
                    Expression expression = parser.parseExpression(spel);
                    return expression.getValue(context);
                })
                .collect(toImmutableList());
    }

    private EvaluationContext createEvaluationContext(JoinPoint joinPoint, Method method) {
        EvaluationContext context = new StandardEvaluationContext();

        String[] paramNames = paramNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return context;
    }

    private UserContext extractUserContext(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof UserContext uc) {
                return uc;
            }
        }
        return null;
    }
}
