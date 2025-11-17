package com.ricky.common.domain;

import com.ricky.common.context.ThreadLocalContext;
import com.ricky.common.context.UserContext;
import com.ricky.common.domain.event.DomainEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static com.ricky.common.domain.AggregateRoot.MAX_OPS_LOG_SIZE;
import static com.ricky.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static org.junit.jupiter.api.Assertions.*;

class AggregateRootTest {

    private static final UserContext TEST_USER = UserContext.of("USR" + newSnowflakeId(), "test username");

    static {
        ThreadLocalContext.setContext(TEST_USER);
    }

    @AfterAll
    static void tearDown() {
        ThreadLocalContext.removeContext();
    }

    @Test
    public void should_create() {
        String id = "Test" + newSnowflakeId();

        TestAggregate aggregate = new TestAggregate(id);
        assertEquals(id, aggregate.getId());
        assertEquals(TEST_USER.getUid(), aggregate.getUserId());

        assertEquals(TEST_USER.getUid(), aggregate.getCreatedBy());
        assertNotNull(aggregate.getCreatedAt());

        assertNull(aggregate.getEvents());
        assertNull(aggregate.getOpsLogs());

        assertEquals(id, aggregate.getId());
    }

    @Test
    public void should_raise_event() {
        String id = "Test" + newSnowflakeId();
        TestAggregate aggregate = new TestAggregate(id);
        DomainEvent event = new DomainEvent() {
        };

        aggregate.raiseEvent(event);
        assertSame(event, aggregate.getEvents().get(0));
    }

    @Test
    public void should_add_ops_log() {
        String id = "Test" + newSnowflakeId();
        TestAggregate aggregate = new TestAggregate(id);
        String opsLog = "Hello ops logs";
        aggregate.addOpsLog(opsLog);
        assertEquals(opsLog, aggregate.getOpsLogs().get(0).getNote());
    }

    @Test
    public void should_slice_ops_logs_if_too_much() {
        String id = "Test" + newSnowflakeId();
        TestAggregate aggregate = new TestAggregate(id);
        String opsLog = "Hello ops logs";
        IntStream.rangeClosed(0, MAX_OPS_LOG_SIZE).forEach(i -> aggregate.addOpsLog(opsLog));
        String lastLog = "last ops log";
        aggregate.addOpsLog(lastLog);
        assertEquals(MAX_OPS_LOG_SIZE, aggregate.getOpsLogs().size());
        assertEquals(lastLog, aggregate.getOpsLogs().getLast().getNote());
    }

    static class TestAggregate extends AggregateRoot {

        public TestAggregate(String id) {
            super(id);
        }
    }

}