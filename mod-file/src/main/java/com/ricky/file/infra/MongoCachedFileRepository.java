package com.ricky.file.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.file.domain.File;
import org.springframework.stereotype.Repository;

@Repository
public class MongoCachedFileRepository extends MongoBaseRepository<File> {

    // TODO

}
