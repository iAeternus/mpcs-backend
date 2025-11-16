package com.ricky.common.hash;

import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.HashUtils;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

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
            throw new MyException(ErrorCodeEnum.INVALID_HASH_ALGORITHM, "Invalid hash algorithm", "algorithm", algorithm().getName());
        }
    }

    /**
     * @brief 获取hash算法类型
     */
    public abstract HashAlgorithm algorithm();

}
