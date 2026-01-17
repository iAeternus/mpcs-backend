package com.ricky.like.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.like.domain.Like;
import com.ricky.like.domain.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MongoLikeRepository extends MongoBaseRepository<Like> implements LikeRepository {

    @Override
    public void save(List<Like> likes) {
        super.save(likes);
    }
}
