package com.ricky.user.domain;

import java.util.Optional;

public interface UserRepository {

    User cachedById(String userId);

    Optional<User> byIdOptional(String userId);

    void save(User user);

    boolean existsByMobileOrEmail(String mobileOrEmail);

    User byId(String userId);

    Optional<User> byMobileOrEmailOptional(String mobileOrEmail);

    boolean existsByMobile(String mobile);
}
