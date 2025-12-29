package com.ricky.common.validation.path;


import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.ricky.common.constants.RegexConstants.PATH_PATTERN;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathValidatorTest {

    static Pattern PATTERN = Pattern.compile(PATH_PATTERN);

    @Test
    void should_path_pattern_match() {
        // Given
        String path1 = "/";
        String path2 = "/home";
        String path3 = "/usr/local/bin";
        String path4 = "/folder-1";
        String path5 = "/my_doc";

        // When & Then
        assertTrue(PATTERN.matcher(path1).matches());
        assertTrue(PATTERN.matcher(path2).matches());
        assertTrue(PATTERN.matcher(path3).matches());
        assertTrue(PATTERN.matcher(path4).matches());
        assertTrue(PATTERN.matcher(path5).matches());
    }

    @Test
    void should_path_pattern_not_match() {
        // Given
        String path1 = "";
        String path2 = "home";
        String path3 = "/path//double";
        String path4 = "/path/";
        String path5 = "/path with spaces";
        String path6 = "/path@home";

        // When & Then
        assertFalse(PATTERN.matcher(path1).matches());
        assertFalse(PATTERN.matcher(path2).matches());
        assertFalse(PATTERN.matcher(path3).matches());
        assertFalse(PATTERN.matcher(path4).matches());
        assertFalse(PATTERN.matcher(path5).matches());
        assertFalse(PATTERN.matcher(path6).matches());
    }

}