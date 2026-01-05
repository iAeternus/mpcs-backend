package com.ricky.common.hash;

import com.ricky.common.exception.MyException;
import com.ricky.common.utils.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.ricky.common.exception.ErrorCodeEnum.FILE_READ_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.INVALID_HASH_ALGORITHM;

public abstract class AbstractFileHasher {

    public MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(algorithm().getName());
        } catch (NoSuchAlgorithmException e) {
            throw new MyException(
                    INVALID_HASH_ALGORITHM,
                    "Invalid hash algorithm",
                    "algorithm", algorithm().getName()
            );
        }
    }

    public String hash(MultipartFile file) {
        try {
            return hash(file.getInputStream());
        } catch (IOException e) {
            throw new MyException(FILE_READ_FAILED, "File read failed", "file", file);
        }
    }

    /**
     * @param input 文件输入流
     * @return hash字符串
     * @brief hash文件内容
     */
    public String hash(InputStream input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm().getName());
            return FileUtils.calcHash(input, md);
        } catch (NoSuchAlgorithmException e) {
            throw new MyException(INVALID_HASH_ALGORITHM, "Invalid hash algorithm", "algorithm", algorithm().getName());
        }
    }

    /**
     * @brief 获取hash算法类型
     */
    public abstract HashAlgorithm algorithm();

}
