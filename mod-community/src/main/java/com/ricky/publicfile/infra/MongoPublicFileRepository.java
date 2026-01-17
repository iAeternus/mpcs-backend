package com.ricky.publicfile.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MongoPublicFileRepository extends MongoBaseRepository<PublicFile> implements PublicFileRepository {

    private final MongoCachedPublicFileRepository cachedPublicFileRepository;

    @Override
    public void save(PublicFile publicFile) {
        super.save(publicFile);
        cachedPublicFileRepository.evictPublicFileCache(publicFile.getId());
    }

    @Override
    public void save(List<PublicFile> publicFiles) {
        super.save(publicFiles);
        cachedPublicFileRepository.evictAll();
    }

    @Override
    public PublicFile byId(String id) {
        return super.byId(id);
    }

    @Override
    public void delete(PublicFile publicFile) {
        super.delete(publicFile);
        cachedPublicFileRepository.evictPublicFileCache(publicFile.getId());
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public List<PublicFile> byIds(Set<String> ids) {
        return super.byIds(ids);
    }

    @Override
    public PublicFile cachedById(String postId) {
        return cachedPublicFileRepository.cachedById(postId);
    }
}
