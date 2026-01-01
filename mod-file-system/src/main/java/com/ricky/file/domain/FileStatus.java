package com.ricky.file.domain;

/**
 * @brief 文件状态
 */
public enum FileStatus {

    NORMAL, // 普通可用状态（默认）
    TRASHED, // 进入回收站（软删）
    DELETED, // 已被永久删除（不可恢复）

}
