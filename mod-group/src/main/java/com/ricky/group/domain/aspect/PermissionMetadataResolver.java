package com.ricky.group.domain.aspect;

import com.ricky.common.auth.PermissionRequired;
import org.aspectj.lang.JoinPoint;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ricky.common.utils.ValidationUtils.isNull;

@Component
public class PermissionMetadataResolver {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    // SpEL 缓存
    private final ConcurrentMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public PermissionMetadata resolve(JoinPoint joinPoint) {
        Method method = resolveTargetMethod(joinPoint);

        PermissionRequired pr = method.getAnnotation(PermissionRequired.class);
        if (isNull(pr)) {
            pr = joinPoint.getTarget()
                    .getClass()
                    .getAnnotation(PermissionRequired.class);
        }

        if (isNull(pr)) {
            return null;
        }

        return new PermissionMetadata(Set.of(pr.value()), pr.resources(), pr.batch());
    }

    public List<Object> resolveResources(JoinPoint joinPoint, PermissionMetadata metadata) {
        Method method = resolveTargetMethod(joinPoint);
        EvaluationContext context = createContext(joinPoint, method);

        return Arrays.stream(metadata.resourceSpels())
                .map(spel -> getExpression(spel).getValue(context))
                .toList();
    }

    private Method resolveTargetMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        try {
            return joinPoint.getTarget()
                    .getClass()
                    .getMethod(signature.getName(), signature.getMethod().getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private EvaluationContext createContext(JoinPoint joinPoint, Method method) {
        EvaluationContext context = new StandardEvaluationContext();

        String[] paramNames = nameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        return context;
    }

    private Expression getExpression(String spel) {
        return expressionCache.computeIfAbsent(spel, parser::parseExpression);
    }
}
