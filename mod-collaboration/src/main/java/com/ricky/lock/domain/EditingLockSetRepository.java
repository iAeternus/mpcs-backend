package com.ricky.lock.domain;

import java.util.Optional;

public interface EditingLockSetRepository {

    void save(EditingLockSet lockSet);

    Optional<EditingLockSet> findBySessionId(String sessionId);
}
