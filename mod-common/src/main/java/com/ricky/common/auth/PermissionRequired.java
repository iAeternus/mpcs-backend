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
     * 资源字段名
     */
    String[] resources();

    /**
     * 是否批量鉴权
     */
    boolean batch() default false;
}