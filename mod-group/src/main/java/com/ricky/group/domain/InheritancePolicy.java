package com.ricky.group.domain;

import lombok.Getter;

/**
 * 继承策略
 */
@Getter
public enum InheritancePolicy {

    NONE("不继承"),
    FULL("完全继承"),
    SELECTIVE("选择性继承"),
    OVERRIDABLE("可覆盖继承"),
    ;

    private final String desc;

    InheritancePolicy(String desc) {
        this.desc = desc;
    }
}