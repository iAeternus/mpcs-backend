package com.ricky.common.permission;

import java.lang.annotation.*;

/**
 * 需要的权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionRequired {

    Permission[] value();

    /**
     * 资源 SpEL 表达式
     * 支持 #folderId/#fileId/#customId
     * 语义分别为 校验文件夹权限、校验文件的父文件夹权限、校验空间根文件夹权限
     */
    String resource();

    /**
     * 资源类型
     */
    ResourceType resourceType() default ResourceType.FOLDER;

    boolean batch() default false;
}