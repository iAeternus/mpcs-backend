package com.ricky.common.hash;

import org.springframework.stereotype.Component;

@Component("md5FileHasher")
public class Md5FileHasher extends AbstractFileHasher {
    @Override
    public HashAlgorithm algorithm() {
        return HashAlgorithm.MD5;
    }
}
