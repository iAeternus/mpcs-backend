package com.ricky.like.domain;

import lombok.Getter;

@Getter
public enum LikeStatus {

    LIKE(1, "点赞"),
    UNLIKE(0, "取消点赞/未点赞"),
    ;

    private final Integer code;
    private final String msg;

    LikeStatus(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static LikeStatus of(Integer code) {
        return switch (code) {
            case 0 -> UNLIKE;
            case 1 -> LIKE;
            default -> throw new IllegalArgumentException("无效的状态码: " + code);
        };
    }

}
