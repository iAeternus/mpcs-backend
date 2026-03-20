package com.ricky.file.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ricky.common.constants.ConfigConstants.DEFAULT_CHARSET;
import static org.apache.tika.metadata.HttpHeaders.CONTENT_DISPOSITION;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadFileResponse implements Response {

    String filename;
    String contentType;
    Long size;
    InputStreamResource resource;

    /**
     * 下载响应：浏览器弹出保存框（attachment），同时声明支持 Range 请求（Accept-Ranges: bytes）。
     * Accept-Ranges 告知客户端可以发送 Range header 进行断点续传或多段请求。
     */
    public ResponseEntity<Resource> toDownloadResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_DISPOSITION, disposition("attachment"));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(size)
                .body(resource);
    }

    /**
     * 预览响应：浏览器直接打开（inline），如 PDF、图片、视频。
     * 同样声明 Accept-Ranges，使视频播放器能够发起 Range 请求分段加载。
     */
    public ResponseEntity<Resource> toPreviewResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_DISPOSITION, disposition("inline"));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(size)
                .body(resource);
    }

    /**
     * 预览响应，支持自定义状态码与响应头。
     * 用于 206 Partial Content（Range 请求成功）或 416 Range Not Satisfiable。
     */
    public ResponseEntity<Resource> toPreviewResponse(HttpStatus status, HttpHeaders headers) {
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private String disposition(String type) {
        return type + "; filename=\"" +
                URLEncoder.encode(filename, DEFAULT_CHARSET) + "\"";
    }

    /**
     * HTTP Range 请求中解析出的字节区间。
     * 例如 Range: bytes=0-1023 表示请求文件第 0 到 1023 字节（共 1024 字节）。
     *
     * @param rangeStart 请求起始字节偏移（从 0 开始）
     * @param rangeEnd   请求结束字节偏移（闭区间）
     */
    public record RangeInfo(long rangeStart, long rangeEnd) {
    }

    /**
     * 解析客户端请求的 Range header。
     * 格式：Range: bytes=start-end
     * <p>
     * RFC 7233 规范：
     * - bytes=0..499      → 请求前 500 字节
     * - bytes=500..       → 从第 500 字节到文件末尾
     * - bytes=..500       → 请求最后 500 字节（本实现未支持）
     * </p>
     *
     * @param rangeHeader 客户端发送的 Range header（可能为 null）
     * @param fileSize    文件总字节数
     * @return 解析成功返回 RangeInfo；格式错误或越界返回 Optional.empty()，由调用方返回 416
     */
    public static Optional<RangeInfo> parseRange(String rangeHeader, long fileSize) {
        if (rangeHeader == null || rangeHeader.isEmpty()) {
            return Optional.empty();
        }
        Pattern rangePattern = Pattern.compile("bytes=(\\d+)-(\\d*)");
        Matcher matcher = rangePattern.matcher(rangeHeader);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        long rangeStart = Long.parseLong(matcher.group(1));
        if (rangeStart >= fileSize) {
            return Optional.empty();
        }
        String endStr = matcher.group(2);
        long rangeEnd = endStr.isEmpty() ? fileSize - 1 : Long.parseLong(endStr);
        if (rangeEnd >= fileSize) {
            rangeEnd = fileSize - 1;
        }
        return Optional.of(new RangeInfo(rangeStart, rangeEnd));
    }

    /**
     * 构建 416 Range Not Satisfiable 响应。
     * <p>
     * RFC 7233：当请求的区间超出文件范围时返回此状态码。
     * Content-Range: bytes * / fileSize 告知客户端文件总大小。
     */
    public static ResponseEntity<Resource> rangeNotSatisfiable(long fileSize) {
        return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                .build();
    }

    /**
     * 构建 206 Partial Content 响应的响应头。
     * <p>
     * 三个关键 header：
     * - Accept-Ranges: bytes    声明服务器支持按字节为单位进行范围请求
     * - Content-Range: bytes start-end/fileSize   告知本次返回的区间及文件总大小
     * - Content-Length: 本次返回的实际字节数
     *
     * @param rangeStart 当前区间起始字节
     * @param rangeEnd   当前区间结束字节
     * @param fileSize   文件总字节数
     */
    public static HttpHeaders buildRangeHeaders(long rangeStart, long rangeEnd, long fileSize) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);
        headers.setContentLength(rangeEnd - rangeStart + 1);
        return headers;
    }
}
