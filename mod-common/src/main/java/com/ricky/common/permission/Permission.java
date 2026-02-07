package com.ricky.common.permission;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

/**
 * 权限
 */
@Getter
public enum Permission {

    CREATE("创建"),
    READ("读取"),
    WRITE("写入"),
    DELETE("删除"),
    MOVE("移动"),
    SHARE("分享"),
    MANAGE("管理"),
    ;

    private final String desc;

    Permission(String desc) {
        this.desc = desc;
    }

    public static Set<Permission> all() {
        return EnumSet.allOf(Permission.class);
    }
}