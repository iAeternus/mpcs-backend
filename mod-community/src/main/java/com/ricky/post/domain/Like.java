package com.ricky.post.domain;

import com.ricky.common.domain.marker.Identified;
import lombok.*;

/**
 * @brief 点赞
 */
@Value
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Like implements Identified {

    String id;

    // TODO

}
