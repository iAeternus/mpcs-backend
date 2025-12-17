package com.ricky.testsuite;

import com.ricky.file.domain.FileExtension;
import com.ricky.file.domain.MimeType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class SetUpService {

    /**
     * @param path 相对于测试资源目录下的文件路径，在其中要指定文件名和扩展名，例如 data/test_file.txt
     * @param name 表单名称
     * @return 文件
     * @brief 加载测试文件
     */
    public MultipartFile loadTestFile(String path, String name) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String filename = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
            FileExtension fileExtension = FileExtension.fromFilename(filename);
            MimeType mimeType = MimeType.fromExtension(fileExtension);

            return new MockMultipartFile(
                    name,
                    filename,
                    mimeType.getContentType(),
                    StreamUtils.copyToByteArray(resource.getInputStream())
            );
        } catch (IOException e) {
            throw new RuntimeException("无法读取测试文件: " + path, e);
        }
    }

}
