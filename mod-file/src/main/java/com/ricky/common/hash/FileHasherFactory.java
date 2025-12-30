package com.ricky.common.hash;

import com.ricky.common.exception.MyException;
import com.ricky.common.properties.FileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

import static com.ricky.common.exception.ErrorCodeEnum.INVALID_HASH_ALGORITHM;

@Component
public class FileHasherFactory {

    private final FileProperties properties;
    private final Map<HashAlgorithm, AbstractFileHasher> hashers;

    @Autowired
    public FileHasherFactory(FileProperties properties,
                             Md5FileHasher md5Hasher,
                             Sha256FileHasher sha256Hasher,
                             Sha512FileHasher sha512Hasher) {
        this.properties = properties;
        this.hashers = new EnumMap<>(HashAlgorithm.class);
        hashers.put(HashAlgorithm.MD5, md5Hasher);
        hashers.put(HashAlgorithm.SHA256, sha256Hasher);
        hashers.put(HashAlgorithm.SHA512, sha512Hasher);
    }

    public AbstractFileHasher getFileHasher() {
        HashAlgorithm algorithm = properties.getHash().getAlgorithm();
        AbstractFileHasher fileHasher = hashers.get(algorithm);
        if (fileHasher == null) {
            throw new MyException(INVALID_HASH_ALGORITHM, "Invalid hash algorithm", Map.of("algorithm", algorithm.getName()));
        }
        return fileHasher;
    }

}
