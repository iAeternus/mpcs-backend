package com.ricky.collaboration.lock.domain;

import com.ricky.common.domain.user.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditingLockDomainService {

    private final EditingLockSetRepository repository;

    public EditingLockSet getOrCreate(String sessionId, String documentId, UserContext userContext) {
        return repository.findBySessionId(sessionId)
                .orElseGet(() -> EditingLockSet.create(sessionId, documentId, userContext));
    }
}
