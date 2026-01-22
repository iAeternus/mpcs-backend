package com.ricky.apitest;

import com.ricky.common.domain.dto.resp.LoginResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.io.File;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TestFileContext {

    LoginResponse manager;
    String customId;
    String parentId;
    String fileHash;
    String fileId;
    File originalFile;

}
