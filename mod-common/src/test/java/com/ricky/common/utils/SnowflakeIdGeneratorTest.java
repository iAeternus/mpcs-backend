package com.ricky.common.utils;

import org.junit.jupiter.api.Test;

class SnowflakeIdGeneratorTest {

    @Test
    void should_generate() {
        System.out.println(SnowflakeIdGenerator.newSnowflakeId());
    }

}