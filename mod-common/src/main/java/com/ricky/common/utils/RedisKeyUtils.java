package com.ricky.common.utils;

public class RedisKeyUtils {

    public static final String SEPARATE = "::";

    /**
     * 格式：{userId}::{postId}
     */
    public static String likedKey(String userId, String postId) {
        return userId + SEPARATE + postId;
    }

}
