package com.ricky.publicfile.service.impl;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import com.ricky.publicfile.service.PublicFileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicFileQueryServiceImpl implements PublicFileQueryService {

    @Override
    public PagedList<PublicFileResponse> page(PublicFilePageQuery query, UserContext userContext) {
        // TODO
        return null;
    }
}
