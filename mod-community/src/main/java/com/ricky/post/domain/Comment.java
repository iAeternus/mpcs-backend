package com.ricky.post.domain;

import com.ricky.common.domain.marker.Identified;
import lombok.*;

/**
 * @brief 评论
 */
@Value
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment implements Identified {

    String id;

    // TODO

}
