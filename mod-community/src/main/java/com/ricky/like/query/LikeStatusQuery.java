package com.ricky.like.query;

import com.ricky.common.domain.marker.Query;
import com.ricky.common.validation.collection.NoNullElement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeStatusQuery implements Query {

    @NotNull
    @NoNullElement
    @Size(max = 1000, min = 1)
    List<String> postIds;

}
