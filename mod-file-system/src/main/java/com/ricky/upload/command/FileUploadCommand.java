package com.ricky.upload.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.exception.MyException;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.path.Path;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_MUST_NOT_BE_EMPTY;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK;
import static com.ricky.common.utils.ValidationUtils.isBlank;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUploadCommand implements Command {

    @NotNull
    MultipartFile file;

    @NotBlank
    @Id(FOLDER_ID_PREFIX)
    String parentId;

    @NotBlank
    @Path
    String path;

    @Override
    public void correctAndValidate() {
        if (file.isEmpty()) {
            throw new MyException(FILE_MUST_NOT_BE_EMPTY, "文件不能为空", "filename", file.getName());
        }
        if (isBlank(file.getOriginalFilename())) {
            throw new MyException(FILE_ORIGINAL_NAME_MUST_NOT_BE_BLANK, "文件原始名称不能为空", "filename", file.getName());
        }
    }

}
