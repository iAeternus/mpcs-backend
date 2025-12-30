package com.ricky.file;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.path.Path;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
import com.ricky.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文档模块")
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

}
