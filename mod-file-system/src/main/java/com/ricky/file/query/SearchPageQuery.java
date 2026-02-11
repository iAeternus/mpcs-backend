package com.ricky.file.query;

import com.ricky.common.domain.page.PageQuery;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import static com.ricky.common.constants.ConfigConstants.MAX_GENERIC_TEXT_LENGTH;

@Value
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchPageQuery extends PageQuery {

    @Nullable
    @Size(max = MAX_GENERIC_TEXT_LENGTH)
    String keyword;

}
