package com.ricky.publicfile.query;

import com.ricky.common.domain.page.SearchablePageQuery;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

/**
 * search: originalFileId/publisher/title
 * sortedBy: title/likeCount/commentCount/createdAt
 */
@Value
@SuperBuilder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicFilePageQuery extends SearchablePageQuery {

}
