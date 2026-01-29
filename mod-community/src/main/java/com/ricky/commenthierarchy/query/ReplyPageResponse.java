package com.ricky.commenthierarchy.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplyPageResponse implements Response {

    String parentId; // 父评论ID
    String recipient; // 回复对象
    String userId; // 回复发布者
    String content; // 内容

}
