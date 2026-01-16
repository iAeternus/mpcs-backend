package com.ricky.publicfile.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MongoPublicFileRepository extends MongoBaseRepository<PublicFile> implements PublicFileRepository {

    @Override
    public void save(PublicFile publicFile) {
        super.save(publicFile);
    }

    @Override
    public PublicFile byId(String id) {
        return super.byId(id);
    }

    @Override
    public void delete(PublicFile publicFile) {
        super.delete(publicFile);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }
}
