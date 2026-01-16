package com.ricky.publicfile.domain;

public interface PublicFileRepository {
    void save(PublicFile publicFile);

    PublicFile byId(String postId);

    void delete(PublicFile publicFile);

    boolean exists(String postId);

}
