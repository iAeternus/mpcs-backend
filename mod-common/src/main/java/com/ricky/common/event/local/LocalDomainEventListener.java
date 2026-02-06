package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalDomainEventListener {

    private final LocalDomainEventConsumer consumer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLocalDomainEvent(LocalDomainEvent event) {
        consumer.consume(event);
    }
}
