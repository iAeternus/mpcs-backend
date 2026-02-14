package com.ricky.fileextra.domain;

import com.ricky.file.domain.storage.GridFsStorageId;
import com.ricky.file.domain.storage.OssStorageId;
import com.ricky.file.domain.storage.StorageId;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static com.ricky.common.constants.ConfigConstants.DEFAULT_CHARSET;

/**
 * Text file cache utilities.
 */
public final class TextFileCache {

    private static final String PREFIX_GRIDFS = "gridfs:";
    private static final String PREFIX_OSS = "oss:";

    private TextFileCache() {
    }

    public static String buildKey(StorageId storageId) {
        String raw;
        if (storageId instanceof GridFsStorageId gridFsStorageId) {
            raw = PREFIX_GRIDFS + gridFsStorageId.getValue();
        } else if (storageId instanceof OssStorageId ossStorageId) {
            raw = PREFIX_OSS + ossStorageId.getBucket() + "/" + ossStorageId.getObjectKey();
        } else {
            raw = storageId.getValue();
        }
        return sha256Hex(raw);
    }

    public static String buildPath(String textFileDir, String textFileKey) {
        return Path.of(textFileDir, textFileKey + ".txt").toString();
    }

    public static String buildPath(String textFileDir, StorageId storageId) {
        return buildPath(textFileDir, buildKey(storageId));
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(DEFAULT_CHARSET));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
