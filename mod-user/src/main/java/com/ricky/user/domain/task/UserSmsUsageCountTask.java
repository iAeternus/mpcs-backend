package com.ricky.user.domain.task;

import com.ricky.common.domain.task.NonRetryableTask;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSmsUsageCountTask implements NonRetryableTask {

    private final UserRepository userRepository;

    @Transactional
    public void run(String userId) {
        userRepository.byIdOptional(userId).ifPresent(user -> {
            user.useSms();
            userRepository.save(user);
        });
    }

}
