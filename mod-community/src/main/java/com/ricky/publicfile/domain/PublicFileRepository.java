package com.ricky.publicfile.domain;

import java.util.List;
import java.util.Set;

public interface PublicFileRepository {
    void save(PublicFile publicFile);

    void save(List<PublicFile> publicFiles);

    PublicFile byId(String postId);

    void delete(PublicFile publicFile);

    boolean exists(String postId);

    List<PublicFile> byIds(Set<String> postIds);

    PublicFile cachedById(String postId);
}
