package com.ricky.file;

import com.ricky.file.domain.dto.FileUploadCommand;
import com.ricky.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文档模块")
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    @PostMapping
    @Operation(summary = "普通上传")
    public String upload(@ModelAttribute @Valid FileUploadCommand dto) {
        return fileService.upload(dto);
    }

}
