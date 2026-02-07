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

    public ResponseEntity<Resource> toResponseEntity() {
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, DEFAULT_CHARSET) + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(size)
                .body(resource);
    }

}
