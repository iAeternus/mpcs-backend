package com.ricky.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.stream.Stream;
import java.util.zip.CRC32;

@Slf4j
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

    public static String generateFilename(String originalFilename) {
        return originalFilename + UuidGenerator.newShortUuid();
    }

    @Deprecated
    public static void deleteUploadSessionChunkDir(Path chunkRootDir, String uploadId) {
        Path sessionDir = chunkRootDir.resolve(uploadId);

        if (!Files.exists(sessionDir)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(sessionDir)) {
            paths.sorted(Comparator.reverseOrder()) // 先删文件，再删目录
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete chunk file: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("Failed to delete chunk dir: {}", sessionDir, e);
        }
    }


}
