package com.ricky.file.infra.impl;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.file.domain.File;
import com.ricky.file.infra.FileRepository;
import com.ricky.file.infra.MongoCachedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoFileRepository extends MongoBaseRepository<File> implements FileRepository {

    private final MongoCachedFileRepository cachedFileRepository;

}
