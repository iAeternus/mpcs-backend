package com.ricky.group.domain.aspect;

import com.ricky.common.permission.PermissionRequired;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.group.domain.permission.FilePermissionResource;
import com.ricky.group.domain.permission.FolderPermissionResource;
import com.ricky.group.domain.permission.PermissionMetadata;
import com.ricky.group.domain.permission.PermissionResource;
import lombok.RequiredArgsConstructor;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.nonNull;

@Component
@RequiredArgsConstructor
public class PermissionMetadataResolver {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    // SpEL 缓存
    private final ConcurrentMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    private final FolderRepository folderRepository;

    public PermissionMetadata resolve(JoinPoint joinPoint) {
        Method method = resolveTargetMethod(joinPoint);

        PermissionRequired pr = method.getAnnotation(PermissionRequired.class);
        if (isNull(pr)) {
            pr = joinPoint.getTarget()
                    .getClass()
                    .getAnnotation(PermissionRequired.class);
        }

        return PermissionMetadata.builder()
                .required(Set.of(pr.value()))
                .resourceSpel(pr.resource())
                .resourceType(pr.resourceType())
                .batch(pr.batch())
                .build();
    }

    public PermissionResource resolveResource(JoinPoint joinPoint, PermissionMetadata metadata) {
        Object value = parseSpel(joinPoint, metadata.getResourceSpel());
        return switch (metadata.getResourceType()) {
            case FOLDER -> resolveFolderResource(joinPoint, value.toString());
            case FILE -> resolveFileResource(value.toString());
            case SPACE -> resolveSpaceResource(value.toString());
        };
    }

    private FolderPermissionResource resolveFolderResource(JoinPoint joinPoint, String folderId) {
        Folder folder = folderRepository.byId(folderId);
        return FolderPermissionResource.builder()
                .customId(folder.getCustomId())
                .folderId(folderId)
                .build();
    }

    private FilePermissionResource resolveFileResource(String fileId) {
        return FilePermissionResource.builder()
                .fileId(fileId)
                .build();
    }

    private FolderPermissionResource resolveSpaceResource(String customId) {
        Folder root = folderRepository.getRoot(customId);
        return FolderPermissionResource.builder()
                .customId(customId)
                .folderId(root.getId())
                .build();
    }

    private Object parseSpel(JoinPoint joinPoint, String spel) {
        Method method = resolveTargetMethod(joinPoint);
        EvaluationContext context = createContext(joinPoint, method);
        return getExpression(spel).getValue(context);
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

        if (nonNull(paramNames)) {
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
