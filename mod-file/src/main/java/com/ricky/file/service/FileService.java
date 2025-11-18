package com.ricky.file.service;

import com.ricky.file.domain.dto.FileUploadCommand;
import jakarta.validation.Valid;

public interface FileService {

    String upload(@Valid FileUploadCommand dto);

}
