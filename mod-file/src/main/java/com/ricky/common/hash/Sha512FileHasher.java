package com.ricky.common.hash;

import org.springframework.stereotype.Component;

@Component("sha512FileHasher")
public class Sha512FileHasher extends AbstractFileHasher {
    @Override
    public HashAlgorithm algorithm() {
        return HashAlgorithm.SHA512;
    }
}
