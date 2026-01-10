package com.ricky.common.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 需要的权限
 */
@Target({METHOD, TYPE})
@Retention(RUNTIME)
@Documented
public @interface PermissionRequired {

    /**
     * 要求的权限集合
     */
    Permission[] value();

    /**
     * SpEL 表达式
     * 单资源: {"#customId", "#folderId"}
     * 批量资源: {"#customId", "#folderIds"}
     */
    String[] resources();

    /**
     * 是否批量鉴权
     */
    boolean batch() default false;
}