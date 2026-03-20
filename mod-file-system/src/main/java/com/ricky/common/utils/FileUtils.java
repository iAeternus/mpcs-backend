package com.ricky.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.zip.CRC32;

public class FileUtils {

    public static long crc32(InputStream inputStream) throws IOException {
        CRC32 crc32 = new CRC32();
        byte[] buf = new byte[8192];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            crc32.update(buf, 0, len);
        }
        return crc32.getValue();
    }

    /**
     * @param input  文件输入流
     * @param digest 信息摘要算法
     * @return hash字符串
     * @brief 计算文件hash
     */
    public static String calcHash(InputStream input, MessageDigest digest) {
        try {
            byte[] buf = new byte[8192];
            int len;

            while ((len = input.read(buf)) != -1) {
                digest.update(buf, 0, len);
            }

            byte[] bytes = digest.digest();
            return HexFormat.of().formatHex(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
