package com.ricky.group.query;

import com.ricky.common.domain.page.SearchablePageQuery;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

/**
 * search: groupId/name
 * sortedBy: name/createdAt/active
 */
@Value
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyGroupsAsForManagerPageQuery extends SearchablePageQuery {

}
