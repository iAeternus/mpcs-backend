package com.ricky.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void should_generate() {
        System.out.println(SnowflakeIdGenerator.newSnowflakeId());
    }

}