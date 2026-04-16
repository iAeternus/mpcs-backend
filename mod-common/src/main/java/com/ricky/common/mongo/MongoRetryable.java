package com.ricky.common.mongo;

import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    value = UncategorizedMongoDbException.class,
    exceptionExpression = "#root.message.contains('WriteConflict error') or " +
                         "#root.message.contains('TransientTransactionError')",
    maxAttempts = 3,
    backoff = @Backoff(delay = 100, multiplier = 2)
)
public @interface MongoRetryable {
}
