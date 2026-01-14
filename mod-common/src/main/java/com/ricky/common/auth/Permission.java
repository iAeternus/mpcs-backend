package com.ricky.common.auth;

import lombok.Getter;

/**
 * 权限
 */
@Getter
public enum Permission {

    CREATE("创建"), // 对父资源而言
    READ("读取"),
    WRITE("写入"),
    DELETE("删除"),
    DELETE_FORCE("彻底删除"),
    MOVE("移动"),
    SHARE("分享"),
    MANAGE("管理"),
    DOWNLOAD("下载"),
    PREVIEW("预览"),
    COMMENT("评论"),
    APPROVE("审批"),
    RENAME("重命名"),
    ;

    private final String desc;

    Permission(String desc) {
        this.desc = desc;
    }
}