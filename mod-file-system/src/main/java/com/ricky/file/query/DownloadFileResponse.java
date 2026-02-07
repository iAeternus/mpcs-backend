package com.ricky.file.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;

import static com.ricky.common.constants.ConfigConstants.DEFAULT_CHARSET;
import static org.apache.tika.metadata.HttpHeaders.CONTENT_DISPOSITION;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadFileResponse implements Response {

    String filename;
    String contentType; // MIME 类型
    Long size; // 文件大小（字节）
    InputStreamResource resource;

    /**
     * 用于下载（浏览器弹出保存框）
     */
    public ResponseEntity<Resource> toDownloadResponse() {
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, disposition("attachment"))
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(size)
                .body(resource);
    }

    /**
     * 用于预览（浏览器直接打开，如 PDF / 图片 / 视频）
     */
    public ResponseEntity<Resource> toPreviewResponse() {
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, disposition("inline"))
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(size)
                .body(resource);
    }

    private String disposition(String type) {
        return type + "; filename=\"" +
                URLEncoder.encode(filename, DEFAULT_CHARSET) + "\"";
    }
}
