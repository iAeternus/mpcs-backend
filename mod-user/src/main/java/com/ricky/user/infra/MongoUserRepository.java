package com.ricky.user.infra;

import com.ricky.common.mongo.MongoBaseRepository;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoUserRepository extends MongoBaseRepository<User> implements UserRepository {

    private final MongoCachedUserRepository cachedUserRepository;

    @Override
    public User cachedById(String userId) {
        requireNotBlank(userId, "User ID must not be blank.");
        return cachedUserRepository.cachedById(userId);
    }

    @Override
    public Optional<User> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public void save(User user) {
        super.save(user);
        cachedUserRepository.evictUserCache(user.getId());
    }

    @Override
    public boolean existsByMobileOrEmail(String mobileOrEmail) {
        requireNotBlank(mobileOrEmail, "Mobile or email must not be blank.");

        Criteria criteria = new Criteria();
        criteria.orOperator(where("mobile").is(mobileOrEmail), where("email").is(mobileOrEmail));
        return mongoTemplate.exists(query(criteria), User.class);
    }

    @Override
    public User byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<User> byMobileOrEmailOptional(String mobileOrEmail) {
        requireNotBlank(mobileOrEmail, "Mobile or email must not be blank.");

        Criteria criteria = new Criteria();
        criteria.orOperator(where("mobile").is(mobileOrEmail), where("email").is(mobileOrEmail));
        return ofNullable(mongoTemplate.findOne(query(criteria), User.class));
    }

    @Override
    public boolean existsByMobile(String mobile) {
        requireNotBlank(mobile, "Mobile must not be blank.");

        Query query = query(where("mobile").is(mobile));
        return mongoTemplate.exists(query, User.class);
    }

    @Override
    public boolean allUserExists(List<String> userIds) {
        requireNonNull(userIds, "User IDs must not be null");

        if (isEmpty(userIds)) {
            return true;
        }

        Query query = query(where("_id").in(userIds));
        long count = mongoTemplate.count(query, User.class);

        return count == userIds.size();
    }
}
