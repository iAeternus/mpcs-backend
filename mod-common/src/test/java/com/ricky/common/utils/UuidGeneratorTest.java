package com.ricky.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UuidGeneratorTest {

    @Test
    public void should_generate_random_base64_uuid() {
        String uuid = UuidGenerator.newShortUuid();
        assertEquals(22, uuid.length());
    }

}