package com.ricky.like.domain;

import java.util.List;

public interface LikeRepository {
    void save(List<Like> likes);
}
