package com.ricky.common.hash;

import lombok.Getter;

/**
 * @brief 哈希算法枚举
 */
@Getter
public enum HashAlgorithm {
    MD5("MD5"),
    SHA256("SHA-256"),
    SHA512("SHA-512");

    private final String name;

    HashAlgorithm(String name) {
        this.name = name;
    }

}
