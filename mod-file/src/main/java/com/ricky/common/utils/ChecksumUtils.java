package com.ricky.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

public class ChecksumUtils {

    public static long crc32(InputStream inputStream) throws IOException {
        CRC32 crc32 = new CRC32();
        byte[] buf = new byte[8192];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            crc32.update(buf, 0, len);
        }
        return crc32.getValue();
    }

}
