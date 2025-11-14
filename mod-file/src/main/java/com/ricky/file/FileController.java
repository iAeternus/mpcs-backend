package com.ricky.file;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@CrossOrigin
@RestController
@Tag(name = "文档模块")
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    // TODO

}
