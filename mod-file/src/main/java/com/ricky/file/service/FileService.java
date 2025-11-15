package com.ricky.file.service;

import com.ricky.file.domain.dto.FileUploadDTO;
import jakarta.validation.Valid;

public interface FileService {

    String upload(@Valid FileUploadDTO dto);

}
