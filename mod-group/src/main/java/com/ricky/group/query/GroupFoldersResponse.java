package com.ricky.group.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupFoldersResponse implements Response {

    String folderId;
    String parentId;
    String folderName;

}
