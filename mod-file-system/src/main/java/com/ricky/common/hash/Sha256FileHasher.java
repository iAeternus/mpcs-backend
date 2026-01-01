package com.ricky.common.hash;

import org.springframework.stereotype.Component;

@Component("sha256FileHasher")
public class Sha256FileHasher extends AbstractFileHasher {
    @Override
    public HashAlgorithm algorithm() {
        return HashAlgorithm.SHA256;
    }
}
