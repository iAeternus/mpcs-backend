package com.ricky.commenthierarchy;

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
@Tag(name = "评论层次结构模块")
@RequestMapping("/comment-hierarchy")
public class CommentHierarchyController {
}
