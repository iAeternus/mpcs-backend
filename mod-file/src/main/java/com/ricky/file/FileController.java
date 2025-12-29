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

//    @PostMapping
//    @Operation(summary = "普通上传")
//    @ResponseStatus(CREATED)
//    public String upload(@ModelAttribute @Valid FileUploadCommand dto) {
//        return fileService.upload(dto);
//    }


    @Operation(summary = "普通上传")
    @ResponseStatus(CREATED)
    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse upload(@RequestParam("file") @NotNull MultipartFile file,
                                     @RequestParam("parentId") @Id(pre = FOLDER_ID_PREFIX) @NotBlank String parentId,
                                     @RequestParam("path") @Path @NotBlank String path,
                                     @AuthenticationPrincipal UserContext userContext) {
        return fileService.upload(file, parentId, path, userContext);
    }

}
