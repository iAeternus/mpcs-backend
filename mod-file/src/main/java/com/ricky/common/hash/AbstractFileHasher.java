package com.ricky.common.hash;

import com.ricky.common.utils.HashUtils;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractFileHasher {

    /**
     * @param input 文件输入流
     * @return hash字符串
     * @brief hash文件内容
     */
    public String hash(InputStream input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm().getName());
            return HashUtils.calcHash(input, md);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief 获取hash算法类型
     */
    public abstract HashAlgorithm algorithm();

}
