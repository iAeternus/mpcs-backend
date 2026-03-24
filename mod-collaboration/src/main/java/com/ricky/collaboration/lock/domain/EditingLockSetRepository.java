package com.ricky.collaboration.lock.domain;

import java.util.Optional;

public interface EditingLockSetRepository {

    void save(EditingLockSet lockSet);

    Optional<EditingLockSet> findBySessionId(String sessionId);
}
