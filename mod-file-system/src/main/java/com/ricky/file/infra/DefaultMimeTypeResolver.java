package com.ricky.file.infra;

import com.ricky.file.domain.FileExtension;
import com.ricky.file.domain.MimeTypeResolver;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

import static com.ricky.common.utils.ValidationUtils.isNull;

@Component
public class DefaultMimeTypeResolver implements MimeTypeResolver {

    private static final Map<FileExtension, String> MAP = new EnumMap<>(FileExtension.class);

    static {
        // 图片
        MAP.put(FileExtension.JPG, "image/jpeg");
        MAP.put(FileExtension.JPEG, "image/jpeg");
        MAP.put(FileExtension.PNG, "image/png");
        MAP.put(FileExtension.GIF, "image/gif");
        MAP.put(FileExtension.BMP, "image/bmp");
        MAP.put(FileExtension.WEBP, "image/webp");
        MAP.put(FileExtension.SVG, "image/svg+xml");

        // 文档
        MAP.put(FileExtension.PDF, "application/pdf");
        MAP.put(FileExtension.DOC, "application/msword");
        MAP.put(FileExtension.DOCX, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MAP.put(FileExtension.XLS, "application/vnd.ms-excel");
        MAP.put(FileExtension.XLSX, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MAP.put(FileExtension.PPT, "application/vnd.ms-powerpoint");
        MAP.put(FileExtension.PPTX, "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // 文本
        MAP.put(FileExtension.TXT, "text/plain");
        MAP.put(FileExtension.CSV, "text/csv");
        MAP.put(FileExtension.HTML, "text/html");
        MAP.put(FileExtension.HTM, "text/html");
        MAP.put(FileExtension.XML, "application/xml");
        MAP.put(FileExtension.JSON, "application/json");

        // 压缩
        MAP.put(FileExtension.ZIP, "application/zip");
        MAP.put(FileExtension.RAR, "application/vnd.rar");
        MAP.put(FileExtension.SEVEN_Z, "application/x-7z-compressed");
        MAP.put(FileExtension.TAR, "application/x-tar");
        MAP.put(FileExtension.GZ, "application/gzip");

        // 音视频
        MAP.put(FileExtension.MP4, "video/mp4");
        MAP.put(FileExtension.MP3, "audio/mpeg");
        MAP.put(FileExtension.AVI, "video/x-msvideo");
        MAP.put(FileExtension.WAV, "audio/wav");

        // 可执行
        MAP.put(FileExtension.EXE, "application/octet-stream");
        MAP.put(FileExtension.BIN, "application/octet-stream");

        // 兜底
        MAP.put(FileExtension.UNKNOWN, "application/octet-stream");
    }

    @Override
    public String resolve(FileExtension ext) {
        if (isNull(ext)) {
            return "application/octet-stream";
        }
        return MAP.getOrDefault(ext, "application/octet-stream");
    }
}
