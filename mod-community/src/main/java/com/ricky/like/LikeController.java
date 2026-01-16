package com.ricky.like;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Tag(name = "点赞模块")
@RequestMapping("/like") // TODO 这里不一定要前缀
public class LikeController {
}
